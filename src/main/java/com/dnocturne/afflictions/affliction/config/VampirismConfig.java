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

    // Blood hunger settings
    private boolean bloodHungerEnabled = true;
    private double bloodHungerThreshold = 20;
    private int bloodHungerTickInterval = 1;
    private boolean hungerSlownessEnabled = true;
    private int hungerSlownessBaseAmplifier = 0;
    private double hungerSlownessMaxScaling = 2;
    private boolean hungerWeaknessEnabled = true;
    private int hungerWeaknessBaseAmplifier = 0;
    private double hungerWeaknessMaxScaling = 1;

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

    // Sun grace period settings
    private boolean sunGracePeriodEnabled = true;
    private double sunGraceBaseDuration = 3.0;
    private double sunGraceLevelScaling = 1.0;
    private boolean sunGraceParticles = true;
    private int sunGraceParticleCount = 5;

    // Night bonus settings
    private boolean nightBonusEnabled = true;
    private int nightBonusTickInterval = 1;
    private boolean nightSpeedEnabled = true;
    private int nightSpeedBaseAmplifier = 0;
    private double nightSpeedLevelScaling = 0.25;
    private boolean nightStrengthEnabled = false;
    private int nightStrengthBaseAmplifier = 0;
    private double nightStrengthLevelScaling = 0.25;
    private boolean nightJumpEnabled = false;
    private int nightJumpBaseAmplifier = 0;
    private double nightJumpLevelScaling = 0.25;
    private boolean nightVisionEnabled = true;

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

        // Blood hunger settings
        bloodHungerEnabled = config.getBoolean("blood.hunger.enabled", bloodHungerEnabled);
        bloodHungerThreshold = config.getDouble("blood.hunger.threshold", bloodHungerThreshold);
        bloodHungerTickInterval = config.getInt("blood.hunger.tick-interval", bloodHungerTickInterval);
        hungerSlownessEnabled = config.getBoolean("blood.hunger.slowness.enabled", hungerSlownessEnabled);
        hungerSlownessBaseAmplifier = config.getInt("blood.hunger.slowness.base-amplifier", hungerSlownessBaseAmplifier);
        hungerSlownessMaxScaling = config.getDouble("blood.hunger.slowness.max-scaling", hungerSlownessMaxScaling);
        hungerWeaknessEnabled = config.getBoolean("blood.hunger.weakness.enabled", hungerWeaknessEnabled);
        hungerWeaknessBaseAmplifier = config.getInt("blood.hunger.weakness.base-amplifier", hungerWeaknessBaseAmplifier);
        hungerWeaknessMaxScaling = config.getDouble("blood.hunger.weakness.max-scaling", hungerWeaknessMaxScaling);

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

        // Sun grace period settings
        sunGracePeriodEnabled = config.getBoolean("sun-damage.grace-period.enabled", sunGracePeriodEnabled);
        sunGraceBaseDuration = config.getDouble("sun-damage.grace-period.base-duration", sunGraceBaseDuration);
        sunGraceLevelScaling = config.getDouble("sun-damage.grace-period.level-scaling", sunGraceLevelScaling);
        sunGraceParticles = config.getBoolean("sun-damage.grace-period.particles", sunGraceParticles);
        sunGraceParticleCount = config.getInt("sun-damage.grace-period.particle-count", sunGraceParticleCount);

        // Night bonus settings
        nightBonusEnabled = config.getBoolean("night-bonuses.enabled", nightBonusEnabled);
        nightBonusTickInterval = config.getInt("night-bonuses.tick-interval", nightBonusTickInterval);
        nightSpeedEnabled = config.getBoolean("night-bonuses.speed.enabled", nightSpeedEnabled);
        nightSpeedBaseAmplifier = config.getInt("night-bonuses.speed.base-amplifier", nightSpeedBaseAmplifier);
        nightSpeedLevelScaling = config.getDouble("night-bonuses.speed.level-scaling", nightSpeedLevelScaling);
        nightStrengthEnabled = config.getBoolean("night-bonuses.strength.enabled", nightStrengthEnabled);
        nightStrengthBaseAmplifier = config.getInt("night-bonuses.strength.base-amplifier", nightStrengthBaseAmplifier);
        nightStrengthLevelScaling = config.getDouble("night-bonuses.strength.level-scaling", nightStrengthLevelScaling);
        nightJumpEnabled = config.getBoolean("night-bonuses.jump.enabled", nightJumpEnabled);
        nightJumpBaseAmplifier = config.getInt("night-bonuses.jump.base-amplifier", nightJumpBaseAmplifier);
        nightJumpLevelScaling = config.getDouble("night-bonuses.jump.level-scaling", nightJumpLevelScaling);
        nightVisionEnabled = config.getBoolean("night-bonuses.night-vision.enabled", nightVisionEnabled);
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

    // Blood hunger getters

    public boolean isBloodHungerEnabled() {
        return bloodHungerEnabled;
    }

    public double getBloodHungerThreshold() {
        return bloodHungerThreshold;
    }

    public int getBloodHungerTickInterval() {
        return bloodHungerTickInterval;
    }

    public boolean isHungerSlownessEnabled() {
        return hungerSlownessEnabled;
    }

    public int getHungerSlownessBaseAmplifier() {
        return hungerSlownessBaseAmplifier;
    }

    public double getHungerSlownessMaxScaling() {
        return hungerSlownessMaxScaling;
    }

    public boolean isHungerWeaknessEnabled() {
        return hungerWeaknessEnabled;
    }

    public int getHungerWeaknessBaseAmplifier() {
        return hungerWeaknessBaseAmplifier;
    }

    public double getHungerWeaknessMaxScaling() {
        return hungerWeaknessMaxScaling;
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

    // Sun grace period getters

    public boolean isSunGracePeriodEnabled() {
        return sunGracePeriodEnabled;
    }

    public double getSunGraceBaseDuration() {
        return sunGraceBaseDuration;
    }

    public double getSunGraceLevelScaling() {
        return sunGraceLevelScaling;
    }

    public boolean isSunGraceParticles() {
        return sunGraceParticles;
    }

    public int getSunGraceParticleCount() {
        return sunGraceParticleCount;
    }

    /**
     * Calculate the grace period duration for a specific vampire level.
     *
     * @param level The vampire level
     * @return The grace period in seconds
     */
    public double calculateGracePeriod(int level) {
        return sunGraceBaseDuration + ((level - 1) * sunGraceLevelScaling);
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

    // Night bonus getters

    public boolean isNightBonusEnabled() {
        return nightBonusEnabled;
    }

    public int getNightBonusTickInterval() {
        return nightBonusTickInterval;
    }

    public boolean isNightSpeedEnabled() {
        return nightSpeedEnabled;
    }

    public int getNightSpeedBaseAmplifier() {
        return nightSpeedBaseAmplifier;
    }

    public double getNightSpeedLevelScaling() {
        return nightSpeedLevelScaling;
    }

    public boolean isNightStrengthEnabled() {
        return nightStrengthEnabled;
    }

    public int getNightStrengthBaseAmplifier() {
        return nightStrengthBaseAmplifier;
    }

    public double getNightStrengthLevelScaling() {
        return nightStrengthLevelScaling;
    }

    public boolean isNightJumpEnabled() {
        return nightJumpEnabled;
    }

    public int getNightJumpBaseAmplifier() {
        return nightJumpBaseAmplifier;
    }

    public double getNightJumpLevelScaling() {
        return nightJumpLevelScaling;
    }

    public boolean isNightVisionEnabled() {
        return nightVisionEnabled;
    }

    /**
     * Calculate the amplifier for a night bonus effect based on level.
     *
     * @param baseAmplifier The base amplifier from config
     * @param levelScaling  The scaling factor per level
     * @param level         The player's vampire level
     * @return The final amplifier (clamped to 0-255)
     */
    public int calculateNightBonusAmplifier(int baseAmplifier, double levelScaling, int level) {
        double amplifier = baseAmplifier + ((level - 1) * levelScaling);
        return Math.max(0, Math.min(255, (int) amplifier));
    }

    @Override
    public @NotNull Affliction createAffliction() {
        return Vampirism.create(this);
    }
}
