package ict.minesunshineone.antiboom.listener;

import ict.minesunshineone.antiboom.service.ExplosionProtectionService;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.entity.EntityExplodeEvent;

import java.util.Objects;

public final class DragonProtectionListener implements Listener {

    private final ExplosionProtectionService protectionService;

    public DragonProtectionListener(ExplosionProtectionService protectionService) {
        this.protectionService = Objects.requireNonNull(protectionService, "protectionService");
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onDragonExplode(EntityExplodeEvent event) {
        if (event.getEntityType() != EntityType.ENDER_DRAGON) {
            return;
        }

        protectionService.protectExplosion(event.getEntity(), event.getLocation(), event.blockList(), event::setYield);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onDragonChangeBlock(EntityChangeBlockEvent event) {
        if (event.getEntityType() != EntityType.ENDER_DRAGON) {
            return;
        }

        if (protectionService.resolveMode(event.getEntity()).suppressBlocks()) {
            event.setCancelled(true);
        }
    }
}
