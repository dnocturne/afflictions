package com.dnocturne.afflictions;

import com.dnocturne.afflictions.command.CommandManager;
import com.dnocturne.afflictions.config.ConfigManager;
import com.dnocturne.afflictions.hook.HookManager;
import com.dnocturne.afflictions.listener.PlayerListener;
import com.dnocturne.afflictions.listener.TimeListener;
import com.dnocturne.afflictions.locale.LocalizationManager;
import com.dnocturne.afflictions.manager.AfflictionManager;
import com.dnocturne.afflictions.storage.StorageManager;
import com.dnocturne.afflictions.util.TaskUtil;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Main plugin class for Afflictions.
 */
public class Afflictions extends JavaPlugin {

    private static Afflictions instance;

    private ConfigManager configManager;
    private LocalizationManager localizationManager;
    private StorageManager storageManager;
    private AfflictionManager afflictionManager;
    private HookManager hookManager;
    private CommandManager commandManager;

    @Override
    public void onEnable() {
        instance = this;

        // Initialize utilities
        TaskUtil.init(this);

        // Load configuration
        configManager = new ConfigManager(this);
        configManager.load();

        // Load localization
        localizationManager = new LocalizationManager(this);
        localizationManager.load();

        // Initialize storage
        storageManager = new StorageManager(this);
        if (!storageManager.init()) {
            getLogger().severe("Failed to initialize storage! Disabling plugin.");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        // Initialize managers
        afflictionManager = new AfflictionManager(this);
        hookManager = new HookManager(this);

        // Setup hooks
        hookManager.init();

        // Configure tick rate from config
        long tickRate = configManager.getMainConfig().getLong("general.tick-rate", 20L);
        afflictionManager.setTickRate(tickRate);

        // Start affliction tick loop
        afflictionManager.start();

        // Start time listener
        new TimeListener(this).start();

        // Register player listener
        new PlayerListener(this).register();

        // Register commands
        commandManager = new CommandManager(this);
        commandManager.registerCommands();

        // TODO: Register default afflictions

        getLogger().info("Afflictions v" + getPluginMeta().getVersion() + " has been enabled!");
    }

    @Override
    public void onDisable() {
        // Stop tick loop
        if (afflictionManager != null) {
            afflictionManager.stop();
        }

        // Shutdown storage
        if (storageManager != null) {
            storageManager.shutdown();
        }

        // Save configuration
        if (configManager != null) {
            configManager.save();
        }

        getLogger().info("Afflictions has been disabled!");
        instance = null;
    }

    /**
     * Get the plugin instance.
     */
    public static Afflictions getInstance() {
        return instance;
    }

    /**
     * Get the configuration manager.
     */
    public ConfigManager getConfigManager() {
        return configManager;
    }

    /**
     * Get the localization manager.
     */
    public LocalizationManager getLocalizationManager() {
        return localizationManager;
    }

    /**
     * Get the affliction manager.
     */
    public AfflictionManager getAfflictionManager() {
        return afflictionManager;
    }

    /**
     * Get the hook manager.
     */
    public HookManager getHookManager() {
        return hookManager;
    }

    /**
     * Get the storage manager.
     */
    public StorageManager getStorageManager() {
        return storageManager;
    }
}
