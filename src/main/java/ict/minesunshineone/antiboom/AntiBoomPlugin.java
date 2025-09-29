package ict.minesunshineone.antiboom;

import ict.minesunshineone.antiboom.listener.CreeperExplosionListener;
import ict.minesunshineone.antiboom.listener.CustomEntityProtectionListener;
import ict.minesunshineone.antiboom.listener.DragonProtectionListener;
import ict.minesunshineone.antiboom.listener.GenericExplosionListener;
import ict.minesunshineone.antiboom.listener.GhastExplosionListener;
import ict.minesunshineone.antiboom.service.ExplosionProtectionService;
import org.bukkit.plugin.java.JavaPlugin;

public final class AntiBoomPlugin extends JavaPlugin {

    private ExplosionSettings settings;
    private ExplosionProtectionService protectionService;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        reloadSettings();
        this.protectionService = new ExplosionProtectionService(this);
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
        pluginManager.registerEvents(new CreeperExplosionListener(protectionService), this);
        pluginManager.registerEvents(new GhastExplosionListener(protectionService), this);
        pluginManager.registerEvents(new DragonProtectionListener(protectionService), this);
        pluginManager.registerEvents(new GenericExplosionListener(protectionService), this);
        pluginManager.registerEvents(new CustomEntityProtectionListener(protectionService), this);
    }

    public ExplosionSettings getSettings() {
        return settings;
    }

    public ExplosionProtectionService getProtectionService() {
        return protectionService;
    }
}
