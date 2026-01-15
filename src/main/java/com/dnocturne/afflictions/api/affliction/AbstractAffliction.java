package com.dnocturne.afflictions.api.affliction;

import com.dnocturne.afflictions.api.component.AfflictionComponent;

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
    public String getId() {
        return id;
    }

    @Override
    public String getDisplayName() {
        return displayName;
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public AfflictionCategory getCategory() {
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
    public Collection<AfflictionComponent> getComponents() {
        return Collections.unmodifiableList(components);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends AfflictionComponent> T getComponent(Class<T> componentClass) {
        for (AfflictionComponent component : components) {
            if (componentClass.isInstance(component)) {
                return (T) component;
            }
        }
        return null;
    }

    @Override
    public boolean hasComponent(Class<? extends AfflictionComponent> componentClass) {
        return getComponent(componentClass) != null;
    }

    /**
     * Add a component to this affliction.
     */
    protected void addComponent(AfflictionComponent component) {
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

        public Builder(String id) {
            this.id = id;
            this.displayName = id;
        }

        public Builder displayName(String displayName) {
            this.displayName = displayName;
            return this;
        }

        public Builder description(String description) {
            this.description = description;
            return this;
        }

        public Builder category(AfflictionCategory category) {
            this.category = category;
            return this;
        }

        public Builder maxLevel(int maxLevel) {
            this.maxLevel = maxLevel;
            return this;
        }

        public Builder curable(boolean curable) {
            this.curable = curable;
            return this;
        }

        public Builder component(AfflictionComponent component) {
            this.components.add(component);
            return this;
        }

        public abstract Affliction build();
    }
}
