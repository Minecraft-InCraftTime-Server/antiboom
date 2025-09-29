package ict.minesunshineone.antiboom;

import ict.minesunshineone.antiboom.protection.EntityProtectionRules;
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
import java.util.HashMap;
import java.util.Locale;
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

    private static final Map<String, EntityType> ENTITY_ALIASES = createEntityAliases();

    private final Map<ExplosionSource, ProtectionMode> explosionModes;
    private final EntityProtectionRules explosionProtection;
    private final EntityProtectionRules windChargeProtection;

    private ExplosionSettings(Map<ExplosionSource, ProtectionMode> explosionModes,
                              EntityProtectionRules explosionProtection,
                              EntityProtectionRules windChargeProtection) {
        this.explosionModes = new EnumMap<>(explosionModes);
        this.explosionProtection = explosionProtection;
        this.windChargeProtection = windChargeProtection;
    }

    public static ExplosionSettings fromConfig(FileConfiguration config, Logger logger) {
        Map<ExplosionSource, ProtectionMode> explosionModes = loadExplosionModes(config, logger);
        boolean explosionEnabled = readEnabledFlag(config, "explosion-protection.enabled", "custom-protection.enabled");
        Map<EntityType, Boolean> explosionProtection = loadEntityProtection(resolveSection(config, "explosion-protection.entities", "custom-protection.entities"));

        boolean windEnabled = config.getBoolean("wind-charge-protection.enabled", true);
        Map<EntityType, Boolean> windProtection = loadEntityProtection(config.getConfigurationSection("wind-charge-protection.entities"));

        EntityProtectionRules explosionRules = new EntityProtectionRules(explosionEnabled, explosionProtection, DEFAULT_PROTECTED_ENTITIES);
        EntityProtectionRules windRules = new EntityProtectionRules(windEnabled, windProtection, DEFAULT_PROTECTED_ENTITIES);

        return new ExplosionSettings(explosionModes, explosionRules, windRules);
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

    public Set<EntityType> defaultProtectedEntities() {
        return EnumSet.copyOf(DEFAULT_PROTECTED_ENTITIES);
    }

    public EntityProtectionRules explosionEntityRules() {
        return explosionProtection;
    }

    public EntityProtectionRules windChargeEntityRules() {
        return windChargeProtection;
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
            EntityType type = resolveEntityType(key);
            if (type != null) {
                values.put(type, section.getBoolean(key));
            }
        }

        return values;
    }

    private static boolean readEnabledFlag(FileConfiguration config, String primary, String fallback) {
        if (config.contains(primary)) {
            return config.getBoolean(primary);
        }
        return config.getBoolean(fallback, true);
    }

    private static ConfigurationSection resolveSection(FileConfiguration config, String primary, String fallback) {
        ConfigurationSection section = config.getConfigurationSection(primary);
        if (section != null) {
            return section;
        }
        return config.getConfigurationSection(fallback);
    }

    private static EntityType resolveEntityType(String key) {
        String normalized = normalizeKey(key);
        try {
            return EntityType.valueOf(normalized);
        } catch (IllegalArgumentException ignored) {
            return ENTITY_ALIASES.get(normalized);
        }
    }

    private static String normalizeKey(String key) {
        return key.trim()
                .toUpperCase(Locale.ROOT)
                .replace('-', '_')
                .replace(' ', '_');
    }

    private static Map<String, EntityType> createEntityAliases() {
        Map<String, EntityType> aliases = new HashMap<>();

        registerAliases(aliases, EntityType.BOAT,
                "OAK_BOAT",
                "SPRUCE_BOAT",
                "BIRCH_BOAT",
                "JUNGLE_BOAT",
                "ACACIA_BOAT",
                "DARK_OAK_BOAT",
                "MANGROVE_BOAT",
                "CHERRY_BOAT",
                "BAMBOO_RAFT",
                "BAMBOO_BOAT",
                "RAFT");

        registerAliases(aliases, EntityType.CHEST_BOAT,
                "OAK_CHEST_BOAT",
                "SPRUCE_CHEST_BOAT",
                "BIRCH_CHEST_BOAT",
                "JUNGLE_CHEST_BOAT",
                "ACACIA_CHEST_BOAT",
                "DARK_OAK_CHEST_BOAT",
                "MANGROVE_CHEST_BOAT",
                "CHERRY_CHEST_BOAT",
                "BAMBOO_RAFT_WITH_CHEST",
                "BAMBOO_CHEST_BOAT",
                "RAFT_WITH_CHEST");

        return Map.copyOf(aliases);
    }

    private static void registerAliases(Map<String, EntityType> aliases, EntityType type, String... names) {
        for (String alias : names) {
            aliases.put(normalizeKey(alias), type);
        }
    }

    private enum ExplosionSource {
        CREEPER,
        GHAST_FIREBALL,
        ENDER_DRAGON
    }
}
