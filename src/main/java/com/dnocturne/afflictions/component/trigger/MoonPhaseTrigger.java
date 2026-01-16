package com.dnocturne.afflictions.component.trigger;

import com.dnocturne.afflictions.api.affliction.AfflictionInstance;
import com.dnocturne.afflictions.api.component.trigger.Trigger;
import com.dnocturne.afflictions.condition.Condition;
import com.dnocturne.afflictions.condition.Conditions;
import com.dnocturne.afflictions.util.TimeUtil;
import com.dnocturne.afflictions.util.TimeUtil.MoonPhase;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.Set;

/**
 * Trigger based on moon phase.
 * Useful for werewolf transformations, etc.
 *
 * <p>Uses the Condition system for reusable moon phase checks.</p>
 */
public class MoonPhaseTrigger implements Trigger {

    private final String id;
    private final Set<MoonPhase> activePhases;
    private final Condition condition;

    /**
     * Create a trigger for specific moon phases.
     *
     * @param id           Component ID
     * @param activePhases Set of moon phases to trigger on
     */
    public MoonPhaseTrigger(@NotNull String id, @NotNull Set<MoonPhase> activePhases) {
        this.id = id;
        this.activePhases = EnumSet.copyOf(activePhases);
        this.condition = Conditions.isMoonPhase(this.activePhases);
    }

    /**
     * Create a trigger for specific moon phases (varargs).
     */
    public MoonPhaseTrigger(@NotNull String id, @NotNull MoonPhase... phases) {
        this(id, EnumSet.copyOf(Arrays.asList(phases)));
    }

    /**
     * Create a trigger with a custom condition.
     *
     * @param id        Component ID
     * @param condition The condition to use
     */
    public MoonPhaseTrigger(@NotNull String id, @NotNull Condition condition) {
        this.id = id;
        this.activePhases = EnumSet.noneOf(MoonPhase.class);
        this.condition = condition;
    }

    /**
     * Create a full moon trigger.
     */
    public static @NotNull MoonPhaseTrigger fullMoon(@NotNull String id) {
        return new MoonPhaseTrigger(id, MoonPhase.FULL_MOON);
    }

    /**
     * Create a new moon trigger.
     */
    public static @NotNull MoonPhaseTrigger newMoon(@NotNull String id) {
        return new MoonPhaseTrigger(id, MoonPhase.NEW_MOON);
    }

    /**
     * Create a trigger for bright moon phases (>= 50% illumination).
     * Includes: Full Moon, Waning Gibbous, Third Quarter, First Quarter, Waxing Gibbous
     */
    public static @NotNull MoonPhaseTrigger brightMoon(@NotNull String id) {
        return new MoonPhaseTrigger(id, Conditions.isBrightMoon());
    }

    /**
     * Create a trigger for dark moon phases (< 50% illumination).
     * Includes: Waning Crescent, New Moon, Waxing Crescent
     */
    public static @NotNull MoonPhaseTrigger darkMoon(@NotNull String id) {
        return new MoonPhaseTrigger(id, Conditions.isDarkMoon());
    }

    @Override
    public @NotNull String getId() {
        return id;
    }

    @Override
    public boolean isActive(@NotNull Player player, @NotNull AfflictionInstance instance) {
        return condition.test(player);
    }

    /**
     * Get the current moon phase for a player.
     */
    public @NotNull MoonPhase getCurrentPhase(@NotNull Player player) {
        return TimeUtil.getMoonPhaseEnum(player.getWorld());
    }

    /**
     * Get the moon brightness for a player (0.0 to 1.0).
     */
    public float getCurrentBrightness(@NotNull Player player) {
        return getCurrentPhase(player).getBrightness();
    }

    /**
     * Get the active moon phases for this trigger.
     *
     * @return Set of active phases (may be empty if using a custom condition)
     */
    public @NotNull Set<MoonPhase> getActivePhases() {
        return EnumSet.copyOf(activePhases);
    }

    /**
     * Get the condition used for moon phase checks.
     *
     * @return The condition
     */
    public @NotNull Condition getCondition() {
        return condition;
    }
}
