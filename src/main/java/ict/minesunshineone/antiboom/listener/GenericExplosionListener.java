package ict.minesunshineone.antiboom.listener;

import ict.minesunshineone.antiboom.service.ExplosionProtectionService;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Fireball;
import org.bukkit.entity.Ghast;
import org.bukkit.entity.TNTPrimed;
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
            var mode = protectionService.protectExplosion(null, event.getLocation(), event.blockList(), event::setYield);
            if (mode.suppressBlocks()) {
                event.blockList().clear();
                event.setYield(0F);
                event.setCancelled(true);
            }
            return;
        }

        if (entity.getType() == EntityType.CREEPER || entity.getType() == EntityType.ENDER_DRAGON) {
            return;
        }

        if (entity instanceof Fireball fireball && fireball.getShooter() instanceof Ghast) {
            return;
        }

        var mode = protectionService.protectExplosion(entity, event.getLocation(), event.blockList(), event::setYield);
        if (mode.suppressBlocks()) {
            boolean isExplosiveVehicle = entity.getType() == EntityType.TNT_MINECART;
            if (protectionService.isRegionProtectionActive(event.getLocation())
                    && (entity instanceof TNTPrimed || isExplosiveVehicle)) {
                entity.remove();
            }
            event.blockList().clear();
            event.setYield(0F);
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockExplode(BlockExplodeEvent event) {
        Block block = event.getBlock();
        Location location = block != null ? block.getLocation() : null;
        var mode = protectionService.protectExplosion(null, location, event.blockList(), event::setYield);
        if (mode.suppressBlocks()) {
            event.blockList().clear();
            event.setYield(0F);
            event.setCancelled(true);
        }
    }
}
