package ict.minesunshineone.antiboom.listener;

import ict.minesunshineone.antiboom.service.ExplosionProtectionService;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityExplodeEvent;

import java.util.Objects;

public final class CreeperExplosionListener implements Listener {

    private final ExplosionProtectionService protectionService;

    public CreeperExplosionListener(ExplosionProtectionService protectionService) {
        this.protectionService = Objects.requireNonNull(protectionService, "protectionService");
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onCreeperExplode(EntityExplodeEvent event) {
        if (event.getEntityType() != EntityType.CREEPER) {
            return;
        }

        if (protectionService.suppressRegionExplosiveEntity(event.getEntity(), event.getLocation())) {
            event.setCancelled(true);
            return;
        }

        protectionService.protectExplosion(event.getEntity(), event.getLocation(), event.blockList(), event::setYield);
    }
}
