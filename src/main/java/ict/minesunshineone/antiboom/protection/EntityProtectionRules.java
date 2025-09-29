package ict.minesunshineone.antiboom.protection;

import org.bukkit.entity.EntityType;

import java.util.Collections;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

/**
 * Immutable representation of per-entity protection flags that can be reused across different damage sources.
 */
public final class EntityProtectionRules {

    private final boolean enabled;
    private final Map<EntityType, Boolean> overrides;
    private final Set<EntityType> defaults;

    public EntityProtectionRules(boolean enabled,
                                 Map<EntityType, Boolean> overrides,
                                 Set<EntityType> defaults) {
        this.enabled = enabled;
        this.overrides = Collections.unmodifiableMap(new EnumMap<>(overrides));
        this.defaults = Collections.unmodifiableSet(defaults.isEmpty()
                ? EnumSet.noneOf(EntityType.class)
                : EnumSet.copyOf(defaults));
    }

    public boolean isEnabled() {
        return enabled;
    }

    public boolean isProtected(EntityType type) {
        if (!enabled) {
            return false;
        }

        if (!overrides.isEmpty()) {
            return overrides.getOrDefault(type, Boolean.FALSE);
        }

        return defaults.contains(type);
    }

    public Map<EntityType, Boolean> overrides() {
        return overrides;
    }

    public Set<EntityType> defaultEntities() {
        return defaults;
    }
}
