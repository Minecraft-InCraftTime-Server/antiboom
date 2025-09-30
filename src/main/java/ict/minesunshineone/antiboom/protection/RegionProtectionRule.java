package ict.minesunshineone.antiboom.protection;

import ict.minesunshineone.antiboom.ProtectionMode;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.EntityType;

import java.util.Objects;
import java.util.Set;
import java.util.Collections;
import java.util.EnumSet;

/**
 * 定义一个以世界和 XYZ 边界描述的爆炸保护区域。
 */
public final class RegionProtectionRule {

    private final String worldName;
    private final int minX;
    private final int maxX;
    private final int minY;
    private final int maxY;
    private final int minZ;
    private final int maxZ;
    private final ProtectionMode mode;
    private final Set<EntityType> protectedEntities;

    public RegionProtectionRule(String worldName,
                                int minX,
                                int minY,
                                int minZ,
                                int maxX,
                                int maxY,
                                int maxZ,
                                ProtectionMode mode,
                                Set<EntityType> protectedEntities) {
        this.worldName = Objects.requireNonNull(worldName, "worldName");
        this.mode = Objects.requireNonNull(mode, "mode");

        this.minX = Math.min(minX, maxX);
        this.maxX = Math.max(minX, maxX);
        this.minY = Math.min(minY, maxY);
        this.maxY = Math.max(minY, maxY);
        this.minZ = Math.min(minZ, maxZ);
        this.maxZ = Math.max(minZ, maxZ);

        if (protectedEntities == null || protectedEntities.isEmpty()) {
            this.protectedEntities = Set.of();
        } else {
            this.protectedEntities = Collections.unmodifiableSet(EnumSet.copyOf(protectedEntities));
        }
    }

    public ProtectionMode mode() {
        return mode;
    }

    public boolean protectsEntity(EntityType type) {
        if (type == null) {
            return false;
        }
        return protectedEntities.contains(type);
    }

    public Set<EntityType> protectedEntities() {
        return protectedEntities;
    }

    public boolean contains(Location location) {
        if (location == null) {
            return false;
        }

        World world = location.getWorld();
        if (world == null) {
            return false;
        }

        if (!world.getName().equalsIgnoreCase(worldName)) {
            return false;
        }

        int x = location.getBlockX();
        int y = location.getBlockY();
        int z = location.getBlockZ();

        return x >= minX && x <= maxX
                && y >= minY && y <= maxY
                && z >= minZ && z <= maxZ;
    }

    @Override
    public String toString() {
        return "RegionProtectionRule{" +
                "worldName='" + worldName + '\'' +
                ", minX=" + minX +
                ", minY=" + minY +
                ", minZ=" + minZ +
                ", maxX=" + maxX +
                ", maxY=" + maxY +
                ", maxZ=" + maxZ +
                ", mode=" + mode +
                ", protectedEntities=" + protectedEntities +
                '}';
    }
}
