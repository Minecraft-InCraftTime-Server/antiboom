package ict.minesunshineone.antiboom.listener;

import org.bukkit.NamespacedKey;
import org.bukkit.entity.Fireball;
import org.bukkit.entity.Ghast;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.persistence.PersistentDataType;

import java.util.Objects;

public final class GhastFireballTrackingListener implements Listener {

    private final NamespacedKey ghastFireballKey;

    public GhastFireballTrackingListener(NamespacedKey ghastFireballKey) {
        this.ghastFireballKey = Objects.requireNonNull(ghastFireballKey, "ghastFireballKey");
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onProjectileLaunch(ProjectileLaunchEvent event) {
        if (!(event.getEntity() instanceof Fireball fireball)) {
            return;
        }

        if (!(fireball.getShooter() instanceof Ghast)) {
            return;
        }

        fireball.getPersistentDataContainer().set(ghastFireballKey, PersistentDataType.BYTE, (byte) 1);
    }
}
