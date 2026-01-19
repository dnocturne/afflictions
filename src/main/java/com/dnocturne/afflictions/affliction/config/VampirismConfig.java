package com.dnocturne.afflictions.affliction.config;

import com.dnocturne.afflictions.Afflictions;
import com.dnocturne.afflictions.affliction.impl.Vampirism;
import com.dnocturne.afflictions.api.affliction.Affliction;
import dev.dejvokep.boostedyaml.YamlDocument;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import org.bukkit.entity.EntityType;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
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

    // Blood system settings
    private boolean bloodEnabled = true;
    private double maxBlood = 100;
    private double startBlood = 50;
    private double bloodGainPerDamage = 2.0;
    private double passiveDrain = 0.1;
    private double sunDrain = 0.5;
    private double emptySunMultiplier = 2.0;

    // Blood source settings
    private boolean bloodSourcesEnabled = true;
    private boolean bloodSourcesWhitelist = false; // false = blacklist mode
    private final Set<EntityType> bloodSourceEntities = new HashSet<>();

    // Blood action bar settings
    private boolean actionBarEnabled = true;
    private int actionBarUpdateInterval = 10;
    private boolean actionBarOnlyOnChange = false;

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
            config = plugin.getConfigManager().loadSubdirectoryConfig("afflictions", "vampirism");
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

        // Blood system settings
        bloodEnabled = config.getBoolean("blood.enabled", bloodEnabled);
        maxBlood = config.getDouble("blood.max-blood", maxBlood);
        startBlood = config.getDouble("blood.start-blood", startBlood);
        bloodGainPerDamage = config.getDouble("blood.gain-per-damage", bloodGainPerDamage);
        passiveDrain = config.getDouble("blood.passive-drain", passiveDrain);
        sunDrain = config.getDouble("blood.sun-drain", sunDrain);
        emptySunMultiplier = config.getDouble("blood.empty-sun-multiplier", emptySunMultiplier);

        // Blood source settings
        bloodSourcesEnabled = config.getBoolean("blood.sources.enabled", bloodSourcesEnabled);
        String mode = config.getString("blood.sources.mode", "blacklist");
        bloodSourcesWhitelist = "whitelist".equalsIgnoreCase(mode);

        bloodSourceEntities.clear();
        List<String> entityNames = config.getStringList("blood.sources.entities");
        for (String entityName : entityNames) {
            try {
                EntityType type = EntityType.valueOf(entityName.toUpperCase());
                bloodSourceEntities.add(type);
            } catch (IllegalArgumentException e) {
                plugin.getLogger().warning("Unknown entity type in blood.sources.entities: " + entityName);
            }
        }

        // Blood action bar settings
        actionBarEnabled = config.getBoolean("blood.action-bar.enabled", actionBarEnabled);
        actionBarUpdateInterval = config.getInt("blood.action-bar.update-interval", actionBarUpdateInterval);
        actionBarOnlyOnChange = config.getBoolean("blood.action-bar.only-on-change", actionBarOnlyOnChange);

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

    // Blood system getters

    public boolean isBloodEnabled() {
        return bloodEnabled;
    }

    public double getMaxBlood() {
        return maxBlood;
    }

    public double getStartBlood() {
        return startBlood;
    }

    public double getBloodGainPerDamage() {
        return bloodGainPerDamage;
    }

    public double getPassiveDrain() {
        return passiveDrain;
    }

    public double getSunDrain() {
        return sunDrain;
    }

    public double getEmptySunMultiplier() {
        return emptySunMultiplier;
    }

    // Blood source getters

    public boolean isBloodSourcesEnabled() {
        return bloodSourcesEnabled;
    }

    public boolean isBloodSourcesWhitelist() {
        return bloodSourcesWhitelist;
    }

    /**
     * Check if the given entity type can provide blood.
     *
     * @param entityType The entity type to check
     * @return true if the entity can provide blood
     */
    public boolean canProvideBlood(EntityType entityType) {
        if (!bloodSourcesEnabled) {
            return false;
        }

        boolean inList = bloodSourceEntities.contains(entityType);
        // Whitelist: must be in list. Blacklist: must NOT be in list.
        return bloodSourcesWhitelist ? inList : !inList;
    }

    public boolean isActionBarEnabled() {
        return actionBarEnabled;
    }

    public int getActionBarUpdateInterval() {
        return actionBarUpdateInterval;
    }

    public boolean isActionBarOnlyOnChange() {
        return actionBarOnlyOnChange;
    }

    // Sun damage getters

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
