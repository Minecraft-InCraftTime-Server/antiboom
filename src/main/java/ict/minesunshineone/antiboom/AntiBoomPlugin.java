package ict.minesunshineone.antiboom;

import ict.minesunshineone.antiboom.listener.CreeperExplosionListener;
import ict.minesunshineone.antiboom.listener.CustomEntityProtectionListener;
import ict.minesunshineone.antiboom.listener.DragonProtectionListener;
import ict.minesunshineone.antiboom.listener.GenericExplosionListener;
import ict.minesunshineone.antiboom.listener.GhastExplosionListener;
import ict.minesunshineone.antiboom.listener.RegionExplosionPrimeListener;
import ict.minesunshineone.antiboom.service.ExplosionProtectionService;
import ict.minesunshineone.antiboom.service.WindChargeProtectionService;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public final class AntiBoomPlugin extends JavaPlugin {

    private ExplosionSettings settings;
    private ExplosionProtectionService explosionProtectionService;
    private WindChargeProtectionService windChargeProtectionService;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        reloadSettings();
        this.explosionProtectionService = new ExplosionProtectionService(this);
        this.windChargeProtectionService = new WindChargeProtectionService(this);
        registerListeners();
        getLogger().info("AntiBoom enabled with Folia-compatible explosion protection.");
    }

    @Override
    public void reloadConfig() {
        super.reloadConfig();
        reloadSettings();
    }

    private void reloadSettings() {
        this.settings = ExplosionSettings.fromConfig(getConfig(), getLogger());
    }

    private void registerListeners() {
        var pluginManager = getServer().getPluginManager();
        pluginManager.registerEvents(new CreeperExplosionListener(explosionProtectionService), this);
        pluginManager.registerEvents(new GhastExplosionListener(explosionProtectionService), this);
        pluginManager.registerEvents(new DragonProtectionListener(explosionProtectionService), this);
        pluginManager.registerEvents(new GenericExplosionListener(explosionProtectionService), this);
        pluginManager.registerEvents(new CustomEntityProtectionListener(explosionProtectionService, windChargeProtectionService), this);
        pluginManager.registerEvents(new RegionExplosionPrimeListener(explosionProtectionService), this);
    }

    public ExplosionSettings getSettings() {
        return settings;
    }

    public ExplosionProtectionService getExplosionProtectionService() {
        return explosionProtectionService;
    }

    public WindChargeProtectionService getWindChargeProtectionService() {
        return windChargeProtectionService;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!command.getName().equalsIgnoreCase("antiboom")) {
            return false;
        }

        if (args.length == 1 && args[0].equalsIgnoreCase("reload")) {
            if (!sender.hasPermission("antiboom.reload")) {
                sender.sendMessage(Component.text("你没有权限执行该命令。", NamedTextColor.RED));
                return true;
            }

            long start = System.currentTimeMillis();
            reloadConfig();
            long cost = System.currentTimeMillis() - start;
            sender.sendMessage(Component.text()
                    .append(Component.text("AntiBoom 配置已重新加载 (", NamedTextColor.GREEN))
                    .append(Component.text(cost, NamedTextColor.GREEN))
                    .append(Component.text("ms)。", NamedTextColor.GREEN))
                    .build());
            return true;
        }

        sender.sendMessage(Component.text("用法: /" + label + " reload", NamedTextColor.YELLOW));
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (!command.getName().equalsIgnoreCase("antiboom")) {
            return Collections.emptyList();
        }

        if (args.length == 1) {
            List<String> suggestions = new ArrayList<>(1);
            if (sender.hasPermission("antiboom.reload")) {
                String prefix = args[0].toLowerCase(Locale.ROOT);
                if (prefix.isEmpty() || "reload".startsWith(prefix)) {
                    suggestions.add("reload");
                }
            }
            return suggestions;
        }

        return Collections.emptyList();
    }
}
