package ict.minesunshineone.antiboom.listener;

import ict.minesunshineone.antiboom.service.ExplosionProtectionService;
import ict.minesunshineone.antiboom.service.WindChargeProtectionService;
import org.bukkit.entity.Breeze;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Explosive;
import org.bukkit.entity.Vehicle;
import org.bukkit.entity.WindCharge;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.hanging.HangingBreakEvent;
import org.bukkit.event.hanging.HangingBreakByEntityEvent;
import org.bukkit.event.hanging.HangingBreakEvent.RemoveCause;
import org.bukkit.event.vehicle.VehicleDamageEvent;
import org.bukkit.event.vehicle.VehicleDestroyEvent;
import org.bukkit.util.Vector;

import java.util.Objects;

public final class CustomEntityProtectionListener implements Listener {

    private final ExplosionProtectionService explosionProtectionService;
    private final WindChargeProtectionService windChargeProtectionService;

    public CustomEntityProtectionListener(ExplosionProtectionService explosionProtectionService,
                                          WindChargeProtectionService windChargeProtectionService) {
        this.explosionProtectionService = Objects.requireNonNull(explosionProtectionService, "explosionProtectionService");
        this.windChargeProtectionService = Objects.requireNonNull(windChargeProtectionService, "windChargeProtectionService");
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onEntityDamage(EntityDamageEvent event) {
        EntityDamageEvent.DamageCause cause = event.getCause();

        if (isExplosionCause(cause)) {
            if (explosionProtectionService.shouldProtectEntity(event.getEntity())) {
                event.setCancelled(true);
                if (event.getEntity() instanceof Vehicle vehicle) {
                    resetVehicleVelocity(vehicle);
                }
                return;
            }
        }

        if (windChargeProtectionService.isProtectionEnabled()
                && event instanceof EntityDamageByEntityEvent byEntity
                && byEntity.getDamager() instanceof WindCharge
                && windChargeProtectionService.isProtectedEntity(event.getEntity())) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onHangingBreak(HangingBreakEvent event) {
        EntityType type = event.getEntity().getType();

        RemoveCause cause = event.getCause();

        if (cause == RemoveCause.EXPLOSION
                && explosionProtectionService.shouldProtectEntity(event.getEntity())) {
            event.setCancelled(true);
            return;
        }

        if (cause == RemoveCause.ENTITY && event instanceof HangingBreakByEntityEvent byEntity) {
            if (byEntity.getRemover() instanceof WindCharge || byEntity.getRemover() instanceof Breeze) {
                if (windChargeProtectionService.isProtectionEnabled()
                        && windChargeProtectionService.isProtectedEntity(event.getEntity())) {
                    event.setCancelled(true);
                    return;
                }

                // Wind Charge also pushes without removing cause when item frame is empty; ensure protection applies.
                if (type == EntityType.ITEM_FRAME || type == EntityType.GLOW_ITEM_FRAME) {
                    if (windChargeProtectionService.isProtectionEnabled()
                            && windChargeProtectionService.isProtectedEntity(event.getEntity())) {
                        event.setCancelled(true);
                    }
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onVehicleDamage(VehicleDamageEvent event) {
        Vehicle vehicle = event.getVehicle();
        if (vehicle == null) {
            return;
        }

        if (shouldCancelVehicleEvent(vehicle, event.getAttacker())) {
            event.setCancelled(true);
            resetVehicleVelocity(vehicle);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onVehicleDestroy(VehicleDestroyEvent event) {
        Vehicle vehicle = event.getVehicle();
        if (vehicle == null) {
            return;
        }

        if (shouldCancelVehicleEvent(vehicle, event.getAttacker())) {
            event.setCancelled(true);
            resetVehicleVelocity(vehicle);
        }
    }

    private static boolean isExplosionCause(EntityDamageEvent.DamageCause cause) {
        return cause == EntityDamageEvent.DamageCause.BLOCK_EXPLOSION
                || cause == EntityDamageEvent.DamageCause.ENTITY_EXPLOSION
                || cause == EntityDamageEvent.DamageCause.DRAGON_BREATH;
    }

    private boolean shouldCancelVehicleEvent(Vehicle vehicle, Entity attacker) {
        if (!explosionProtectionService.shouldProtectEntity(vehicle)) {
            return false;
        }

        if (attacker != null) {
            return isExplosiveAttacker(attacker);
        }

        EntityDamageEvent lastDamage = vehicle.getLastDamageCause();
        if (lastDamage != null && isExplosionCause(lastDamage.getCause())) {
            return true;
        }

        // 某些版本在爆炸摧毁载具时不会提供攻击者或最后伤害来源，
        // 此时依然遵循防护规则，避免误判导致船只被炸毁。
        return true;
    }

    private boolean isExplosiveAttacker(Entity attacker) {
        if (attacker instanceof Explosive) {
            return true;
        }

        EntityType type = attacker.getType();
        return type == EntityType.WITHER || type == EntityType.ENDER_DRAGON || type == EntityType.WITHER_SKULL;
    }

    private void resetVehicleVelocity(Vehicle vehicle) {
        vehicle.setVelocity(new Vector());
        vehicle.setFallDistance(0F);
    }
}
