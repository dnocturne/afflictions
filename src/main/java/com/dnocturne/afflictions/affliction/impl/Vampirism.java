package com.dnocturne.afflictions.affliction.impl;

import com.dnocturne.afflictions.api.affliction.AbstractAffliction;
import com.dnocturne.afflictions.api.affliction.AfflictionCategory;
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

    private Vampirism(Builder builder) {
        super(builder);
    }

    /**
     * Create a new Vampirism affliction with default settings.
     */
    public static Vampirism create() {
        return new Builder().build();
    }

    /**
     * Create a new Vampirism affliction with custom settings.
     */
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder extends AbstractAffliction.Builder {

        private double sunDamage = 2.0;
        private int damageTickInterval = 1; // Every affliction tick
        private boolean weatherProtection = true;
        private double helmetReduction = 0.5;

        public Builder() {
            super(ID);
            this.displayName = "Vampirism";
            this.description = "A dark curse that burns in sunlight but grants power in darkness.";
            this.category = AfflictionCategory.SUPERNATURAL;
            this.maxLevel = 5;
            this.curable = true;
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
            // Add sunlight damage component
            this.components.add(new SunlightDamageComponent(
                    "vampirism_sun_damage",
                    sunDamage,
                    damageTickInterval,
                    weatherProtection,
                    helmetReduction
            ));

            return new Vampirism(this);
        }
    }
}
