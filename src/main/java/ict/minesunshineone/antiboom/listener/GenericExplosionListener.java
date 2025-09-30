package ict.minesunshineone.antiboom.listener;

import ict.minesunshineone.antiboom.service.ExplosionProtectionService;
import org.bukkit.Location;
import org.bukkit.Tag;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Fireball;
import org.bukkit.entity.Ghast;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.entity.EntityExplodeEvent;

import java.util.Objects;

public final class GenericExplosionListener implements Listener {

    private final ExplosionProtectionService protectionService;

    public GenericExplosionListener(ExplosionProtectionService protectionService) {
        this.protectionService = Objects.requireNonNull(protectionService, "protectionService");
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onEntityExplode(EntityExplodeEvent event) {
        Entity entity = event.getEntity();
        if (entity == null) {
            protectionService.protectExplosion(null, event.getLocation(), event.blockList(), event::setYield);
            return;
        }

        if (protectionService.suppressRegionExplosiveEntity(entity, event.getLocation())) {
            event.setCancelled(true);
            return;
        }

        if (entity.getType() == EntityType.CREEPER || entity.getType() == EntityType.ENDER_DRAGON) {
            return;
        }

        if (entity instanceof Fireball fireball && fireball.getShooter() instanceof Ghast) {
            return;
        }

        protectionService.protectExplosion(entity, event.getLocation(), event.blockList(), event::setYield);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockExplode(BlockExplodeEvent event) {
        Block block = event.getBlock();
        Location location = block != null ? block.getLocation() : null;

        if (block != null && isBed(block) && protectionService.suppressRegionBlockExplosion(location)) {
            event.setCancelled(true);
            return;
        }

        protectionService.protectExplosion(null, location, event.blockList(), event::setYield);
    }

    private boolean isBed(Block block) {
        return Tag.BEDS.isTagged(block.getType());
    }
}
