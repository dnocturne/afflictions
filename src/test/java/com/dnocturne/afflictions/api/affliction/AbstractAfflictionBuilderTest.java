package com.dnocturne.afflictions.api.affliction;

import com.dnocturne.basalt.component.Component;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Collection;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for AbstractAffliction.Builder validation logic.
 */
@DisplayName("AbstractAffliction.Builder")
class AbstractAfflictionBuilderTest {

    @Nested
    @DisplayName("Validation")
    class Validation {

        @Test
        @DisplayName("Build succeeds with valid configuration")
        void build_validConfig_succeeds() {
            Affliction result = new TestAffliction.Builder("valid_id")
                    .displayName("Valid Name")
                    .description("A valid description")
                    .category(AfflictionCategory.PHYSICAL)
                    .maxLevel(10)
                    .curable(false)
                    .build();

            assertNotNull(result);
            assertEquals("valid_id", result.getId());
            assertEquals("Valid Name", result.getDisplayName());
            assertEquals("A valid description", result.getDescription());
            assertEquals(AfflictionCategory.PHYSICAL, result.getCategory());
            assertEquals(10, result.getMaxLevel());
            assertFalse(result.isCurable());
        }

        @Test
        @DisplayName("Build fails with null ID")
        void build_nullId_throws() {
            IllegalStateException ex = assertThrows(
                    IllegalStateException.class,
                    () -> new TestAffliction.Builder(null)
                            .displayName("Name")
                            .build()
            );

            assertTrue(ex.getMessage().contains("id"));
        }

        @Test
        @DisplayName("Build fails with blank ID")
        void build_blankId_throws() {
            IllegalStateException ex = assertThrows(
                    IllegalStateException.class,
                    () -> new TestAffliction.Builder("   ")
                            .displayName("Name")
                            .build()
            );

            assertTrue(ex.getMessage().contains("id"));
        }

        @Test
        @DisplayName("Build fails with empty ID")
        void build_emptyId_throws() {
            IllegalStateException ex = assertThrows(
                    IllegalStateException.class,
                    () -> new TestAffliction.Builder("")
                            .displayName("Name")
                            .build()
            );

            assertTrue(ex.getMessage().contains("id"));
        }

        @Test
        @DisplayName("Build fails with null displayName")
        void build_nullDisplayName_throws() {
            IllegalStateException ex = assertThrows(
                    IllegalStateException.class,
                    () -> new TestAffliction.Builder("valid_id")
                            .displayName(null)
                            .build()
            );

            assertTrue(ex.getMessage().contains("displayName"));
        }

        @Test
        @DisplayName("Build fails with blank displayName")
        void build_blankDisplayName_throws() {
            IllegalStateException ex = assertThrows(
                    IllegalStateException.class,
                    () -> new TestAffliction.Builder("valid_id")
                            .displayName("   ")
                            .build()
            );

            assertTrue(ex.getMessage().contains("displayName"));
        }

        @Test
        @DisplayName("Build fails with null category")
        void build_nullCategory_throws() {
            IllegalStateException ex = assertThrows(
                    IllegalStateException.class,
                    () -> new TestAffliction.Builder("valid_id")
                            .displayName("Name")
                            .category(null)
                            .build()
            );

            assertTrue(ex.getMessage().contains("category"));
        }

        @Test
        @DisplayName("maxLevel() setter throws for zero value")
        void maxLevel_zero_throwsImmediately() {
            IllegalArgumentException ex = assertThrows(
                    IllegalArgumentException.class,
                    () -> new TestAffliction.Builder("valid_id")
                            .maxLevel(0)
            );

            assertTrue(ex.getMessage().contains("maxLevel"));
        }

        @Test
        @DisplayName("maxLevel() setter throws for negative value")
        void maxLevel_negative_throwsImmediately() {
            IllegalArgumentException ex = assertThrows(
                    IllegalArgumentException.class,
                    () -> new TestAffliction.Builder("valid_id")
                            .maxLevel(-1)
            );

            assertTrue(ex.getMessage().contains("maxLevel"));
        }
    }

    @Nested
    @DisplayName("Default Values")
    class DefaultValues {

        @Test
        @DisplayName("Default description is empty string")
        void defaultDescription_isEmpty() {
            Affliction result = new TestAffliction.Builder("test")
                    .displayName("Test")
                    .build();

            assertEquals("", result.getDescription());
        }

        @Test
        @DisplayName("Default category is SUPERNATURAL")
        void defaultCategory_isSupernatural() {
            Affliction result = new TestAffliction.Builder("test")
                    .displayName("Test")
                    .build();

            assertEquals(AfflictionCategory.SUPERNATURAL, result.getCategory());
        }

        @Test
        @DisplayName("Default maxLevel is 5")
        void defaultMaxLevel_isFive() {
            Affliction result = new TestAffliction.Builder("test")
                    .displayName("Test")
                    .build();

            assertEquals(5, result.getMaxLevel());
        }

        @Test
        @DisplayName("Default curable is true")
        void defaultCurable_isTrue() {
            Affliction result = new TestAffliction.Builder("test")
                    .displayName("Test")
                    .build();

            assertTrue(result.isCurable());
        }

        @Test
        @DisplayName("Default displayName equals id")
        void defaultDisplayName_equalsId() {
            // Create with minimal settings to test default displayName
            Affliction result = new TestAffliction.Builder("my_test_id")
                    .build();

            assertEquals("my_test_id", result.getDisplayName());
        }
    }

    @Nested
    @DisplayName("Components")
    class Components {

        @Test
        @DisplayName("Can add components via builder")
        void component_addsToList() {
            TestComponent component = new TestComponent("test_component");

            Affliction result = new TestAffliction.Builder("test")
                    .displayName("Test")
                    .component(component)
                    .build();

            Collection<Component<Player, AfflictionInstance>> components = result.getComponents();
            assertEquals(1, components.size());
            assertTrue(components.contains(component));
        }

        @Test
        @DisplayName("Can add multiple components")
        void component_multipleComponents() {
            TestComponent first = new TestComponent("first");
            TestComponent second = new TestComponent("second");

            Affliction result = new TestAffliction.Builder("test")
                    .displayName("Test")
                    .component(first)
                    .component(second)
                    .build();

            Collection<Component<Player, AfflictionInstance>> components = result.getComponents();
            assertEquals(2, components.size());
        }

        @Test
        @DisplayName("getComponent returns matching component")
        void getComponent_returnsMatch() {
            TestComponent component = new TestComponent("my_component");

            Affliction result = new TestAffliction.Builder("test")
                    .displayName("Test")
                    .component(component)
                    .build();

            TestComponent retrieved = result.getComponent(TestComponent.class);
            assertSame(component, retrieved);
        }

        @Test
        @DisplayName("getComponent returns null for missing component")
        void getComponent_missingReturnsNull() {
            Affliction result = new TestAffliction.Builder("test")
                    .displayName("Test")
                    .build();

            TestComponent retrieved = result.getComponent(TestComponent.class);
            assertNull(retrieved);
        }

        @Test
        @DisplayName("hasComponent returns true when present")
        void hasComponent_present_returnsTrue() {
            Affliction result = new TestAffliction.Builder("test")
                    .displayName("Test")
                    .component(new TestComponent("comp"))
                    .build();

            assertTrue(result.hasComponent(TestComponent.class));
        }

        @Test
        @DisplayName("hasComponent returns false when absent")
        void hasComponent_absent_returnsFalse() {
            Affliction result = new TestAffliction.Builder("test")
                    .displayName("Test")
                    .build();

            assertFalse(result.hasComponent(TestComponent.class));
        }

        @Test
        @DisplayName("getComponents returns unmodifiable collection")
        void getComponents_unmodifiable() {
            Affliction result = new TestAffliction.Builder("test")
                    .displayName("Test")
                    .component(new TestComponent("comp"))
                    .build();

            Collection<Component<Player, AfflictionInstance>> components = result.getComponents();
            assertThrows(UnsupportedOperationException.class, components::clear);
        }
    }

    @Nested
    @DisplayName("Builder Fluency")
    class BuilderFluency {

        @Test
        @DisplayName("Builder methods return the builder instance")
        void builderMethods_returnBuilder() {
            AbstractAffliction.Builder builder = new TestAffliction.Builder("test");

            assertSame(builder, builder.displayName("Name"));
            assertSame(builder, builder.description("Desc"));
            assertSame(builder, builder.category(AfflictionCategory.PHYSICAL));
            assertSame(builder, builder.maxLevel(3));
            assertSame(builder, builder.curable(false));
            assertSame(builder, builder.component(new TestComponent("c")));
        }
    }

    /**
     * Simple test affliction implementation.
     */
    private static class TestAffliction extends AbstractAffliction {

        private TestAffliction(Builder builder) {
            super(builder);
        }

        public static class Builder extends AbstractAffliction.Builder {

            public Builder(String id) {
                super(id);
            }

            @Override
            public @NotNull TestAffliction build() {
                validate();
                return new TestAffliction(this);
            }
        }
    }

    /**
     * Simple test component implementation.
     */
    private record TestComponent(String id) implements Component<Player, AfflictionInstance> {

        @Override
        public String getId() {
            return id;
        }
    }
}
