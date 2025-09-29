package ict.minesunshineone.antiboom.service;

import ict.minesunshineone.antiboom.AntiBoomPlugin;
import ict.minesunshineone.antiboom.ExplosionSettings;
import ict.minesunshineone.antiboom.protection.EntityProtectionRules;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;

import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.TimeUnit;
import java.util.Deque;

/**
 * Encapsulates Wind Charge specific protection logic so that it can be reused across listeners.
 */
public final class WindChargeProtectionService {

    private final AntiBoomPlugin plugin;
    private final Deque<WindChargeImpact> recentImpacts = new ConcurrentLinkedDeque<>();

    private static final long IMPACT_TTL_NANOS = TimeUnit.MILLISECONDS.toNanos(400);
    private static final double IMPACT_RADIUS_SQUARED = 4.0D;
    private static final int IMPACT_HISTORY_LIMIT = 64;

    public WindChargeProtectionService(AntiBoomPlugin plugin) {
        this.plugin = Objects.requireNonNull(plugin, "plugin");
    }

    public boolean isProtectionEnabled() {
        ExplosionSettings settings = plugin.getSettings();
        if (settings == null) {
            return false;
        }

        return settings.windChargeEntityRules().isEnabled();
    }

    public boolean isProtectedEntity(EntityType type) {
        ExplosionSettings settings = plugin.getSettings();
        if (settings == null) {
            return false;
        }

        EntityProtectionRules rules = settings.windChargeEntityRules();
        return rules.isProtected(type);
    }

    public void recordImpact(Location location) {
        if (location == null || location.getWorld() == null) {
            return;
        }

        long now = System.nanoTime();
        purgeExpired(now);

        if (recentImpacts.size() >= IMPACT_HISTORY_LIMIT) {
            recentImpacts.pollFirst();
        }

        recentImpacts.addLast(new WindChargeImpact(
                location.getWorld().getUID(),
                location.getX(),
                location.getY(),
                location.getZ(),
                now
        ));
    }

    public boolean wasRecentImpact(Location location) {
        if (location == null || location.getWorld() == null) {
            return false;
        }

        long now = System.nanoTime();
        purgeExpired(now);

        if (recentImpacts.isEmpty()) {
            return false;
        }

        UUID worldId = location.getWorld().getUID();
        double x = location.getX();
        double y = location.getY();
        double z = location.getZ();

        for (WindChargeImpact impact : recentImpacts) {
            if (!impact.worldId.equals(worldId)) {
                continue;
            }

            double dx = impact.x - x;
            double dy = impact.y - y;
            double dz = impact.z - z;
            if (dx * dx + dy * dy + dz * dz <= IMPACT_RADIUS_SQUARED) {
                return true;
            }
        }

        return false;
    }

    private void purgeExpired(long now) {
        while (!recentImpacts.isEmpty()) {
            WindChargeImpact impact = recentImpacts.peekFirst();
            if (impact == null) {
                break;
            }
            if (now - impact.timestampNanos > IMPACT_TTL_NANOS) {
                recentImpacts.pollFirst();
            } else {
                break;
            }
        }
    }

    private static final class WindChargeImpact {
        private final UUID worldId;
        private final double x;
        private final double y;
        private final double z;
        private final long timestampNanos;

        private WindChargeImpact(UUID worldId, double x, double y, double z, long timestampNanos) {
            this.worldId = worldId;
            this.x = x;
            this.y = y;
            this.z = z;
            this.timestampNanos = timestampNanos;
        }
    }
}
