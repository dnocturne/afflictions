package com.dnocturne.afflictions.component.trigger;

import com.dnocturne.afflictions.api.affliction.AfflictionInstance;
import com.dnocturne.afflictions.api.component.trigger.Trigger;
import com.dnocturne.afflictions.util.TimeUtil;
import org.bukkit.entity.Player;

/**
 * Trigger based on time of day.
 * Can trigger during day, night, or a custom time range.
 */
public class TimeTrigger implements Trigger {

    private final String id;
    private final TimeCondition condition;
    private final long customStart;
    private final long customEnd;

    public TimeTrigger(String id, TimeCondition condition) {
        this(id, condition, 0, 0);
    }

    public TimeTrigger(String id, long customStart, long customEnd) {
        this(id, TimeCondition.CUSTOM, customStart, customEnd);
    }

    private TimeTrigger(String id, TimeCondition condition, long customStart, long customEnd) {
        this.id = id;
        this.condition = condition;
        this.customStart = customStart;
        this.customEnd = customEnd;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public boolean isActive(Player player, AfflictionInstance instance) {
        return switch (condition) {
            case DAY -> TimeUtil.isDay(player.getWorld());
            case NIGHT -> TimeUtil.isNight(player.getWorld());
            case CUSTOM -> {
                long time = player.getWorld().getTime();
                yield time >= customStart && time <= customEnd;
            }
        };
    }

    public TimeCondition getCondition() {
        return condition;
    }

    public enum TimeCondition {
        DAY,
        NIGHT,
        CUSTOM
    }
}
