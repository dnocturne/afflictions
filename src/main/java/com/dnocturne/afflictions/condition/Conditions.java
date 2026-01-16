package com.dnocturne.afflictions.condition;

import com.dnocturne.afflictions.util.TimeUtil;
import com.dnocturne.afflictions.util.TimeUtil.MoonPhase;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.EnumSet;
import java.util.Set;

/**
 * Factory class providing built-in conditions.
 *
 * <p>All conditions are designed to be composable using
 * {@link Condition#and}, {@link Condition#or}, and {@link Condition#negate}.</p>
 *
 * <p>Example usage:</p>
 * <pre>{@code
 * // Player is exposed to sunlight
 * Condition sunlightExposure = Conditions.isDay()
 *     .and(Conditions.hasSkyAccess())
 *     .and(Conditions.noClearWeather().negate())
 *     .and(Conditions.hasHelmet().negate());
 *
 * // Or use the pre-built sunlight condition
 * Condition sunlight = Conditions.isExposedToSunlight();
 * }</pre>
 */
public final class Conditions {

    private Conditions() {
    }

    // ============================================================
    // Time-based conditions
    // ============================================================

    /**
     * Condition that is true during daytime.
     */
    public static @NotNull Condition isDay() {
        return new DescribedCondition("isDay", player -> TimeUtil.isDay(player.getWorld()));
    }

    /**
     * Condition that is true during nighttime.
     */
    public static @NotNull Condition isNight() {
        return new DescribedCondition("isNight", player -> TimeUtil.isNight(player.getWorld()));
    }

    /**
     * Condition that is true when world time is within the specified range.
     *
     * @param startTick The start of the time range (inclusive)
     * @param endTick   The end of the time range (inclusive)
     */
    public static @NotNull Condition isTimeInRange(long startTick, long endTick) {
        return new DescribedCondition(
                "isTimeInRange(" + startTick + "-" + endTick + ")",
                player -> {
                    long time = player.getWorld().getTime();
                    if (startTick <= endTick) {
                        return time >= startTick && time <= endTick;
                    } else {
                        // Handle wrap-around (e.g., 22000-2000)
                        return time >= startTick || time <= endTick;
                    }
                }
        );
    }

    // ============================================================
    // Moon phase conditions
    // ============================================================

    /**
     * Condition that is true during a full moon.
     */
    public static @NotNull Condition isFullMoon() {
        return new DescribedCondition("isFullMoon", player -> TimeUtil.isFullMoon(player.getWorld()));
    }

    /**
     * Condition that is true during a new moon.
     */
    public static @NotNull Condition isNewMoon() {
        return new DescribedCondition("isNewMoon", player -> TimeUtil.isNewMoon(player.getWorld()));
    }

    /**
     * Condition that is true when the moon phase matches any of the specified phases.
     *
     * @param phases The moon phases to match
     */
    public static @NotNull Condition isMoonPhase(@NotNull MoonPhase... phases) {
        Set<MoonPhase> phaseSet = EnumSet.noneOf(MoonPhase.class);
        for (MoonPhase phase : phases) {
            phaseSet.add(phase);
        }
        return isMoonPhase(phaseSet);
    }

    /**
     * Condition that is true when the moon phase matches any of the specified phases.
     *
     * @param phases The moon phases to match
     */
    public static @NotNull Condition isMoonPhase(@NotNull Set<MoonPhase> phases) {
        String desc = "isMoonPhase(" + phases + ")";
        return new DescribedCondition(desc, player -> {
            MoonPhase current = TimeUtil.getMoonPhaseEnum(player.getWorld());
            return phases.contains(current);
        });
    }

    /**
     * Condition that is true when the moon is bright (>= 50% illumination).
     * Includes: Full Moon, Waning Gibbous, First Quarter, Third Quarter, Waxing Gibbous
     */
    public static @NotNull Condition isBrightMoon() {
        return new DescribedCondition("isBrightMoon", player -> {
            MoonPhase phase = TimeUtil.getMoonPhaseEnum(player.getWorld());
            return phase.isBright();
        });
    }

    /**
     * Condition that is true when the moon is dark (< 50% illumination).
     * Includes: New Moon, Waning Crescent, Waxing Crescent
     */
    public static @NotNull Condition isDarkMoon() {
        return isBrightMoon().negate();
    }

    // ============================================================
    // Environmental conditions
    // ============================================================

    /**
     * Condition that is true when the player has direct sky access (light level 15).
     */
    public static @NotNull Condition hasSkyAccess() {
        return new DescribedCondition("hasSkyAccess", player -> {
            Location loc = player.getLocation();
            return loc.getBlock().getLightFromSky() >= 15;
        });
    }

    /**
     * Condition that is true when the player is underground or under cover.
     */
    public static @NotNull Condition isUnderCover() {
        return hasSkyAccess().negate();
    }

    /**
     * Condition that is true when there is a storm (rain/thunder) in the world.
     */
    public static @NotNull Condition hasStorm() {
        return new DescribedCondition("hasStorm", player -> player.getWorld().hasStorm());
    }

    /**
     * Condition that is true when the weather is clear (no storm).
     */
    public static @NotNull Condition isClearWeather() {
        return hasStorm().negate();
    }

    /**
     * Condition that is true when there is thunder.
     */
    public static @NotNull Condition isThundering() {
        return new DescribedCondition("isThundering", player -> player.getWorld().isThundering());
    }

    // ============================================================
    // Equipment conditions
    // ============================================================

    /**
     * Condition that is true when the player is wearing a helmet.
     */
    public static @NotNull Condition hasHelmet() {
        return new DescribedCondition("hasHelmet",
                player -> player.getInventory().getHelmet() != null);
    }

    /**
     * Condition that is true when the player is NOT wearing a helmet.
     */
    public static @NotNull Condition noHelmet() {
        return hasHelmet().negate();
    }

    // ============================================================
    // World/Environment type conditions
    // ============================================================

    /**
     * Condition that is true when the player is in the overworld.
     */
    public static @NotNull Condition isInOverworld() {
        return new DescribedCondition("isInOverworld",
                player -> player.getWorld().getEnvironment() == World.Environment.NORMAL);
    }

    /**
     * Condition that is true when the player is in the nether.
     */
    public static @NotNull Condition isInNether() {
        return new DescribedCondition("isInNether",
                player -> player.getWorld().getEnvironment() == World.Environment.NETHER);
    }

    /**
     * Condition that is true when the player is in the end.
     */
    public static @NotNull Condition isInEnd() {
        return new DescribedCondition("isInEnd",
                player -> player.getWorld().getEnvironment() == World.Environment.THE_END);
    }

    // ============================================================
    // Composite/common conditions
    // ============================================================

    /**
     * Condition that is true when the player is exposed to sunlight.
     * This is a composite condition checking:
     * - Daytime
     * - Has sky access (light level 15)
     * - No storm (weather protection)
     * - No helmet
     */
    public static @NotNull Condition isExposedToSunlight() {
        return isDay()
                .and(hasSkyAccess())
                .and(isClearWeather())
                .and(noHelmet());
    }

    /**
     * Condition that is true when the player is protected from sunlight.
     * Any of these provide protection:
     * - Nighttime
     * - Under cover (no sky access)
     * - Storm (weather protection)
     * - Wearing helmet
     */
    public static @NotNull Condition isProtectedFromSunlight() {
        return isExposedToSunlight().negate();
    }

    /**
     * Condition that is true during a night with a full moon.
     */
    public static @NotNull Condition isFullMoonNight() {
        return isNight().and(isFullMoon());
    }

    /**
     * Condition that is true during a night with a bright moon (>= 50% illumination).
     */
    public static @NotNull Condition isBrightMoonNight() {
        return isNight().and(isBrightMoon());
    }

    // ============================================================
    // Internal helper class
    // ============================================================

    /**
     * A condition with a custom description.
     */
    private record DescribedCondition(
            @NotNull String description,
            @NotNull Condition delegate
    ) implements Condition {

        @Override
        public boolean test(@NotNull Player player) {
            return delegate.test(player);
        }

        @Override
        public @NotNull String describe() {
            return description;
        }
    }
}
