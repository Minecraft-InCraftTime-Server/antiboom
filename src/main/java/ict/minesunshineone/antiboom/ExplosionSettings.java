package ict.minesunshineone.antiboom;

import ict.minesunshineone.antiboom.protection.EntityProtectionRules;
import ict.minesunshineone.antiboom.protection.RegionProtectionRule;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Creeper;
import org.bukkit.entity.EnderDragon;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Fireball;
import org.bukkit.entity.Ghast;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.List;
import java.util.logging.Logger;

public final class ExplosionSettings {

    private static final Set<EntityType> DEFAULT_PROTECTED_ENTITIES = EnumSet.of(
            EntityType.PAINTING,
            EntityType.ITEM_FRAME,
            EntityType.GLOW_ITEM_FRAME,
            EntityType.ARMOR_STAND,
            EntityType.BOAT,
            EntityType.CHEST_BOAT,
            EntityType.LEASH_KNOT
    );

    private static final Map<String, EntityType> ENTITY_ALIASES = createEntityAliases();

    private final Map<ExplosionSource, ProtectionMode> explosionModes;
    private final EntityProtectionRules explosionProtection;
    private final EntityProtectionRules windChargeProtection;
    private final boolean protectSupportBlocks;
    private final List<RegionProtectionRule> regionProtections;
    private final Map<String, List<RegionProtectionRule>> regionProtectionsByWorld;

    private ExplosionSettings(Map<ExplosionSource, ProtectionMode> explosionModes,
                              EntityProtectionRules explosionProtection,
                              EntityProtectionRules windChargeProtection,
                              boolean protectSupportBlocks,
                              List<RegionProtectionRule> regionProtections) {
        this.explosionModes = new EnumMap<>(explosionModes);
        this.explosionProtection = explosionProtection;
        this.windChargeProtection = windChargeProtection;
        this.protectSupportBlocks = protectSupportBlocks;
        this.regionProtections = List.copyOf(regionProtections);
        this.regionProtectionsByWorld = Map.copyOf(groupRegionsByWorld(regionProtections));
    }

    public static ExplosionSettings fromConfig(FileConfiguration config, Logger logger) {
        Map<ExplosionSource, ProtectionMode> explosionModes = loadExplosionModes(config, logger);
        boolean explosionEnabled = config.getBoolean("explosion-protection.enabled", true);
        Map<EntityType, Boolean> explosionProtection = loadEntityProtection(config.getConfigurationSection("explosion-protection.entities"));
        boolean protectSupportBlocks = config.getBoolean("explosion-protection.protect-support-blocks", true);
        List<RegionProtectionRule> regionProtections = loadRegionProtections(config, logger);

        boolean windEnabled = config.getBoolean("wind-charge-protection.enabled", true);
        Map<EntityType, Boolean> windProtection = loadEntityProtection(config.getConfigurationSection("wind-charge-protection.entities"));

        EntityProtectionRules explosionRules = new EntityProtectionRules(explosionEnabled, explosionProtection, DEFAULT_PROTECTED_ENTITIES);
        EntityProtectionRules windRules = new EntityProtectionRules(windEnabled, windProtection, DEFAULT_PROTECTED_ENTITIES);

        return new ExplosionSettings(explosionModes, explosionRules, windRules, protectSupportBlocks, regionProtections);
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

    public boolean protectSupportBlocks() {
        return protectSupportBlocks;
    }

    public Optional<ProtectionMode> resolveRegion(Location location) {
        if (regionProtections.isEmpty() || location == null) {
            return Optional.empty();
        }

        String worldName = Optional.ofNullable(location.getWorld())
                .map(world -> world.getName().toLowerCase(Locale.ROOT))
                .orElse(null);
        if (worldName == null) {
            return Optional.empty();
        }

        List<RegionProtectionRule> regions = regionProtectionsByWorld.get(worldName);
        if (regions == null || regions.isEmpty()) {
            return Optional.empty();
        }

        return regions.stream()
                .filter(region -> region.contains(location))
                .map(RegionProtectionRule::mode)
                .findFirst();
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

    private static List<RegionProtectionRule> loadRegionProtections(FileConfiguration config, Logger logger) {
        List<Map<?, ?>> entries = config.getMapList("explosion-protection.xyz-regions");
        if (entries.isEmpty()) {
            return List.of();
        }

        List<RegionProtectionRule> regions = new ArrayList<>();
        int index = 0;
        for (Map<?, ?> raw : entries) {
            String path = "explosion-protection.xyz-regions[" + index + "]";
            index++;
            if (raw == null) {
                continue;
            }

            String world = asString(raw.get("world"));
            if (world == null || world.isBlank()) {
                log(logger, "Skipped " + path + " because 'world' is missing or blank.");
                continue;
            }

            Map<?, ?> min = asMap(raw.get("min"));
            Map<?, ?> max = asMap(raw.get("max"));
            if (min == null || max == null) {
                log(logger, "Skipped " + path + " because 'min' or 'max' is missing.");
                continue;
            }

            Integer minX = asInt(min.get("x"));
            Integer minY = asInt(min.get("y"));
            Integer minZ = asInt(min.get("z"));
            Integer maxX = asInt(max.get("x"));
            Integer maxY = asInt(max.get("y"));
            Integer maxZ = asInt(max.get("z"));

            if (minX == null || minY == null || minZ == null || maxX == null || maxY == null || maxZ == null) {
                log(logger, "Skipped " + path + " because some coordinates are missing or invalid.");
                continue;
            }

            String modeValue = asString(raw.get("mode"));
            ProtectionMode mode = ProtectionMode.fromConfigValue(modeValue, ProtectionMode.PROTECT, logger, path + ".mode");

            regions.add(new RegionProtectionRule(world.trim(), minX, minY, minZ, maxX, maxY, maxZ, mode));
        }

        return List.copyOf(regions);
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
                "BAMBOO_RAFT");

        registerAliases(aliases, EntityType.CHEST_BOAT,
                "OAK_CHEST_BOAT",
                "SPRUCE_CHEST_BOAT",
                "BIRCH_CHEST_BOAT",
                "JUNGLE_CHEST_BOAT",
                "ACACIA_CHEST_BOAT",
                "DARK_OAK_CHEST_BOAT",
                "MANGROVE_CHEST_BOAT",
                "CHERRY_CHEST_BOAT");

        return Map.copyOf(aliases);
    }

    private static void registerAliases(Map<String, EntityType> aliases, EntityType type, String... names) {
        for (String alias : names) {
            aliases.put(normalizeKey(alias), type);
        }
    }

    private static Map<String, List<RegionProtectionRule>> groupRegionsByWorld(List<RegionProtectionRule> regions) {
        if (regions.isEmpty()) {
            return Map.of();
        }

        Map<String, List<RegionProtectionRule>> grouped = new HashMap<>();
        for (RegionProtectionRule region : regions) {
            String world = region.worldName().toLowerCase(Locale.ROOT);
            grouped.computeIfAbsent(world, key -> new ArrayList<>()).add(region);
        }

        Map<String, List<RegionProtectionRule>> immutable = new HashMap<>();
        for (Map.Entry<String, List<RegionProtectionRule>> entry : grouped.entrySet()) {
            immutable.put(entry.getKey(), List.copyOf(entry.getValue()));
        }

        return immutable;
    }

    private static String asString(Object value) {
        if (value instanceof String string) {
            return string;
        }
        return null;
    }

    private static Map<?, ?> asMap(Object value) {
        if (value instanceof Map<?, ?> map) {
            return map;
        }
        return null;
    }

    private static Integer asInt(Object value) {
        if (value instanceof Number number) {
            return number.intValue();
        }
        if (value instanceof String string) {
            try {
                return Integer.parseInt(string.trim());
            } catch (NumberFormatException ignored) {
                return null;
            }
        }
        return null;
    }

    private static void log(Logger logger, String message) {
        if (logger != null) {
            logger.warning(message);
        }
    }

    private enum ExplosionSource {
        CREEPER,
        GHAST_FIREBALL,
        ENDER_DRAGON
    }
}
