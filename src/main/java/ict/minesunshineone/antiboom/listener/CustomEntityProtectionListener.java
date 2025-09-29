package ict.minesunshineone.antiboom.listener;

import ict.minesunshineone.antiboom.service.ExplosionProtectionService;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.hanging.HangingBreakEvent;

import java.util.Objects;

public final class CustomEntityProtectionListener implements Listener {

    private final ExplosionProtectionService protectionService;

    public CustomEntityProtectionListener(ExplosionProtectionService protectionService) {
        this.protectionService = Objects.requireNonNull(protectionService, "protectionService");
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onEntityDamage(EntityDamageEvent event) {
        if (!protectionService.isCustomProtectionEnabled()) {
            return;
        }

        EntityType type = event.getEntityType();
        if (!protectionService.isProtectedEntity(type)) {
            return;
        }

        EntityDamageEvent.DamageCause cause = event.getCause();
        if (cause == EntityDamageEvent.DamageCause.BLOCK_EXPLOSION
                || cause == EntityDamageEvent.DamageCause.ENTITY_EXPLOSION
                || cause == EntityDamageEvent.DamageCause.DRAGON_BREATH) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onHangingBreak(HangingBreakEvent event) {
        if (!protectionService.isCustomProtectionEnabled()) {
            return;
        }

        if (event.getCause() != HangingBreakEvent.RemoveCause.EXPLOSION) {
            return;
        }

        if (protectionService.isProtectedEntity(event.getEntity().getType())) {
            event.setCancelled(true);
        }
    }
}
