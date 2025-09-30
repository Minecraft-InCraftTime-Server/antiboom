package ict.minesunshineone.antiboom.listener;

import ict.minesunshineone.antiboom.service.ExplosionProtectionService;
import org.bukkit.entity.Fireball;
import org.bukkit.entity.Ghast;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityExplodeEvent;

import java.util.Objects;

public final class GhastExplosionListener implements Listener {

    private final ExplosionProtectionService protectionService;

    public GhastExplosionListener(ExplosionProtectionService protectionService) {
        this.protectionService = Objects.requireNonNull(protectionService, "protectionService");
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onGhastFireballExplode(EntityExplodeEvent event) {
        if (!(event.getEntity() instanceof Fireball fireball)) {
            return;
        }

        if (!(fireball.getShooter() instanceof Ghast)) {
            return;
        }

        var mode = protectionService.protectExplosion(event.getEntity(), event.getLocation(), event.blockList(), event::setYield);
        if (mode.suppressBlocks()) {
            event.blockList().clear();
            event.setYield(0F);
            event.setCancelled(true);
        }
    }
}
