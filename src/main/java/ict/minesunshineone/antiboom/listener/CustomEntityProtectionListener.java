package ict.minesunshineone.antiboom.listener;

import ict.minesunshineone.antiboom.service.ExplosionProtectionService;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.WindCharge;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.hanging.HangingBreakByEntityEvent;
import org.bukkit.event.hanging.HangingBreakEvent;

import java.util.Objects;

public final class CustomEntityProtectionListener implements Listener {

    private final ExplosionProtectionService protectionService;

    public CustomEntityProtectionListener(ExplosionProtectionService protectionService) {
        this.protectionService = Objects.requireNonNull(protectionService, "protectionService");
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onEntityDamage(EntityDamageEvent event) {
        EntityType type = event.getEntityType();

        EntityDamageEvent.DamageCause cause = event.getCause();

        if (protectionService.isExplosionProtectionEnabled()
                && protectionService.isExplosionProtectedEntity(type)
                && (cause == EntityDamageEvent.DamageCause.BLOCK_EXPLOSION
                || cause == EntityDamageEvent.DamageCause.ENTITY_EXPLOSION
                || cause == EntityDamageEvent.DamageCause.DRAGON_BREATH)) {
            event.setCancelled(true);
            return;
        }

    if (protectionService.isWindChargeProtectionEnabled()
        && event instanceof EntityDamageByEntityEvent byEntity
        && byEntity.getDamager() instanceof WindCharge
        && protectionService.isWindChargeProtectedEntity(type)) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onHangingBreak(HangingBreakEvent event) {
        EntityType type = event.getEntity().getType();

        if (event.getCause() == HangingBreakEvent.RemoveCause.EXPLOSION
                && protectionService.isExplosionProtectionEnabled()
                && protectionService.isExplosionProtectedEntity(type)) {
            event.setCancelled(true);
            return;
        }

        if (event.getCause() == HangingBreakEvent.RemoveCause.ENTITY
                && protectionService.isWindChargeProtectionEnabled()
                && protectionService.isWindChargeProtectedEntity(type)
                && event instanceof HangingBreakByEntityEvent byEntity
                && byEntity.getRemover() instanceof WindCharge) {
            event.setCancelled(true);
        }
    }
}
