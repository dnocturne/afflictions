package com.dnocturne.afflictions.config;

import com.dnocturne.afflictions.Afflictions;
import dev.dejvokep.boostedyaml.YamlDocument;
import dev.dejvokep.boostedyaml.dvs.versioning.BasicVersioning;
import dev.dejvokep.boostedyaml.settings.dumper.DumperSettings;
import dev.dejvokep.boostedyaml.settings.general.GeneralSettings;
import dev.dejvokep.boostedyaml.settings.loader.LoaderSettings;
import dev.dejvokep.boostedyaml.settings.updater.UpdaterSettings;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Manages all plugin configuration files using BoostedYAML.
 */
public class ConfigManager {

    private final Afflictions plugin;
    private final Logger logger;
    private final Map<String, YamlDocument> configs = new HashMap<>();

    private YamlDocument mainConfig;

    public ConfigManager(Afflictions plugin) {
        this.plugin = plugin;
        this.logger = plugin.getLogger();
    }

    /**
     * Load all configuration files.
     */
    public void load() {
        try {
            mainConfig = loadConfig("config.yml");
            configs.put("config", mainConfig);

            logger.info("Configuration loaded successfully");
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Failed to load configuration", e);
        }
    }

    /**
     * Reload all configuration files.
     */
    public void reload() {
        try {
            for (YamlDocument doc : configs.values()) {
                doc.reload();
            }
            logger.info("Configuration reloaded successfully");
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Failed to reload configuration", e);
        }
    }

    /**
     * Save all configuration files.
     */
    public void save() {
        try {
            for (YamlDocument doc : configs.values()) {
                doc.save();
            }
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Failed to save configuration", e);
        }
    }

    /**
     * Load a configuration file with defaults from resources.
     */
    public YamlDocument loadConfig(String fileName) throws IOException {
        return YamlDocument.create(
                new File(plugin.getDataFolder(), fileName),
                Objects.requireNonNull(plugin.getResource(fileName)),
                GeneralSettings.DEFAULT,
                LoaderSettings.builder()
                        .setAutoUpdate(true)
                        .build(),
                DumperSettings.DEFAULT,
                UpdaterSettings.builder()
                        .setVersioning(new BasicVersioning("config-version"))
                        .setOptionSorting(UpdaterSettings.OptionSorting.SORT_BY_DEFAULTS)
                        .build()
        );
    }

    /**
     * Load a configuration file without defaults (pure data file).
     */
    public YamlDocument loadDataFile(String fileName) throws IOException {
        return YamlDocument.create(
                new File(plugin.getDataFolder(), fileName),
                GeneralSettings.DEFAULT,
                LoaderSettings.DEFAULT,
                DumperSettings.DEFAULT,
                UpdaterSettings.DEFAULT
        );
    }

    /**
     * Load an affliction-specific config file.
     */
    public YamlDocument loadAfflictionConfig(String afflictionId) throws IOException {
        String fileName = "afflictions/" + afflictionId + ".yml";
        YamlDocument doc = loadConfig(fileName);
        configs.put("affliction:" + afflictionId, doc);
        return doc;
    }

    /**
     * Get the main configuration.
     */
    public YamlDocument getMainConfig() {
        return mainConfig;
    }

    /**
     * Get a loaded configuration by name.
     */
    public YamlDocument getConfig(String name) {
        return configs.get(name);
    }
}
