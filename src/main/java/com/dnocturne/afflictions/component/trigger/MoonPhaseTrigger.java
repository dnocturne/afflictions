package com.dnocturne.afflictions.component.trigger;

import com.dnocturne.afflictions.api.affliction.AfflictionInstance;
import com.dnocturne.afflictions.api.component.trigger.Trigger;
import com.dnocturne.afflictions.util.TimeUtil;
import com.dnocturne.afflictions.util.TimeUtil.MoonPhase;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.Set;

/**
 * Trigger based on moon phase.
 * Useful for werewolf transformations, etc.
 */
public class MoonPhaseTrigger implements Trigger {

    private final String id;
    private final Set<MoonPhase> activePhases;

    /**
     * Create a trigger for specific moon phases.
     *
     * @param id           Component ID
     * @param activePhases Set of moon phases to trigger on
     */
    public MoonPhaseTrigger(String id, Set<MoonPhase> activePhases) {
        this.id = id;
        this.activePhases = EnumSet.copyOf(activePhases);
    }

    /**
     * Create a trigger for specific moon phases (varargs).
     */
    public MoonPhaseTrigger(String id, MoonPhase... phases) {
        this(id, EnumSet.copyOf(Arrays.asList(phases)));
    }

    /**
     * Create a full moon trigger.
     */
    public static MoonPhaseTrigger fullMoon(String id) {
        return new MoonPhaseTrigger(id, MoonPhase.FULL_MOON);
    }

    /**
     * Create a new moon trigger.
     */
    public static MoonPhaseTrigger newMoon(String id) {
        return new MoonPhaseTrigger(id, MoonPhase.NEW_MOON);
    }

    /**
     * Create a trigger for bright moon phases (>= 50% illumination).
     * Includes: Full Moon, Waning Gibbous, Third Quarter, First Quarter, Waxing Gibbous
     */
    public static MoonPhaseTrigger brightMoon(String id) {
        return new MoonPhaseTrigger(id,
                MoonPhase.FULL_MOON,
                MoonPhase.WANING_GIBBOUS,
                MoonPhase.THIRD_QUARTER,
                MoonPhase.FIRST_QUARTER,
                MoonPhase.WAXING_GIBBOUS
        );
    }

    /**
     * Create a trigger for dark moon phases (< 50% illumination).
     * Includes: Waning Crescent, New Moon, Waxing Crescent
     */
    public static MoonPhaseTrigger darkMoon(String id) {
        return new MoonPhaseTrigger(id,
                MoonPhase.WANING_CRESCENT,
                MoonPhase.NEW_MOON,
                MoonPhase.WAXING_CRESCENT
        );
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public boolean isActive(Player player, AfflictionInstance instance) {
        MoonPhase currentPhase = TimeUtil.getMoonPhaseEnum(player.getWorld());
        return activePhases.contains(currentPhase);
    }

    /**
     * Get the current moon phase for a player.
     */
    public MoonPhase getCurrentPhase(Player player) {
        return TimeUtil.getMoonPhaseEnum(player.getWorld());
    }

    /**
     * Get the moon brightness for a player (0.0 to 1.0).
     */
    public float getCurrentBrightness(Player player) {
        return getCurrentPhase(player).getBrightness();
    }

    public Set<MoonPhase> getActivePhases() {
        return EnumSet.copyOf(activePhases);
    }
}
