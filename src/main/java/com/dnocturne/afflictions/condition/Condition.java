package com.dnocturne.afflictions.condition;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

/**
 * A condition that can be evaluated against a player context.
 *
 * <p>Conditions are reusable, composable predicates that can be combined
 * using logical operators ({@link #and}, {@link #or}, {@link #negate}).</p>
 *
 * <p>Example usage:</p>
 * <pre>{@code
 * Condition sunlightExposure = Conditions.isDay()
 *     .and(Conditions.hasSkyAccess())
 *     .and(Conditions.noWeatherProtection())
 *     .and(Conditions.noHelmet());
 *
 * if (sunlightExposure.test(player)) {
 *     // Player is exposed to sunlight
 * }
 * }</pre>
 *
 * @see Conditions
 * @see CompositeCondition
 */
@FunctionalInterface
public interface Condition {

    /**
     * Test if this condition is met for the given player.
     *
     * @param player The player to test
     * @return true if the condition is met
     */
    boolean test(@NotNull Player player);

    /**
     * Get a human-readable description of this condition.
     *
     * @return The condition description
     */
    default @NotNull String describe() {
        return getClass().getSimpleName();
    }

    /**
     * Combine this condition with another using logical AND.
     *
     * @param other The other condition
     * @return A new condition that is true only if both conditions are true
     */
    default @NotNull Condition and(@NotNull Condition other) {
        return CompositeCondition.and(this, other);
    }

    /**
     * Combine this condition with another using logical OR.
     *
     * @param other The other condition
     * @return A new condition that is true if either condition is true
     */
    default @NotNull Condition or(@NotNull Condition other) {
        return CompositeCondition.or(this, other);
    }

    /**
     * Negate this condition.
     *
     * @return A new condition that is true only if this condition is false
     */
    default @NotNull Condition negate() {
        return CompositeCondition.not(this);
    }

    /**
     * A condition that always returns true.
     */
    Condition ALWAYS = player -> true;

    /**
     * A condition that always returns false.
     */
    Condition NEVER = player -> false;
}
