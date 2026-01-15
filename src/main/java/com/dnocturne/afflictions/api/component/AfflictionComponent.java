package com.dnocturne.afflictions.api.component;

import com.dnocturne.afflictions.api.affliction.AfflictionInstance;
import org.bukkit.entity.Player;

/**
 * Base interface for all affliction components.
 * Components are reusable building blocks that define affliction behavior.
 */
public interface AfflictionComponent {

    /**
     * @return Unique identifier for this component type
     */
    String getId();

    /**
     * Called when the affliction is first applied to a player.
     *
     * @param player   The player
     * @param instance The affliction instance
     */
    default void onApply(Player player, AfflictionInstance instance) {
    }

    /**
     * Called when the affliction is removed from a player.
     *
     * @param player   The player
     * @param instance The affliction instance
     */
    default void onRemove(Player player, AfflictionInstance instance) {
    }
}
