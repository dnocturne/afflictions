package com.dnocturne.afflictions.api.affliction;

import com.dnocturne.basalt.component.Component;
import com.dnocturne.basalt.registry.Identifiable;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;

/**
 * Represents an affliction type definition.
 * Afflictions are composed of reusable components that define their behavior.
 */
public interface Affliction extends Identifiable {

    /**
     * @return Unique identifier for this affliction (e.g., "vampirism", "werewolf")
     */
    @NotNull String getId();

    /**
     * @return Display name shown to players
     */
    @NotNull String getDisplayName();

    /**
     * @return Description of this affliction
     */
    @NotNull String getDescription();

    /**
     * @return The category of this affliction
     */
    @NotNull AfflictionCategory getCategory();

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
    @NotNull Collection<Component<Player, AfflictionInstance>> getComponents();

    /**
     * Get a specific component by type.
     *
     * @param componentClass The component class to find
     * @return The component, or null if not present
     */
    <T extends Component<Player, AfflictionInstance>> @Nullable T getComponent(@NotNull Class<T> componentClass);

    /**
     * Check if this affliction has a specific component type.
     *
     * @param componentClass The component class to check for
     * @return true if the component is present
     */
    boolean hasComponent(@NotNull Class<? extends Component<Player, AfflictionInstance>> componentClass);
}
