package ict.minesunshineone.antiboom;

import java.util.Locale;
import java.util.logging.Logger;

public enum ProtectionMode {
    ALLOW(false, false),
    PROTECT(true, false),
    FIREWORK(true, true);

    private final boolean suppressBlocks;
    private final boolean spawnFirework;

    ProtectionMode(boolean suppressBlocks, boolean spawnFirework) {
        this.suppressBlocks = suppressBlocks;
        this.spawnFirework = spawnFirework;
    }

    public boolean suppressBlocks() {
        return suppressBlocks;
    }

    public boolean spawnFirework() {
        return spawnFirework;
    }

    public static ProtectionMode fromConfigValue(String value,
                                                 ProtectionMode fallback,
                                                 Logger logger,
                                                 String path) {
        if (value == null) {
            return fallback;
        }

        String normalized = value.trim().toLowerCase(Locale.ROOT);
        switch (normalized) {
            case "true", "protect", "block" -> {
                return PROTECT;
            }
            case "false", "allow", "off" -> {
                return ALLOW;
            }
            case "firework" -> {
                return FIREWORK;
            }
            default -> {
                if (logger != null) {
                    logger.warning("Unknown protection mode '" + value + "' at " + path + ", falling back to " + fallback.name());
                }
                return fallback;
            }
        }
    }
}
