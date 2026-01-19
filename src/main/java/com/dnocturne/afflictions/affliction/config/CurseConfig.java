package com.dnocturne.afflictions.affliction.config;

import com.dnocturne.afflictions.Afflictions;
import com.dnocturne.afflictions.affliction.config.curse.*;
import com.dnocturne.afflictions.affliction.impl.Curse;
import com.dnocturne.afflictions.api.affliction.Affliction;
import dev.dejvokep.boostedyaml.YamlDocument;
import dev.dejvokep.boostedyaml.block.implementation.Section;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

/**
 * Configuration holder for a curse affliction.
 *
 * <p>Curses are data-driven afflictions loaded from YAML files in the
 * afflictions directory. Each curse file defines effects, triggers,
 * duration, and display settings.</p>
 *
 * <p>Example curse file structure:</p>
 * <pre>
 * # curse_weakness.yml
 * display:
 *   name: "&cWeakened"
 *   affliction: "&4Curse of Weakness"
 *   prefix: "&4[W] "
 *   description: "A curse that saps your strength."
 *
 * settings:
 *   enabled: true
 *   max-level: 3
 *   stackable: false
 *   duration:
 *     type: permanent  # permanent, fixed, or level-scaled
 *     base-duration: 0  # in seconds (0 = permanent)
 *     level-scaling: 0
 *
 * trigger:
 *   type: always  # always, day, night, full_moon, sunlight, etc.
 *   inverted: false
 *
 * effects:
 *   potions:
 *     - type: WEAKNESS
 *       base-amplifier: 0
 *       level-scaling: 0.5
 *   damage:
 *     enabled: false
 *   attributes:
 *     - attribute: GENERIC_ATTACK_DAMAGE
 *       base-amount: -1.0
 *       level-scaling: -0.5
 *       operation: ADD_NUMBER
 *   particles:
 *     enabled: true
 *     type: WITCH
 *     count: 5
 *   sound:
 *     enabled: false
 *
 * messages:
 *   on-apply: "&cYou have been cursed!"
 *   on-remove: "&aThe curse has been lifted."
 *
 * levels:
 *   1:
 *     title: "Minor Curse"
 *   2:
 *     title: "Curse"
 *   3:
 *     title: "Greater Curse"
 * </pre>
 */
public class CurseConfig extends AbstractAfflictionConfig {

    private final Afflictions plugin;
    private final String id;
    private final String fileName;
    private @Nullable YamlDocument config;

    // Display settings
    private String name = "<#666666>Cursed";
    private String afflictionName = "<#444444>Curse";
    private String prefix = "<#444444>[C] ";
    private String description = "A dark curse.";

    // Affliction settings
    private int maxLevel = 3;
    private boolean stackable = false;

    // Duration settings
    private String durationType = "permanent";
    private long baseDuration = 0;
    private double durationLevelScaling = 0;

    // Level titles (level -> title)
    private final Map<Integer, String> levelTitles = new HashMap<>();

    // Trigger config
    private TriggerConfig triggerConfig = TriggerConfig.of("always");

    // Effect configs
    private List<PotionEffectConfig> potionEffects = List.of();
    private DamageConfig damageConfig = DamageConfig.disabled();
    private List<AttributeConfig> attributeConfigs = List.of();
    private ParticleConfig particleConfig = ParticleConfig.disabled();
    private SoundConfig soundConfig = SoundConfig.disabled();
    private MessageConfig messageConfig = MessageConfig.empty();

    /**
     * Create a curse config.
     *
     * @param plugin   The plugin instance
     * @param id       The curse ID (derived from filename, e.g., "weakness" from "curse_weakness.yml")
     * @param fileName The YAML file name (e.g., "curse_weakness.yml")
     */
    public CurseConfig(@NotNull Afflictions plugin, @NotNull String id, @NotNull String fileName) {
        this.plugin = plugin;
        this.id = id;
        this.fileName = fileName;
    }

    @Override
    public void load() {
        try {
            // Use loadSubdirectoryDataFile since curse configs are user-editable
            // data files that don't require versioning/auto-update
            config = plugin.getConfigManager().loadSubdirectoryDataFile("afflictions", fileName.replace(".yml", ""));
            loadValues();
            plugin.getLogger().info("Loaded curse configuration: " + id);
        } catch (IOException e) {
            plugin.getLogger().log(Level.WARNING, "Failed to load curse config " + fileName + ", using defaults", e);
        }
    }

    @Override
    public void reload() {
        if (config != null) {
            try {
                config.reload();
                loadValues();
            } catch (IOException e) {
                plugin.getLogger().log(Level.WARNING, "Failed to reload curse config " + fileName, e);
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
        stackable = config.getBoolean("settings.stackable", stackable);

        // Duration settings
        durationType = config.getString("settings.duration.type", durationType);
        baseDuration = config.getLong("settings.duration.base-duration", baseDuration);
        durationLevelScaling = config.getDouble("settings.duration.level-scaling", durationLevelScaling);

        // Level titles
        levelTitles.clear();
        for (int level = 1; level <= maxLevel; level++) {
            String title = config.getString("levels." + level + ".title");
            if (title != null) {
                levelTitles.put(level, title);
            }
        }

        // Build legacy cache for PlaceholderAPI
        buildLegacyCache();

        // Trigger config
        Section triggerSection = config.getSection("trigger");
        if (triggerSection != null) {
            triggerConfig = TriggerConfig.fromSection(triggerSection);
            if (triggerConfig == null) {
                triggerConfig = TriggerConfig.of("always");
            }
        }

        // Effect configs
        Section effectsSection = config.getSection("effects");
        if (effectsSection != null) {
            potionEffects = PotionEffectConfig.fromSection(effectsSection);
            damageConfig = DamageConfig.fromSection(effectsSection);
            attributeConfigs = AttributeConfig.fromSection(effectsSection);
            particleConfig = ParticleConfig.fromSection(effectsSection);
            soundConfig = SoundConfig.fromSection(effectsSection);
        }

        // Message config
        messageConfig = MessageConfig.fromSection(config);
    }

    // Getters

    @Override
    public @NotNull String getId() {
        return id;
    }

    @Override
    public @NotNull String getName() {
        return name;
    }

    @Override
    public @NotNull String getAfflictionName() {
        return afflictionName;
    }

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

    @Override
    public @Nullable String getLevelTitle(int level) {
        return levelTitles.get(level);
    }

    /**
     * Check if this curse is stackable (can have multiple instances).
     */
    public boolean isStackable() {
        return stackable;
    }

    /**
     * Get the duration type: "permanent", "fixed", or "level-scaled".
     */
    public @NotNull String getDurationType() {
        return durationType;
    }

    /**
     * Get the base duration in seconds.
     */
    public long getBaseDuration() {
        return baseDuration;
    }

    /**
     * Get the duration level scaling factor.
     */
    public double getDurationLevelScaling() {
        return durationLevelScaling;
    }

    /**
     * Calculate the duration for a specific level.
     *
     * @param level The curse level
     * @return The duration in seconds (0 = permanent)
     */
    public long calculateDuration(int level) {
        if ("permanent".equalsIgnoreCase(durationType)) {
            return 0;
        }
        return (long) (baseDuration + ((level - 1) * durationLevelScaling));
    }

    /**
     * Check if this curse is permanent.
     */
    public boolean isPermanent() {
        return "permanent".equalsIgnoreCase(durationType) || baseDuration <= 0;
    }

    /**
     * Get the trigger configuration.
     */
    public @NotNull TriggerConfig getTriggerConfig() {
        return triggerConfig;
    }

    /**
     * Get the potion effect configurations.
     */
    public @NotNull List<PotionEffectConfig> getPotionEffects() {
        return potionEffects;
    }

    /**
     * Get the damage configuration.
     */
    public @NotNull DamageConfig getDamageConfig() {
        return damageConfig;
    }

    /**
     * Get the attribute configurations.
     */
    public @NotNull List<AttributeConfig> getAttributeConfigs() {
        return attributeConfigs;
    }

    /**
     * Get the particle configuration.
     */
    public @NotNull ParticleConfig getParticleConfig() {
        return particleConfig;
    }

    /**
     * Get the sound configuration.
     */
    public @NotNull SoundConfig getSoundConfig() {
        return soundConfig;
    }

    /**
     * Get the message configuration.
     */
    public @NotNull MessageConfig getMessageConfig() {
        return messageConfig;
    }

    /**
     * Get the YAML file name.
     */
    public @NotNull String getFileName() {
        return fileName;
    }

    @Override
    public @NotNull Affliction createAffliction() {
        return Curse.create(this);
    }
}
