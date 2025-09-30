package ict.minesunshineone.antiboom.listener;

import ict.minesunshineone.antiboom.service.ExplosionProtectionService;
import org.bukkit.entity.Entity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ExplosionPrimeEvent;

import java.util.Objects;

public final class RegionExplosionPrimeListener implements Listener {

    private final ExplosionProtectionService protectionService;

    public RegionExplosionPrimeListener(ExplosionProtectionService protectionService) {
        this.protectionService = Objects.requireNonNull(protectionService, "protectionService");
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onExplosionPrime(ExplosionPrimeEvent event) {
        Entity entity = event.getEntity();
        if (entity == null) {
            return;
        }

        if (protectionService.suppressRegionExplosiveEntity(entity, entity.getLocation())) {
            event.setCancelled(true);
        }
    }
}
