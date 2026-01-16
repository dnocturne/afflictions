package com.dnocturne.afflictions.component.effect;

import com.dnocturne.afflictions.api.affliction.AfflictionInstance;
import com.dnocturne.afflictions.api.component.TickableComponent;
import com.dnocturne.afflictions.condition.Condition;
import com.dnocturne.afflictions.condition.Conditions;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

/**
 * Deals damage to the player when exposed to sunlight.
 * Checks for daytime, sky visibility, weather, and helmet protection.
 *
 * <p>Uses the Condition system for reusable environment checks.</p>
 */
public class SunlightDamageComponent implements TickableComponent {

    private final String id;
    private final double baseDamage;
    private final int tickInterval;
    private final Condition exposureCondition;
    private final double helmetDamageReduction;
    private final boolean checkWeather;

    /**
     * Create a sunlight damage component with default weather checking.
     *
     * @param id           The component ID
     * @param baseDamage   Base damage per tick
     * @param tickInterval Tick interval (in server ticks)
     */
    public SunlightDamageComponent(@NotNull String id, double baseDamage, int tickInterval) {
        this(id, baseDamage, tickInterval, true, 0.5);
    }

    /**
     * Create a sunlight damage component with configurable options.
     *
     * @param id                    The component ID
     * @param baseDamage            Base damage per tick
     * @param tickInterval          Tick interval (in server ticks)
     * @param checkWeather          Whether weather (storm) should provide protection
     * @param helmetDamageReduction Damage reduction when wearing helmet (0.0 to 1.0)
     */
    public SunlightDamageComponent(@NotNull String id, double baseDamage, int tickInterval,
                                   boolean checkWeather, double helmetDamageReduction) {
        this.id = id;
        this.baseDamage = baseDamage;
        this.tickInterval = tickInterval;
        this.helmetDamageReduction = helmetDamageReduction;
        this.checkWeather = checkWeather;

        // Build the exposure condition (daytime + sky access + optionally clear weather)
        Condition condition = Conditions.isDay().and(Conditions.hasSkyAccess());
        if (checkWeather) {
            condition = condition.and(Conditions.isClearWeather());
        }
        this.exposureCondition = condition;
    }

    /**
     * Create a sunlight damage component with a custom exposure condition.
     *
     * @param id                    The component ID
     * @param baseDamage            Base damage per tick
     * @param tickInterval          Tick interval (in server ticks)
     * @param exposureCondition     Custom condition for sunlight exposure
     * @param helmetDamageReduction Damage reduction when wearing helmet (0.0 to 1.0)
     */
    public SunlightDamageComponent(@NotNull String id, double baseDamage, int tickInterval,
                                   @NotNull Condition exposureCondition, double helmetDamageReduction) {
        this.id = id;
        this.baseDamage = baseDamage;
        this.tickInterval = tickInterval;
        this.exposureCondition = exposureCondition;
        this.helmetDamageReduction = helmetDamageReduction;
        this.checkWeather = true; // Default when using custom condition
    }

    @Override
    public @NotNull String getId() {
        return id;
    }

    @Override
    public int getTickInterval() {
        return tickInterval;
    }

    @Override
    public void onTick(@NotNull Player player, @NotNull AfflictionInstance instance) {
        if (!exposureCondition.test(player)) {
            instance.setData("burning", false);
            return;
        }

        instance.setData("burning", true);

        // Calculate damage based on level and helmet
        double damage = calculateDamage(player, instance);

        // Apply damage
        player.damage(damage);

        // Visual feedback - set player on fire briefly
        if (player.getFireTicks() < 20) {
            player.setFireTicks(40); // 2 seconds of fire effect
        }
    }

    /**
     * Calculate damage based on affliction level and protection.
     */
    private double calculateDamage(@NotNull Player player, @NotNull AfflictionInstance instance) {
        double damage = baseDamage;

        // Scale damage with level
        int level = instance.getLevel();
        // Higher level = less damage (more control over vampirism)
        // Level 1: full damage, Level 5: 60% damage
        double levelReduction = 1.0 - ((level - 1) * 0.1);
        damage *= levelReduction;

        // Helmet provides damage reduction
        if (Conditions.hasHelmet().test(player)) {
            damage *= (1.0 - helmetDamageReduction);
            instance.setData("has_helmet", true);
        } else {
            instance.setData("has_helmet", false);
        }

        return Math.max(0.5, damage); // Minimum 0.5 damage
    }

    /**
     * Get the base damage per tick.
     *
     * @return The base damage
     */
    public double getBaseDamage() {
        return baseDamage;
    }

    /**
     * Get the condition used for sunlight exposure checks.
     *
     * @return The exposure condition
     */
    public @NotNull Condition getExposureCondition() {
        return exposureCondition;
    }

    /**
     * Get the helmet damage reduction multiplier.
     *
     * @return The damage reduction (0.0 to 1.0)
     */
    public double getHelmetDamageReduction() {
        return helmetDamageReduction;
    }

    /**
     * Check if weather protection is enabled.
     *
     * @return true if storm/rain provides protection from sunlight
     */
    public boolean isCheckWeather() {
        return checkWeather;
    }
}
