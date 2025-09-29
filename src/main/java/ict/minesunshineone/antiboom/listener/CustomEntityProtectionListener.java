package ict.minesunshineone.antiboom.listener;

import ict.minesunshineone.antiboom.service.ExplosionProtectionService;
import ict.minesunshineone.antiboom.service.WindChargeProtectionService;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.WindCharge;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.hanging.HangingBreakEvent;
import org.bukkit.event.hanging.HangingBreakByEntityEvent;
import org.bukkit.event.hanging.HangingBreakEvent.RemoveCause;

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
        EntityType type = event.getEntityType();

        EntityDamageEvent.DamageCause cause = event.getCause();

        if (explosionProtectionService.isExplosionProtectionEnabled()
                && explosionProtectionService.isExplosionProtectedEntity(type)
                && (cause == EntityDamageEvent.DamageCause.BLOCK_EXPLOSION
                || cause == EntityDamageEvent.DamageCause.ENTITY_EXPLOSION
                || cause == EntityDamageEvent.DamageCause.DRAGON_BREATH)) {
            event.setCancelled(true);
            return;
        }

        if (windChargeProtectionService.isProtectionEnabled()
                && event instanceof EntityDamageByEntityEvent byEntity
                && byEntity.getDamager() instanceof WindCharge
                && windChargeProtectionService.isProtectedEntity(type)) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onHangingBreak(HangingBreakEvent event) {
        EntityType type = event.getEntity().getType();

        RemoveCause cause = event.getCause();

        if (cause == RemoveCause.EXPLOSION
                && explosionProtectionService.isExplosionProtectionEnabled()
                && explosionProtectionService.isExplosionProtectedEntity(type)) {
            event.setCancelled(true);
            return;
        }

        if (cause == RemoveCause.ENTITY && event instanceof HangingBreakByEntityEvent byEntity) {
            if (byEntity.getRemover() instanceof WindCharge) {
                if (windChargeProtectionService.isProtectionEnabled()
                        && windChargeProtectionService.isProtectedEntity(type)) {
                    event.setCancelled(true);
                    return;
                }

                // Wind Charge also pushes without removing cause when item frame is empty; ensure protection applies.
                if (type == EntityType.ITEM_FRAME || type == EntityType.GLOW_ITEM_FRAME) {
                    if (windChargeProtectionService.isProtectionEnabled()
                            && windChargeProtectionService.isProtectedEntity(type)) {
                        event.setCancelled(true);
                    }
                }
            }
        }
    }
}
