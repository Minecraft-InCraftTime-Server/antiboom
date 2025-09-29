package ict.minesunshineone.antiboom;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Creeper;
import org.bukkit.entity.EnderDragon;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Fireball;
import org.bukkit.entity.Ghast;

import java.util.Collections;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.logging.Logger;

public final class ExplosionSettings {

    private static final Set<EntityType> DEFAULT_PROTECTED_ENTITIES = EnumSet.of(
            EntityType.PAINTING,
            EntityType.ITEM_FRAME,
            EntityType.GLOW_ITEM_FRAME,
            EntityType.ARMOR_STAND,
            EntityType.BOAT,
            EntityType.LEASH_KNOT
    );

    private final Map<ExplosionSource, ProtectionMode> explosionModes;
    private final boolean customEntityProtectionEnabled;
    private final Map<EntityType, Boolean> entityProtections;

    private ExplosionSettings(Map<ExplosionSource, ProtectionMode> explosionModes,
                              boolean customEntityProtectionEnabled,
                              Map<EntityType, Boolean> entityProtections) {
        this.explosionModes = new EnumMap<>(explosionModes);
        this.customEntityProtectionEnabled = customEntityProtectionEnabled;
        this.entityProtections = entityProtections;
    }

    public static ExplosionSettings fromConfig(FileConfiguration config, Logger logger) {
        Map<ExplosionSource, ProtectionMode> explosionModes = loadExplosionModes(config, logger);
        boolean enabled = config.getBoolean("custom-protection.enabled", true);
        Map<EntityType, Boolean> entityProtection = loadEntityProtection(config.getConfigurationSection("custom-protection.entities"));

        return new ExplosionSettings(explosionModes, enabled, entityProtection);
    }

    public ProtectionMode resolveMode(Entity source) {
        if (source instanceof EnderDragon) {
            return explosionModes.getOrDefault(ExplosionSource.ENDER_DRAGON, ProtectionMode.PROTECT);
        }

        return Optional.ofNullable(source)
                .map(this::resolveOverride)
                .orElse(ProtectionMode.ALLOW);
    }

    private ProtectionMode resolveOverride(Entity entity) {
        if (entity instanceof Creeper) {
            return explosionModes.getOrDefault(ExplosionSource.CREEPER, ProtectionMode.ALLOW);
        }

        if (entity instanceof Fireball fireball) {
            if (fireball.getShooter() instanceof Ghast) {
                return explosionModes.getOrDefault(ExplosionSource.GHAST_FIREBALL, ProtectionMode.ALLOW);
            }
        }

        return ProtectionMode.ALLOW;
    }

    public boolean isCustomProtectionEnabled() {
        return customEntityProtectionEnabled;
    }

    public boolean isEntityProtected(EntityType type) {
        if (!customEntityProtectionEnabled) {
            return false;
        }

        if (!entityProtections.isEmpty()) {
            return entityProtections.getOrDefault(type, Boolean.FALSE);
        }

        return DEFAULT_PROTECTED_ENTITIES.contains(type);
    }

    public Set<EntityType> defaultProtectedEntities() {
        return EnumSet.copyOf(DEFAULT_PROTECTED_ENTITIES);
    }

    private static Map<ExplosionSource, ProtectionMode> loadExplosionModes(FileConfiguration config, Logger logger) {
        Map<ExplosionSource, ProtectionMode> result = new EnumMap<>(ExplosionSource.class);

        result.put(ExplosionSource.CREEPER,
                ProtectionMode.fromConfigValue(
                        config.getString("explosions.creeper"),
                        ProtectionMode.ALLOW,
                        logger,
                        "explosions.creeper"));

        result.put(ExplosionSource.GHAST_FIREBALL,
                ProtectionMode.fromConfigValue(
                        config.getString("explosions.ghast-fireball"),
                        ProtectionMode.ALLOW,
                        logger,
                        "explosions.ghast-fireball"));

        result.put(ExplosionSource.ENDER_DRAGON,
                ProtectionMode.fromConfigValue(
                        config.getString("explosions.ender-dragon"),
                        ProtectionMode.PROTECT,
                        logger,
                        "explosions.ender-dragon"));

        return result;
    }

    private static Map<EntityType, Boolean> loadEntityProtection(ConfigurationSection section) {
        if (section == null) {
            return Collections.emptyMap();
        }

        Map<EntityType, Boolean> values = new EnumMap<>(EntityType.class);
        for (String key : section.getKeys(false)) {
            try {
                EntityType type = EntityType.valueOf(key.toUpperCase());
                values.put(type, section.getBoolean(key));
            } catch (IllegalArgumentException ignored) {
                // Unknown entity type, skip silently.
            }
        }

        return values;
    }

    private enum ExplosionSource {
        CREEPER,
        GHAST_FIREBALL,
        ENDER_DRAGON
    }
}
