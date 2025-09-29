package ict.minesunshineone.antiboom;

import ict.minesunshineone.antiboom.listener.CreeperExplosionListener;
import ict.minesunshineone.antiboom.listener.CustomEntityProtectionListener;
import ict.minesunshineone.antiboom.listener.DragonProtectionListener;
import ict.minesunshineone.antiboom.listener.GenericExplosionListener;
import ict.minesunshineone.antiboom.listener.GhastExplosionListener;
import ict.minesunshineone.antiboom.service.ExplosionProtectionService;
import ict.minesunshineone.antiboom.service.WindChargeProtectionService;
import org.bukkit.plugin.java.JavaPlugin;

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
}
