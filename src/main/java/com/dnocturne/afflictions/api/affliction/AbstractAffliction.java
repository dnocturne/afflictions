package com.dnocturne.afflictions.api.affliction;

import com.dnocturne.afflictions.api.component.AfflictionComponent;
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
    private final List<AfflictionComponent> components;

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
    public @NotNull Collection<AfflictionComponent> getComponents() {
        return Collections.unmodifiableList(components);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends AfflictionComponent> @Nullable T getComponent(@NotNull Class<T> componentClass) {
        for (AfflictionComponent component : components) {
            if (componentClass.isInstance(component)) {
                return (T) component;
            }
        }
        return null;
    }

    @Override
    public boolean hasComponent(@NotNull Class<? extends AfflictionComponent> componentClass) {
        return getComponent(componentClass) != null;
    }

    /**
     * Add a component to this affliction.
     */
    protected void addComponent(@NotNull AfflictionComponent component) {
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
        protected List<AfflictionComponent> components = new ArrayList<>();

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
            this.maxLevel = maxLevel;
            return this;
        }

        public @NotNull Builder curable(boolean curable) {
            this.curable = curable;
            return this;
        }

        public @NotNull Builder component(@NotNull AfflictionComponent component) {
            this.components.add(component);
            return this;
        }

        public abstract @NotNull Affliction build();
    }
}
