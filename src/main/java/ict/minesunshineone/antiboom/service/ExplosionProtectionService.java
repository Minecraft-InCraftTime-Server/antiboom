package ict.minesunshineone.antiboom.service;

import ict.minesunshineone.antiboom.AntiBoomPlugin;
import ict.minesunshineone.antiboom.ExplosionSettings;
import ict.minesunshineone.antiboom.ProtectionMode;
import ict.minesunshineone.antiboom.protection.EntityProtectionRules;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Creeper;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Fireball;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Ghast;
import org.bukkit.entity.Hanging;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.util.BoundingBox;
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
        filterAttachedSupportBlocks(location, affectedBlocks);
        ProtectionMode mode = resolveMode(source, location);
        applyMode(mode, location, affectedBlocks, yieldSetter, source);
    }

    public ProtectionMode resolveMode(Entity source) {
        return resolveMode(source, null);
    }

    public ProtectionMode resolveMode(Entity source, Location location) {
        ExplosionSettings settings = plugin.getSettings();
        if (settings == null) {
            return ProtectionMode.ALLOW;
        }

        ProtectionMode baseMode = settings.resolveMode(source);
        return settings.resolveRegion(location).orElse(baseMode);
    }

    public boolean shouldProtectEntity(Entity entity) {
        if (entity == null) {
            return false;
        }

        if (isExplosionProtectionEnabled() && isExplosionProtectedEntity(entity.getType())) {
            return true;
        }

        return isRegionProtectionActive(entity.getLocation());
    }

    public boolean isRegionProtectionActive(Location location) {
        ExplosionSettings settings = plugin.getSettings();
        if (settings == null || location == null) {
            return false;
        }

        return settings.resolveRegion(location)
                .map(ProtectionMode::suppressBlocks)
                .orElse(false);
    }

    public boolean isExplosionProtectionEnabled() {
        ExplosionSettings settings = plugin.getSettings();
        if (settings == null) {
            return false;
        }

        return settings.explosionEntityRules().isEnabled();
    }

    public boolean isExplosionProtectedEntity(EntityType type) {
        ExplosionSettings settings = plugin.getSettings();
        if (settings == null) {
            return false;
        }

        return settings.explosionEntityRules().isProtected(type);
    }

    private void applyMode(ProtectionMode mode,
                           Location location,
                           List<?> affectedBlocks,
                           Consumer<Float> yieldSetter,
                           Entity source) {
        if (mode == null || !mode.suppressBlocks()) {
            return;
        }

        affectedBlocks.clear();
        yieldSetter.accept(0F);

        if (mode.spawnFirework()) {
            spawnFirework(location, source);
        }
    }

    private void spawnFirework(Location location, Entity source) {
        if (location == null) {
            return;
        }

        World world = location.getWorld();
        if (world == null) {
            return;
        }

        double yOffset = source != null ? source.getHeight() + 0.3 : 1.0;
        Location effectLocation = location.clone().add(new Vector(0.5, yOffset, 0.5));
        World finalWorld = world;

        Bukkit.getRegionScheduler().run(plugin, effectLocation, task -> {
            Firework firework = finalWorld.spawn(effectLocation, Firework.class, fw -> configureFirework(fw, source));
            firework.detonate();
        });
    }

    private void configureFirework(Firework firework, Entity source) {
        FireworkMeta meta = firework.getFireworkMeta();
        FireworkEffect effect = createFireworkEffect(source);
        meta.clearEffects();
        meta.addEffect(effect);
        meta.setPower(0);
        firework.setFireworkMeta(meta);
        firework.setTicksLived(2);
    }

    private FireworkEffect createFireworkEffect(Entity source) {
        FireworkEffect.Builder builder = FireworkEffect.builder()
                .trail(false)
                .flicker(false)
                .withFade(Color.fromRGB(120, 120, 120));

        if (source instanceof Creeper) {
            builder.with(FireworkEffect.Type.CREEPER)
                    .withColor(Color.fromRGB(84, 255, 84));
        } else if (source instanceof Fireball fireball && fireball.getShooter() instanceof Ghast) {
            builder.with(FireworkEffect.Type.BALL)
                    .withColor(Color.fromRGB(240, 32, 32));
        } else {
            builder.with(FireworkEffect.Type.BALL)
                    .withColor(Color.fromRGB(255, 240, 200));
        }

        return builder.build();
    }

    private void filterAttachedSupportBlocks(Location location, List<?> affectedBlocks) {
        if (affectedBlocks == null || affectedBlocks.isEmpty()) {
            return;
        }

        ExplosionSettings settings = plugin.getSettings();
        if (settings == null) {
            return;
        }

        EntityProtectionRules rules = settings.explosionEntityRules();
        if (!rules.isEnabled()) {
            return;
        }

        if (!settings.protectSupportBlocks()) {
            return;
        }

        @SuppressWarnings("unchecked")
        List<Block> blocks = (List<Block>) affectedBlocks;

        World world = location != null ? location.getWorld() : null;
        if (world == null && !blocks.isEmpty()) {
            world = blocks.get(0).getWorld();
        }

        if (world == null) {
            return;
        }

        blocks.removeIf(block -> supportsProtectedHanging(block, rules));
    }

    private boolean supportsProtectedHanging(Block block, EntityProtectionRules rules) {
        BoundingBox box = BoundingBox.of(block).expand(1.0);
        return block.getWorld().getNearbyEntities(box, entity -> entity instanceof Hanging hanging && isProtectedHanging(hanging, rules))
                .stream()
                .map(Hanging.class::cast)
                .anyMatch(hanging -> {
                    BlockFace face = hanging.getAttachedFace();
                    if (face == null) {
                        return false;
                    }
                    Block attached = hanging.getLocation().getBlock().getRelative(face);
                    return attached.equals(block);
                });
    }

    private boolean isProtectedHanging(Hanging hanging, EntityProtectionRules rules) {
        EntityType type = hanging.getType();
        return rules.isProtected(type);
    }
}
