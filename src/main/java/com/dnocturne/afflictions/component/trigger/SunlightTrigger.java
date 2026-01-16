package com.dnocturne.afflictions.component.trigger;

import com.dnocturne.afflictions.api.affliction.AfflictionInstance;
import com.dnocturne.afflictions.api.component.trigger.Trigger;
import com.dnocturne.afflictions.condition.Condition;
import com.dnocturne.afflictions.condition.Conditions;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

/**
 * Trigger when player is exposed to sunlight.
 * Checks for daytime, sky access, and optionally weather/helmet.
 *
 * <p>Uses the Condition system for reusable environment checks.</p>
 */
public class SunlightTrigger implements Trigger {

    private final String id;
    private final Condition exposureCondition;
    private final boolean trackHelmet;

    /**
     * Create a sunlight trigger with default settings (weather and helmet protection).
     *
     * @param id The trigger ID
     */
    public SunlightTrigger(@NotNull String id) {
        this(id, true, true);
    }

    /**
     * Create a sunlight trigger with configurable weather and helmet checks.
     *
     * @param id           The trigger ID
     * @param checkWeather Whether weather (storm) should provide protection
     * @param checkHelmet  Whether helmet should be tracked for protection calculations
     */
    public SunlightTrigger(@NotNull String id, boolean checkWeather, boolean checkHelmet) {
        this.id = id;
        this.trackHelmet = checkHelmet;

        // Build the exposure condition
        Condition condition = Conditions.isDay().and(Conditions.hasSkyAccess());

        if (checkWeather) {
            condition = condition.and(Conditions.isClearWeather());
        }

        this.exposureCondition = condition;
    }

    /**
     * Create a sunlight trigger with a custom condition.
     *
     * @param id        The trigger ID
     * @param condition The custom condition to use
     */
    public SunlightTrigger(@NotNull String id, @NotNull Condition condition) {
        this.id = id;
        this.exposureCondition = condition;
        this.trackHelmet = false;
    }

    @Override
    public @NotNull String getId() {
        return id;
    }

    @Override
    public boolean isActive(@NotNull Player player, @NotNull AfflictionInstance instance) {
        if (!exposureCondition.test(player)) {
            return false;
        }

        // Track helmet status for damage reduction calculations
        if (trackHelmet) {
            boolean hasHelmet = Conditions.hasHelmet().test(player);
            instance.setData("has_helmet", hasHelmet);
        }

        return true;
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
     * Check if helmet tracking is enabled.
     *
     * @return true if helmet status is tracked
     */
    public boolean isTrackHelmet() {
        return trackHelmet;
    }
}
