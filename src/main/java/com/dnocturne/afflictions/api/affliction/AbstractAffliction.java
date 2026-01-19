package com.dnocturne.afflictions.api.affliction;

import com.dnocturne.basalt.component.Component;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * Base implementation of Affliction with component composition.
 */
public abstract class AbstractAffliction implements Affliction {

    private final String id;
    private final String displayName;
    private final String description;
    private final AfflictionCategory category;
    private final int maxLevel;
    private final boolean curable;
    private final List<Component<Player, AfflictionInstance>> components;

    protected AbstractAffliction(Builder builder) {
        this.id = builder.id;
        this.displayName = builder.displayName;
        this.description = builder.description;
        this.category = builder.category;
        this.maxLevel = builder.maxLevel;
        this.curable = builder.curable;
        this.components = new ArrayList<>(builder.components);
    }

    @Override
    public @NotNull String getId() {
        return id;
    }

    @Override
    public @NotNull String getDisplayName() {
        return displayName;
    }

    @Override
    public @NotNull String getDescription() {
        return description;
    }

    @Override
    public @NotNull AfflictionCategory getCategory() {
        return category;
    }

    @Override
    public int getMaxLevel() {
        return maxLevel;
    }

    @Override
    public boolean isCurable() {
        return curable;
    }

    @Override
    public @NotNull Collection<Component<Player, AfflictionInstance>> getComponents() {
        return Collections.unmodifiableList(components);
    }

    @Override
    public <T extends Component<Player, AfflictionInstance>> @Nullable T getComponent(@NotNull Class<T> componentClass) {
        for (Component<Player, AfflictionInstance> component : components) {
            if (componentClass.isInstance(component)) {
                return componentClass.cast(component);
            }
        }
        return null;
    }

    @Override
    public boolean hasComponent(@NotNull Class<? extends Component<Player, AfflictionInstance>> componentClass) {
        return getComponent(componentClass) != null;
    }

    /**
     * Add a component to this affliction.
     */
    protected void addComponent(@NotNull Component<Player, AfflictionInstance> component) {
        components.add(component);
    }

    /**
     * Builder for creating afflictions.
     */
    public static abstract class Builder {
        protected String id;
        protected String displayName;
        protected String description = "";
        protected AfflictionCategory category = AfflictionCategory.SUPERNATURAL;
        protected int maxLevel = 5;
        protected boolean curable = true;
        protected List<Component<Player, AfflictionInstance>> components = new ArrayList<>();

        public Builder(@NotNull String id) {
            this.id = id;
            this.displayName = id;
        }

        public @NotNull Builder displayName(@NotNull String displayName) {
            this.displayName = displayName;
            return this;
        }

        public @NotNull Builder description(@NotNull String description) {
            this.description = description;
            return this;
        }

        public @NotNull Builder category(@NotNull AfflictionCategory category) {
            this.category = category;
            return this;
        }

        public @NotNull Builder maxLevel(int maxLevel) {
            if (maxLevel <= 0) {
                throw new IllegalArgumentException("maxLevel must be positive, got: " + maxLevel);
            }
            this.maxLevel = maxLevel;
            return this;
        }

        public @NotNull Builder curable(boolean curable) {
            this.curable = curable;
            return this;
        }

        public @NotNull Builder component(@NotNull Component<Player, AfflictionInstance> component) {
            this.components.add(component);
            return this;
        }

        /**
         * Validate builder fields before building.
         * Subclasses should call this at the start of their build() method.
         *
         * @throws IllegalStateException if required fields are missing or invalid
         */
        protected void validate() {
            if (id == null || id.isBlank()) {
                throw new IllegalStateException("Affliction id cannot be null or blank");
            }
            if (displayName == null || displayName.isBlank()) {
                throw new IllegalStateException("Affliction displayName cannot be null or blank");
            }
            if (category == null) {
                throw new IllegalStateException("Affliction category cannot be null");
            }
            if (maxLevel <= 0) {
                throw new IllegalStateException("Affliction maxLevel must be positive, got: " + maxLevel);
            }
        }

        public abstract @NotNull Affliction build();
    }
}
