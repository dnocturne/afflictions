package com.dnocturne.afflictions.api.component;

import com.dnocturne.afflictions.api.affliction.AfflictionInstance;
import org.bukkit.entity.Player;

/**
 * A component that only activates under certain conditions.
 * Used for triggers like time-of-day, moon phase, health thresholds, etc.
 */
public interface ConditionalComponent extends AfflictionComponent {

    /**
     * Check if this component's conditions are currently met.
     *
     * @param player   The afflicted player
     * @param instance The affliction instance
     * @return true if conditions are met
     */
    boolean isActive(Player player, AfflictionInstance instance);

    /**
     * Called when conditions become true (transition from inactive to active).
     *
     * @param player   The player
     * @param instance The affliction instance
     */
    default void onActivate(Player player, AfflictionInstance instance) {
    }

    /**
     * Called when conditions become false (transition from active to inactive).
     *
     * @param player   The player
     * @param instance The affliction instance
     */
    default void onDeactivate(Player player, AfflictionInstance instance) {
    }
}
