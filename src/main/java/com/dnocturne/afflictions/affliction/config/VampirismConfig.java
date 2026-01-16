package com.dnocturne.afflictions.affliction.config;

import com.dnocturne.afflictions.Afflictions;
import com.dnocturne.afflictions.affliction.impl.Vampirism;
import com.dnocturne.afflictions.api.affliction.Affliction;
import dev.dejvokep.boostedyaml.YamlDocument;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

/**
 * Configuration holder for Vampirism affliction.
 */
public class VampirismConfig extends AbstractAfflictionConfig {

    private static final String ID = "vampirism";

    private final Afflictions plugin;
    private @Nullable YamlDocument config;

    // Display settings (MiniMessage format - converted to legacy for PlaceholderAPI)
    private String name = "<#c93434>Vampire";
    private String afflictionName = "<#8b0000>Vampirism";
    private String prefix = "<#8b0000>[<#c93434>V<#8b0000>] ";
    private String description = "A dark curse that burns in sunlight but grants power in darkness.";

    // Affliction settings (enabled is inherited from AbstractAfflictionConfig)
    private int maxLevel = 5;
    private boolean curable = true;

    // Level titles (level -> title)
    private final Map<Integer, String> levelTitles = new HashMap<>();

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

    @Override
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
        name = config.getString("display.name", name);
        afflictionName = config.getString("display.affliction", afflictionName);
        prefix = config.getString("display.prefix", prefix);
        description = config.getString("display.description", description);

        // Affliction settings
        enabled = config.getBoolean("settings.enabled", enabled);
        maxLevel = config.getInt("settings.max-level", maxLevel);
        curable = config.getBoolean("settings.curable", curable);

        // Level titles
        levelTitles.clear();
        for (int level = 1; level <= maxLevel; level++) {
            String title = config.getString("levels." + level + ".title");
            if (title != null) {
                levelTitles.put(level, title);
            }
        }

        // Build legacy cache for PlaceholderAPI performance
        buildLegacyCache();

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

    @Override
    public @NotNull String getId() {
        return ID;
    }

    /**
     * Get the name of what the player "is" (e.g., "Vampire").
     * Used for "You are: {name}"
     */
    @Override
    public @NotNull String getName() {
        return name;
    }

    /**
     * Get the affliction name (e.g., "Vampirism").
     * Used for "Affliction: {affliction}"
     */
    @Override
    public @NotNull String getAfflictionName() {
        return afflictionName;
    }

    /**
     * Get the short prefix/tag (e.g., "[V]").
     * Used for chat prefixes, tab, etc.
     */
    @Override
    public @NotNull String getPrefix() {
        return prefix;
    }

    @Override
    public @NotNull String getDescription() {
        return description;
    }

    @Override
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

    /**
     * Get the title for a specific level.
     *
     * @param level The affliction level
     * @return The title for the level, or null if not configured
     */
    @Override
    public @Nullable String getLevelTitle(int level) {
        return levelTitles.get(level);
    }

    @Override
    public @NotNull Affliction createAffliction() {
        return Vampirism.create(this);
    }
}
