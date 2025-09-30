package ict.minesunshineone.antiboom.service;

import ict.minesunshineone.antiboom.AntiBoomPlugin;
import ict.minesunshineone.antiboom.ExplosionSettings;
import ict.minesunshineone.antiboom.protection.EntityProtectionRules;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.ItemFrame;
import org.bukkit.inventory.ItemStack;

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
        if (type == null) {
            return false;
        }

        ExplosionSettings settings = plugin.getSettings();
        if (settings == null) {
            return false;
        }

        EntityProtectionRules rules = settings.windChargeEntityRules();
        return rules.isProtected(type);
    }

    public boolean isProtectedEntity(Entity entity) {
        if (entity == null) {
            return false;
        }

        ExplosionSettings settings = plugin.getSettings();
        if (settings == null) {
            return false;
        }

        EntityProtectionRules rules = settings.windChargeEntityRules();
        EntityType type = entity.getType();
        if (!rules.isProtected(type)) {
            return false;
        }

        if (entity instanceof ItemFrame frame) {
            ItemStack item = frame.getItem();
            return item != null && !item.getType().isAir();
        }

        return true;
    }
}
