package ict.minesunshineone.antiboom.service;

import ict.minesunshineone.antiboom.AntiBoomPlugin;
import ict.minesunshineone.antiboom.ExplosionSettings;
import ict.minesunshineone.antiboom.protection.EntityProtectionRules;
import org.bukkit.entity.EntityType;

import java.util.Objects;

/**
 * Encapsulates Wind Charge specific protection logic so that it can be reused across listeners.
 */
public final class WindChargeProtectionService {

    private final AntiBoomPlugin plugin;

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
}
