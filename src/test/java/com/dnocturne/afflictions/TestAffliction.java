package com.dnocturne.afflictions;

import com.dnocturne.afflictions.api.affliction.AbstractAffliction;
import com.dnocturne.afflictions.api.affliction.AfflictionCategory;
import com.dnocturne.afflictions.api.affliction.AfflictionInstance;
import com.dnocturne.basalt.component.Component;
import org.bukkit.entity.Player;

/**
 * Simple test affliction for unit tests.
 */
public class TestAffliction extends AbstractAffliction {

    public static final String DEFAULT_ID = "test_affliction";

    private TestAffliction(Builder builder) {
        super(builder);
    }

    public static TestAffliction create() {
        return new Builder(DEFAULT_ID).build();
    }

    public static TestAffliction create(String id) {
        return new Builder(id).build();
    }

    public static Builder builder(String id) {
        return new Builder(id);
    }

    public static class Builder extends AbstractAffliction.Builder {

        public Builder(String id) {
            super(id);
            this.displayName = "Test Affliction";
            this.description = "A test affliction for unit tests.";
            this.category = AfflictionCategory.SUPERNATURAL;
            this.maxLevel = 5;
            this.curable = true;
        }

        @Override
        public Builder displayName(String displayName) {
            super.displayName(displayName);
            return this;
        }

        @Override
        public Builder description(String description) {
            super.description(description);
            return this;
        }

        @Override
        public Builder category(AfflictionCategory category) {
            super.category(category);
            return this;
        }

        @Override
        public Builder maxLevel(int maxLevel) {
            super.maxLevel(maxLevel);
            return this;
        }

        @Override
        public Builder curable(boolean curable) {
            super.curable(curable);
            return this;
        }

        @Override
        public Builder component(Component<Player, AfflictionInstance> component) {
            super.component(component);
            return this;
        }

        @Override
        public TestAffliction build() {
            return new TestAffliction(this);
        }
    }
}
