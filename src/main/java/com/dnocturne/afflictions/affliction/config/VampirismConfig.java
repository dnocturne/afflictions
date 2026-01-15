package com.dnocturne.afflictions.affliction.config;

import com.dnocturne.afflictions.Afflictions;
import dev.dejvokep.boostedyaml.YamlDocument;

import java.io.IOException;
import java.util.logging.Level;

/**
 * Configuration holder for Vampirism affliction.
 */
public class VampirismConfig {

    private final Afflictions plugin;
    private YamlDocument config;

    // Display settings
    private String displayName = "Vampirism";
    private String formattedName = "<gradient:dark_red:red>Vampire</gradient>";
    private String description = "A dark curse that burns in sunlight but grants power in darkness.";
    private String prefix = "<dark_red>[<red>Vampire</red>]</dark_red> ";

    // Affliction settings
    private int maxLevel = 5;
    private boolean curable = true;

    // Sun damage settings
    private boolean sunDamageEnabled = true;
    private double baseDamage = 2.0;
    private int tickInterval = 1;
    private boolean weatherProtection = true;
    private double helmetReduction = 0.5;
    private double minimumDamage = 0.5;
    private int fireTicks = 40;

    public VampirismConfig(Afflictions plugin) {
        this.plugin = plugin;
    }

    /**
     * Load the configuration from file.
     */
    public void load() {
        try {
            config = plugin.getConfigManager().loadAfflictionConfig("vampirism");
            loadValues();
            plugin.getLogger().info("Loaded vampirism configuration");
        } catch (IOException e) {
            plugin.getLogger().log(Level.WARNING, "Failed to load vampirism config, using defaults", e);
        }
    }

    /**
     * Reload the configuration.
     */
    public void reload() {
        if (config != null) {
            try {
                config.reload();
                loadValues();
            } catch (IOException e) {
                plugin.getLogger().log(Level.WARNING, "Failed to reload vampirism config", e);
            }
        } else {
            load();
        }
    }

    private void loadValues() {
        if (config == null) return;

        // Display settings
        displayName = config.getString("display.name", displayName);
        formattedName = config.getString("display.formatted-name", formattedName);
        description = config.getString("display.description", description);
        prefix = config.getString("display.prefix", prefix);

        // Affliction settings
        maxLevel = config.getInt("settings.max-level", maxLevel);
        curable = config.getBoolean("settings.curable", curable);

        // Sun damage settings
        sunDamageEnabled = config.getBoolean("sun-damage.enabled", sunDamageEnabled);
        baseDamage = config.getDouble("sun-damage.base-damage", baseDamage);
        tickInterval = config.getInt("sun-damage.tick-interval", tickInterval);
        weatherProtection = config.getBoolean("sun-damage.weather-protection", weatherProtection);
        helmetReduction = config.getDouble("sun-damage.helmet-reduction", helmetReduction);
        minimumDamage = config.getDouble("sun-damage.minimum-damage", minimumDamage);
        fireTicks = config.getInt("sun-damage.fire-ticks", fireTicks);
    }

    // Getters

    public String getDisplayName() {
        return displayName;
    }

    public String getFormattedName() {
        return formattedName;
    }

    public String getDescription() {
        return description;
    }

    public String getPrefix() {
        return prefix;
    }

    public int getMaxLevel() {
        return maxLevel;
    }

    public boolean isCurable() {
        return curable;
    }

    public boolean isSunDamageEnabled() {
        return sunDamageEnabled;
    }

    public double getBaseDamage() {
        return baseDamage;
    }

    public int getTickInterval() {
        return tickInterval;
    }

    public boolean isWeatherProtection() {
        return weatherProtection;
    }

    public double getHelmetReduction() {
        return helmetReduction;
    }

    public double getMinimumDamage() {
        return minimumDamage;
    }

    public int getFireTicks() {
        return fireTicks;
    }
}
