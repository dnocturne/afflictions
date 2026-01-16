package com.dnocturne.afflictions;

import com.dnocturne.afflictions.affliction.config.AfflictionDisplayConfig;
import com.dnocturne.afflictions.affliction.config.VampirismConfig;
import com.dnocturne.afflictions.affliction.impl.Vampirism;
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
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

/**
 * Main plugin class for Afflictions.
 */
public class Afflictions extends JavaPlugin {

    private static @Nullable Afflictions instance;

    private @Nullable ConfigManager configManager;
    private @Nullable LocalizationManager localizationManager;
    private @Nullable StorageManager storageManager;
    private @Nullable AfflictionManager afflictionManager;
    private @Nullable HookManager hookManager;
    private @Nullable CommandManager commandManager;
    private @Nullable TimeListener timeListener;
    private @Nullable VampirismConfig vampirismConfig;

    // Registry of affliction display configs by ID
    private final Map<String, AfflictionDisplayConfig> displayConfigs = new HashMap<>();

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
        timeListener = new TimeListener(this);
        timeListener.start();

        // Register player listener
        new PlayerListener(this).register();

        // Register commands
        commandManager = new CommandManager(this);
        commandManager.registerCommands();

        // Register default afflictions
        registerAfflictions();

        getLogger().info("Afflictions v" + getPluginMeta().getVersion() + " has been enabled!");
    }

    @Override
    public void onDisable() {
        // Stop tick loop
        if (afflictionManager != null) {
            afflictionManager.stop();
        }

        // Stop time listener
        if (timeListener != null) {
            timeListener.stop();
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
    public static @Nullable Afflictions getInstance() {
        return instance;
    }

    /**
     * Get the configuration manager.
     */
    public @Nullable ConfigManager getConfigManager() {
        return configManager;
    }

    /**
     * Get the localization manager.
     */
    public @Nullable LocalizationManager getLocalizationManager() {
        return localizationManager;
    }

    /**
     * Get the affliction manager.
     */
    public @Nullable AfflictionManager getAfflictionManager() {
        return afflictionManager;
    }

    /**
     * Get the hook manager.
     */
    public @Nullable HookManager getHookManager() {
        return hookManager;
    }

    /**
     * Get the storage manager.
     */
    public @Nullable StorageManager getStorageManager() {
        return storageManager;
    }

    /**
     * Register all built-in afflictions.
     */
    private void registerAfflictions() {
        // Load and register Vampirism
        vampirismConfig = new VampirismConfig(this);
        vampirismConfig.load();
        afflictionManager.getRegistry().register(Vampirism.create(vampirismConfig));
        displayConfigs.put(vampirismConfig.getId(), vampirismConfig);

        getLogger().info("Registered " + afflictionManager.getRegistry().getAll().size() + " affliction(s)");
    }

    /**
     * Get an affliction display config by ID.
     *
     * @param afflictionId The affliction ID (e.g., "vampirism")
     * @return The display config, or null if not found
     */
    public @Nullable AfflictionDisplayConfig getDisplayConfig(@NotNull String afflictionId) {
        return displayConfigs.get(afflictionId.toLowerCase());
    }

    /**
     * Get the Vampirism configuration.
     */
    public @Nullable VampirismConfig getVampirismConfig() {
        return vampirismConfig;
    }
}
