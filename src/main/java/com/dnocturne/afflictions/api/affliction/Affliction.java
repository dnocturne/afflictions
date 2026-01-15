package com.dnocturne.afflictions.api.affliction;

import com.dnocturne.afflictions.api.component.AfflictionComponent;

import java.util.Collection;

/**
 * Represents an affliction type definition.
 * Afflictions are composed of reusable components that define their behavior.
 */
public interface Affliction {

    /**
     * @return Unique identifier for this affliction (e.g., "vampirism", "werewolf")
     */
    String getId();

    /**
     * @return Display name shown to players
     */
    String getDisplayName();

    /**
     * @return Description of this affliction
     */
    String getDescription();

    /**
     * @return The category of this affliction
     */
    AfflictionCategory getCategory();

    /**
     * @return Maximum level this affliction can reach
     */
    int getMaxLevel();

    /**
     * @return Whether this affliction can be cured
     */
    boolean isCurable();

    /**
     * @return All components that make up this affliction's behavior
     */
    Collection<AfflictionComponent> getComponents();

    /**
     * Get a specific component by type.
     *
     * @param componentClass The component class to find
     * @return The component, or null if not present
     */
    <T extends AfflictionComponent> T getComponent(Class<T> componentClass);

    /**
     * Check if this affliction has a specific component type.
     *
     * @param componentClass The component class to check for
     * @return true if the component is present
     */
    boolean hasComponent(Class<? extends AfflictionComponent> componentClass);
}
