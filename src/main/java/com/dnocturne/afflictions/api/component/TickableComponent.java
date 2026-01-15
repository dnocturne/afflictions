package com.dnocturne.afflictions.api.component;

import com.dnocturne.afflictions.api.affliction.AfflictionInstance;
import org.bukkit.entity.Player;

/**
 * A component that processes logic each tick.
 */
public interface TickableComponent extends AfflictionComponent {

    /**
     * Called every affliction tick while active.
     *
     * @param player   The afflicted player
     * @param instance The affliction instance
     */
    void onTick(Player player, AfflictionInstance instance);

    /**
     * @return How often this component should tick (in server ticks).
     *         Default is every affliction tick (1).
     */
    default int getTickInterval() {
        return 1;
    }
}
