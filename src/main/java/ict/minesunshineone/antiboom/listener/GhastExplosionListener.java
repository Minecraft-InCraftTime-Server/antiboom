package ict.minesunshineone.antiboom.listener;

import ict.minesunshineone.antiboom.service.ExplosionProtectionService;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Fireball;
import org.bukkit.entity.Ghast;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.persistence.PersistentDataType;

import java.util.Objects;

public final class GhastExplosionListener implements Listener {

    private final ExplosionProtectionService protectionService;
    private final NamespacedKey ghastFireballKey;

    public GhastExplosionListener(ExplosionProtectionService protectionService,
                                  NamespacedKey ghastFireballKey) {
        this.protectionService = Objects.requireNonNull(protectionService, "protectionService");
        this.ghastFireballKey = Objects.requireNonNull(ghastFireballKey, "ghastFireballKey");
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onGhastFireballExplode(EntityExplodeEvent event) {
        if (!(event.getEntity() instanceof Fireball fireball)) {
            return;
        }

        if (!isTrackedGhastFireball(fireball)) {
            return;
        }

        protectionService.protectExplosion(event.getEntity(), event.getLocation(), event.blockList(), event::setYield);
    }

    private boolean isTrackedGhastFireball(Fireball fireball) {
        if (fireball.getPersistentDataContainer().has(ghastFireballKey, PersistentDataType.BYTE)) {
            return true;
        }

        if (fireball.getShooter() instanceof Ghast) {
            fireball.getPersistentDataContainer().set(ghastFireballKey, PersistentDataType.BYTE, (byte) 1);
            return true;
        }

        return false;
    }
}
