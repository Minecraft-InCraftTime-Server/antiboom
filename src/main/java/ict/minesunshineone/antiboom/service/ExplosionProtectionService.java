package ict.minesunshineone.antiboom.service;

import ict.minesunshineone.antiboom.AntiBoomPlugin;
import ict.minesunshineone.antiboom.ExplosionSettings;
import ict.minesunshineone.antiboom.ProtectionMode;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Firework;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.util.Vector;

import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

public final class ExplosionProtectionService {

    private final AntiBoomPlugin plugin;

    public ExplosionProtectionService(AntiBoomPlugin plugin) {
        this.plugin = Objects.requireNonNull(plugin, "plugin");
    }

    public void protectExplosion(Entity source,
                                 Location location,
                                 List<?> affectedBlocks,
                                 Consumer<Float> yieldSetter) {
        ProtectionMode mode = resolveMode(source);
        applyMode(mode, location, affectedBlocks, yieldSetter);
    }

    public ProtectionMode resolveMode(Entity source) {
        ExplosionSettings settings = plugin.getSettings();
        if (settings == null) {
            return ProtectionMode.ALLOW;
        }

        return settings.resolveMode(source);
    }

    public boolean isCustomProtectionEnabled() {
        ExplosionSettings settings = plugin.getSettings();
        return settings != null && settings.isCustomProtectionEnabled();
    }

    public boolean isProtectedEntity(EntityType type) {
        ExplosionSettings settings = plugin.getSettings();
        if (settings == null) {
            return false;
        }

        return settings.isEntityProtected(type);
    }

    private void applyMode(ProtectionMode mode,
                           Location location,
                           List<?> affectedBlocks,
                           Consumer<Float> yieldSetter) {
        if (mode == null || !mode.suppressBlocks()) {
            return;
        }

        affectedBlocks.clear();
        yieldSetter.accept(0F);

        if (mode.spawnFirework()) {
            spawnFirework(location);
        }
    }

    private void spawnFirework(Location location) {
        if (location == null) {
            return;
        }

        World world = location.getWorld();
        if (world == null) {
            return;
        }

        Location centered = location.clone().add(new Vector(0.5, 0.0, 0.5));
        World finalWorld = world;

        Bukkit.getRegionScheduler().run(plugin, centered, task -> {
            Firework firework = finalWorld.spawn(centered, Firework.class, this::configureFirework);
            firework.detonate();
        });
    }

    private void configureFirework(Firework firework) {
        FireworkMeta meta = firework.getFireworkMeta();
        FireworkEffect effect = FireworkEffect.builder()
                .with(FireworkEffect.Type.BALL_LARGE)
                .withColor(Color.ORANGE, Color.YELLOW)
                .withFade(Color.WHITE)
                .flicker(true)
                .trail(true)
                .build();
        meta.clearEffects();
        meta.addEffect(effect);
        meta.setPower(0);
        firework.setFireworkMeta(meta);
        firework.setTicksLived(2);
    }
}
