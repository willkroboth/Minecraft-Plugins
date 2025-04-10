package me.willkroboth.platformspawner;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Map;
import java.util.logging.Logger;

public final class PlatformSpawner extends JavaPlugin {
    private boolean loaded = false;
    private Logger logger;
    private PlatformManager platformManager;

    @Override
    public void onLoad() {
        saveDefaultConfig();
        logger = getLogger();

        FileConfiguration config = getConfig();
        ConfigurationSection shapesConfig = config.getConfigurationSection("shapes");

        Map<String, Shape> shapes;
        if(shapesConfig == null || (
                shapes = Shape.loadShapes(this, shapesConfig)
        ).size() == 0) {
            logError("No shapes configured. Disabling PlatformSpawner.");
            this.getServer().getPluginManager().disablePlugin(this);
            return;
        }

        platformManager = new PlatformManager(this, shapes);
        platformManager.registerCommands();

        loaded = true;
    }

    @Override
    public void onDisable() {
        // If no shapes were configured, we may disable without initializing platformManager
        if (!loaded) return;

        platformManager.clearPlatforms();
        loaded = false;
    }

    public void logError(String... messages) {
        for (String message : messages) {
            logger.severe(message);
        }
    }

    public void logWarning(String... messages) {
        for (String message : messages) {
            logger.warning(message);
        }
    }
}
