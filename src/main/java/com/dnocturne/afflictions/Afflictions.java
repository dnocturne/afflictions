package com.dnocturne.afflictions;

import com.dnocturne.afflictions.affliction.config.AbstractAfflictionConfig;
import com.dnocturne.afflictions.affliction.config.AfflictionDisplayConfig;
import com.dnocturne.afflictions.affliction.config.VampirismConfig;
import com.dnocturne.afflictions.command.CommandManager;
import com.dnocturne.afflictions.hook.HookManager;
import com.dnocturne.afflictions.listener.BloodGainListener;
import com.dnocturne.afflictions.listener.PlayerListener;
import com.dnocturne.afflictions.listener.TimeListener;
import com.dnocturne.afflictions.manager.AfflictionManager;
import com.dnocturne.afflictions.storage.StorageManager;
import com.dnocturne.basalt.BasaltPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Main plugin class for Afflictions.
 */
public class Afflictions extends BasaltPlugin {

    private static @Nullable Afflictions instance;

    private @Nullable StorageManager storageManager;
    private @Nullable AfflictionManager afflictionManager;
    private @Nullable HookManager hookManager;
    private @Nullable CommandManager commandManager;
    private @Nullable TimeListener timeListener;

    // Affliction configs
    private final List<AbstractAfflictionConfig> afflictionConfigs = new ArrayList<>();

    // Registry of affliction display configs by ID
    private final Map<String, AfflictionDisplayConfig> displayConfigs = new HashMap<>();

    @Override
    protected void enable() {
        instance = this;

        // Configure localization
        getLocalizationManager()
                .availableLanguages("en", "es", "de", "fr", "pt", "zh", "ja", "ko", "ru")
                .defaultLanguage("en")
                .languageFromConfig(() -> getConfigManager().getMainConfig().getString("general.language", "en"))
                .load();

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

        // Configure tick rate from config (validate bounds)
        long tickRate = getConfigManager().getMainConfig().getLong("general.tick-rate", 20L);
        if (tickRate <= 0) {
            getLogger().warning("Invalid tick-rate " + tickRate + " in config, using default of 20");
            tickRate = 20L;
        } else if (tickRate > 1200) {
            // More than 60 seconds is probably a mistake
            getLogger().warning("Tick-rate " + tickRate + " seems too high (>60s), using default of 20");
            tickRate = 20L;
        }
        afflictionManager.setTickRate(tickRate);

        // Start affliction tick loop
        afflictionManager.start();

        // Start time listener
        timeListener = new TimeListener(this);
        timeListener.start();

        // Register player listener
        new PlayerListener(this).register();

        // Register blood gain listener for vampires
        new BloodGainListener(this).register();

        // Register commands (may fail in test environments without Paper Brigadier)
        try {
            commandManager = new CommandManager(this);
            commandManager.registerCommands();
        } catch (NoClassDefFoundError | ExceptionInInitializerError e) {
            getLogger().warning("Failed to register commands - Paper Brigadier API not available (test environment?)");
            commandManager = null;
        }

        // Register default afflictions
        registerAfflictions();

        getLogger().info("Afflictions v" + getPluginMeta().getVersion() + " has been enabled!");
    }

    @Override
    protected void disable() {
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
     * Load and register all built-in afflictions.
     */
    private void registerAfflictions() {
        // Add all affliction configs here
        afflictionConfigs.add(new VampirismConfig(this));

        // Load and register each config generically
        int registered = 0;
        for (AbstractAfflictionConfig config : afflictionConfigs) {
            config.load();
            if (config.isEnabled()) {
                afflictionManager.getRegistry().register(config.createAffliction());
                displayConfigs.put(config.getId(), config);
                registered++;
            } else {
                getLogger().info(config.getId() + " affliction is disabled");
            }
        }

        getLogger().info("Registered " + registered + " affliction(s)");
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
     * Get an affliction config by type.
     *
     * @param configClass The config class to look for
     * @param <T> The config type
     * @return The config instance, or null if not found
     */
    public <T extends AbstractAfflictionConfig> @Nullable T getAfflictionConfig(@NotNull Class<T> configClass) {
        for (AbstractAfflictionConfig config : afflictionConfigs) {
            if (configClass.isInstance(config)) {
                return configClass.cast(config);
            }
        }
        return null;
    }
}
