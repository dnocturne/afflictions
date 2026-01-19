package com.dnocturne.afflictions.affliction.impl;

import com.dnocturne.afflictions.affliction.config.VampirismConfig;
import com.dnocturne.afflictions.api.affliction.AbstractAffliction;
import com.dnocturne.afflictions.api.affliction.AfflictionCategory;
import com.dnocturne.afflictions.component.effect.BloodComponent;
import com.dnocturne.afflictions.component.effect.BloodHungerComponent;
import com.dnocturne.afflictions.component.effect.NightBonusComponent;
import com.dnocturne.afflictions.component.effect.SunlightDamageComponent;

/**
 * Vampirism affliction - the curse of the night.
 *
 * Effects:
 * - Takes damage when exposed to sunlight
 * - Higher levels provide more control (reduced sun damage)
 *
 * Future planned effects:
 * - Night vision
 * - Enhanced strength at night
 * - Blood thirst mechanics
 * - Weakness to holy items
 */
public class Vampirism extends AbstractAffliction {

    public static final String ID = "vampirism";

    private final VampirismConfig config;

    private Vampirism(Builder builder, VampirismConfig config) {
        super(builder);
        this.config = config;
    }

    /**
     * Create a new Vampirism affliction with default settings.
     */
    public static Vampirism create() {
        return new Builder().build();
    }

    /**
     * Create a new Vampirism affliction from configuration.
     */
    public static Vampirism create(VampirismConfig config) {
        return new Builder()
                .fromConfig(config)
                .build(config);
    }

    /**
     * Create a new Vampirism affliction with custom settings.
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Get the configuration for this affliction.
     */
    public VampirismConfig getConfig() {
        return config;
    }

    /**
     * Get the prefix for this affliction (from config).
     * Returns empty string if no config is set.
     */
    public String getPrefix() {
        return config != null ? config.getPrefix() : "";
    }

    public static class Builder extends AbstractAffliction.Builder {

        private double sunDamage = 2.0;
        private int damageTickInterval = 1;
        private boolean weatherProtection = true;
        private double helmetReduction = 0.5;

        // Blood system settings
        private boolean bloodEnabled = false;
        private double maxBlood = 100;
        private double startBlood = 50;
        private double passiveDrain = 0.1;
        private double sunDrain = 0.5;
        private double emptySunMultiplier = 2.0;

        // Action bar settings
        private boolean actionBarEnabled = true;
        private int actionBarUpdateInterval = 10;
        private boolean actionBarOnlyOnChange = false;

        // Blood hunger settings
        private boolean bloodHungerEnabled = false;
        private int bloodHungerTickInterval = 1;
        private double bloodHungerThreshold = 20;
        private boolean hungerSlownessEnabled = true;
        private int hungerSlownessBaseAmplifier = 0;
        private double hungerSlownessMaxScaling = 2;
        private boolean hungerWeaknessEnabled = true;
        private int hungerWeaknessBaseAmplifier = 0;
        private double hungerWeaknessMaxScaling = 1;

        // Night bonus settings
        private boolean nightBonusEnabled = false;
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

        // Sun grace period settings
        private boolean sunGracePeriodEnabled = false;
        private double sunGraceBaseDuration = 3.0;
        private double sunGraceLevelScaling = 1.0;
        private boolean sunGraceParticles = true;
        private int sunGraceParticleCount = 5;

        public Builder() {
            super(ID);
            this.displayName = "Vampirism";
            this.description = "A dark curse that burns in sunlight but grants power in darkness.";
            this.category = AfflictionCategory.SUPERNATURAL;
            this.maxLevel = 5;
            this.curable = true;
        }

        /**
         * Load settings from a VampirismConfig.
         */
        public Builder fromConfig(VampirismConfig config) {
            // Use plain "Vampirism" for internal display name (commands, etc.)
            // The formatted names are accessed via config for placeholders
            this.displayName = "Vampirism";
            this.description = config.getDescription();
            this.maxLevel = config.getMaxLevel();
            this.curable = config.isCurable();
            this.sunDamage = config.getBaseDamage();
            this.damageTickInterval = config.getTickInterval();
            this.weatherProtection = config.isWeatherProtection();
            this.helmetReduction = config.getHelmetReduction();

            // Blood system settings
            this.bloodEnabled = config.isBloodEnabled();
            this.maxBlood = config.getMaxBlood();
            this.startBlood = config.getStartBlood();
            this.passiveDrain = config.getPassiveDrain();
            this.sunDrain = config.getSunDrain();
            this.emptySunMultiplier = config.getEmptySunMultiplier();

            // Action bar settings
            this.actionBarEnabled = config.isActionBarEnabled();
            this.actionBarUpdateInterval = config.getActionBarUpdateInterval();
            this.actionBarOnlyOnChange = config.isActionBarOnlyOnChange();

            // Blood hunger settings
            this.bloodHungerEnabled = config.isBloodHungerEnabled();
            this.bloodHungerTickInterval = config.getBloodHungerTickInterval();
            this.bloodHungerThreshold = config.getBloodHungerThreshold();
            this.hungerSlownessEnabled = config.isHungerSlownessEnabled();
            this.hungerSlownessBaseAmplifier = config.getHungerSlownessBaseAmplifier();
            this.hungerSlownessMaxScaling = config.getHungerSlownessMaxScaling();
            this.hungerWeaknessEnabled = config.isHungerWeaknessEnabled();
            this.hungerWeaknessBaseAmplifier = config.getHungerWeaknessBaseAmplifier();
            this.hungerWeaknessMaxScaling = config.getHungerWeaknessMaxScaling();

            // Night bonus settings
            this.nightBonusEnabled = config.isNightBonusEnabled();
            this.nightBonusTickInterval = config.getNightBonusTickInterval();
            this.nightSpeedEnabled = config.isNightSpeedEnabled();
            this.nightSpeedBaseAmplifier = config.getNightSpeedBaseAmplifier();
            this.nightSpeedLevelScaling = config.getNightSpeedLevelScaling();
            this.nightStrengthEnabled = config.isNightStrengthEnabled();
            this.nightStrengthBaseAmplifier = config.getNightStrengthBaseAmplifier();
            this.nightStrengthLevelScaling = config.getNightStrengthLevelScaling();
            this.nightJumpEnabled = config.isNightJumpEnabled();
            this.nightJumpBaseAmplifier = config.getNightJumpBaseAmplifier();
            this.nightJumpLevelScaling = config.getNightJumpLevelScaling();
            this.nightVisionEnabled = config.isNightVisionEnabled();

            // Sun grace period settings
            this.sunGracePeriodEnabled = config.isSunGracePeriodEnabled();
            this.sunGraceBaseDuration = config.getSunGraceBaseDuration();
            this.sunGraceLevelScaling = config.getSunGraceLevelScaling();
            this.sunGraceParticles = config.isSunGraceParticles();
            this.sunGraceParticleCount = config.getSunGraceParticleCount();
            return this;
        }

        public Builder sunDamage(double damage) {
            this.sunDamage = damage;
            return this;
        }

        public Builder damageTickInterval(int interval) {
            this.damageTickInterval = interval;
            return this;
        }

        public Builder weatherProtection(boolean enabled) {
            this.weatherProtection = enabled;
            return this;
        }

        public Builder helmetReduction(double reduction) {
            this.helmetReduction = reduction;
            return this;
        }

        @Override
        public Vampirism build() {
            return build(null);
        }

        public Vampirism build(VampirismConfig config) {
            validate();

            // Add blood component if enabled
            if (bloodEnabled) {
                this.components.add(new BloodComponent(
                        "vampirism_blood",
                        maxBlood,
                        startBlood,
                        passiveDrain,
                        actionBarEnabled,
                        actionBarUpdateInterval,
                        actionBarOnlyOnChange
                ));
            }

            // Add sunlight damage component with blood integration and grace period
            this.components.add(new SunlightDamageComponent(
                    "vampirism_sun_damage",
                    sunDamage,
                    damageTickInterval,
                    weatherProtection,
                    helmetReduction,
                    bloodEnabled,
                    sunDrain,
                    emptySunMultiplier,
                    sunGracePeriodEnabled,
                    sunGraceBaseDuration,
                    sunGraceLevelScaling,
                    sunGraceParticles,
                    sunGraceParticleCount
            ));

            // Add night bonus component if enabled
            if (nightBonusEnabled) {
                this.components.add(new NightBonusComponent(
                        "vampirism_night_bonus",
                        nightBonusTickInterval,
                        nightSpeedEnabled, nightSpeedBaseAmplifier, nightSpeedLevelScaling,
                        nightStrengthEnabled, nightStrengthBaseAmplifier, nightStrengthLevelScaling,
                        nightJumpEnabled, nightJumpBaseAmplifier, nightJumpLevelScaling,
                        nightVisionEnabled
                ));
            }

            // Add blood hunger component if blood system is enabled and hunger is enabled
            if (bloodEnabled && bloodHungerEnabled) {
                this.components.add(new BloodHungerComponent(
                        "vampirism_blood_hunger",
                        bloodHungerTickInterval,
                        maxBlood,
                        bloodHungerThreshold,
                        hungerSlownessEnabled, hungerSlownessBaseAmplifier, hungerSlownessMaxScaling,
                        hungerWeaknessEnabled, hungerWeaknessBaseAmplifier, hungerWeaknessMaxScaling
                ));
            }

            return new Vampirism(this, config);
        }
    }
}
