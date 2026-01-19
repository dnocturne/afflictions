package com.dnocturne.afflictions.affliction.config.curse;

import dev.dejvokep.boostedyaml.block.implementation.Section;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Configuration for curse damage over time.
 *
 * <p>Example YAML:</p>
 * <pre>
 * effects:
 *   damage:
 *     enabled: true
 *     base-damage: 1.0
 *     level-scaling: 0.5
 *     bypass-armor: false
 *     tick-interval: 20
 * </pre>
 */
public class DamageConfig {

    private final boolean enabled;
    private final double baseDamage;
    private final double levelScaling;
    private final boolean bypassArmor;
    private final int tickInterval;

    /**
     * Create a damage config.
     */
    public DamageConfig(boolean enabled, double baseDamage, double levelScaling,
                        boolean bypassArmor, int tickInterval) {
        this.enabled = enabled;
        this.baseDamage = baseDamage;
        this.levelScaling = levelScaling;
        this.bypassArmor = bypassArmor;
        this.tickInterval = tickInterval;
    }

    /**
     * Load a damage config from a YAML section.
     *
     * @param section The YAML section containing damage settings
     * @return The loaded damage config, or a disabled config if section is null
     */
    public static @NotNull DamageConfig fromSection(@Nullable Section section) {
        if (section == null) {
            return disabled();
        }

        Section damageSection = section.getSection("damage");
        if (damageSection == null) {
            return disabled();
        }

        boolean enabled = damageSection.getBoolean("enabled", false);
        double baseDamage = damageSection.getDouble("base-damage", 1.0);
        double levelScaling = damageSection.getDouble("level-scaling", 0.0);
        boolean bypassArmor = damageSection.getBoolean("bypass-armor", false);
        int tickInterval = damageSection.getInt("tick-interval", 20);

        return new DamageConfig(enabled, baseDamage, levelScaling, bypassArmor, tickInterval);
    }

    /**
     * Create a disabled damage config.
     */
    public static @NotNull DamageConfig disabled() {
        return new DamageConfig(false, 0, 0, false, 20);
    }

    /**
     * Check if damage is enabled.
     */
    public boolean isEnabled() {
        return enabled;
    }

    /**
     * Get the base damage (at level 1).
     */
    public double getBaseDamage() {
        return baseDamage;
    }

    /**
     * Get the level scaling factor.
     */
    public double getLevelScaling() {
        return levelScaling;
    }

    /**
     * Check if damage bypasses armor.
     */
    public boolean isBypassArmor() {
        return bypassArmor;
    }

    /**
     * Get the tick interval.
     */
    public int getTickInterval() {
        return tickInterval;
    }

    /**
     * Calculate the damage for a specific level.
     *
     * @param level The curse level
     * @return The calculated damage
     */
    public double calculateDamage(int level) {
        return baseDamage + ((level - 1) * levelScaling);
    }
}
