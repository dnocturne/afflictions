package com.dnocturne.afflictions.affliction.config.curse;

import com.dnocturne.basalt.condition.Condition;
import com.dnocturne.basalt.condition.PlayerConditions;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

/**
 * Factory for creating conditions from trigger configurations.
 */
public final class TriggerFactory {

    private TriggerFactory() {
    }

    /**
     * Create a condition from a trigger config.
     *
     * @param config The trigger config
     * @return The condition
     */
    public static @NotNull Condition<Player> fromConfig(@NotNull TriggerConfig config) {
        Condition<Player> condition = fromType(config.getType());
        return config.isInverted() ? condition.negate() : condition;
    }

    /**
     * Create a condition from a trigger type string.
     *
     * @param type The trigger type
     * @return The condition
     */
    public static @NotNull Condition<Player> fromType(@NotNull String type) {
        return switch (type.toLowerCase()) {
            // Always/Never
            case "always" -> Condition.always();
            case "never" -> Condition.never();

            // Time-based
            case "day" -> PlayerConditions.isDay();
            case "night" -> PlayerConditions.isNight();

            // Moon phase-based
            case "full_moon" -> PlayerConditions.isFullMoon();
            case "new_moon" -> PlayerConditions.isNewMoon();
            case "bright_moon" -> PlayerConditions.isBrightMoon();
            case "dark_moon" -> PlayerConditions.isDarkMoon();
            case "full_moon_night" -> PlayerConditions.isFullMoonNight();
            case "bright_moon_night" -> PlayerConditions.isBrightMoonNight();

            // Sunlight/Cover
            case "sunlight" -> PlayerConditions.isExposedToSunlight();
            case "underground", "under_cover" -> PlayerConditions.isUnderCover();
            case "sky_access" -> PlayerConditions.hasSkyAccess();
            case "protected_from_sun" -> PlayerConditions.isProtectedFromSunlight();

            // Weather
            case "rain", "storm" -> PlayerConditions.hasStorm();
            case "thunder", "thunderstorm" -> PlayerConditions.isThundering();
            case "clear", "clear_weather" -> PlayerConditions.isClearWeather();

            // Dimension
            case "overworld" -> PlayerConditions.isInOverworld();
            case "nether" -> PlayerConditions.isInNether();
            case "end" -> PlayerConditions.isInEnd();

            // Armor
            case "has_helmet" -> PlayerConditions.hasHelmet();
            case "no_helmet" -> PlayerConditions.noHelmet();

            // Default: always active
            default -> Condition.always();
        };
    }
}
