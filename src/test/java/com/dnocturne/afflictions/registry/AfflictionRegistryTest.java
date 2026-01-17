package com.dnocturne.afflictions.registry;

import com.dnocturne.afflictions.api.affliction.Affliction;
import com.dnocturne.afflictions.api.affliction.AbstractAffliction;
import com.dnocturne.afflictions.api.affliction.AfflictionCategory;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Collection;
import java.util.Optional;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for AfflictionRegistry - registration, collision detection, and lookup.
 */
@DisplayName("AfflictionRegistry")
class AfflictionRegistryTest {

    private AfflictionRegistry registry;
    private Logger testLogger;

    @BeforeEach
    void setUp() {
        registry = new AfflictionRegistry();
        testLogger = Logger.getLogger("TestRegistry");
        registry.setLogger(testLogger);
    }

    @Nested
    @DisplayName("Registration")
    class Registration {

        @Test
        @DisplayName("Can register a new affliction")
        void register_newAffliction_succeeds() {
            Affliction affliction = createTestAffliction("test_affliction", "Test Affliction");

            assertDoesNotThrow(() -> registry.register(affliction));
            assertTrue(registry.isRegistered("test_affliction"));
        }

        @Test
        @DisplayName("Registration stores affliction correctly")
        void register_storesAffliction() {
            Affliction affliction = createTestAffliction("stored_test", "Stored Test");

            registry.register(affliction);

            Optional<Affliction> retrieved = registry.get("stored_test");
            assertTrue(retrieved.isPresent());
            assertSame(affliction, retrieved.get());
        }

        @Test
        @DisplayName("Can register multiple different afflictions")
        void register_multipleAfflictions_succeeds() {
            Affliction first = createTestAffliction("first", "First");
            Affliction second = createTestAffliction("second", "Second");
            Affliction third = createTestAffliction("third", "Third");

            registry.register(first);
            registry.register(second);
            registry.register(third);

            assertEquals(3, registry.getAll().size());
            assertTrue(registry.isRegistered("first"));
            assertTrue(registry.isRegistered("second"));
            assertTrue(registry.isRegistered("third"));
        }
    }

    @Nested
    @DisplayName("ID Collision Detection")
    class CollisionDetection {

        @Test
        @DisplayName("Throws exception on duplicate ID registration")
        void register_duplicateId_throwsException() {
            Affliction first = createTestAffliction("duplicate", "First One");
            Affliction second = createTestAffliction("duplicate", "Second One");

            registry.register(first);

            IllegalArgumentException ex = assertThrows(
                    IllegalArgumentException.class,
                    () -> registry.register(second)
            );

            assertTrue(ex.getMessage().contains("duplicate"));
            assertTrue(ex.getMessage().contains("already registered"));
        }

        @Test
        @DisplayName("ID collision is case-insensitive")
        void register_caseInsensitiveDuplicate_throwsException() {
            Affliction lower = createTestAffliction("myaffliction", "Lower Case");
            Affliction upper = createTestAffliction("MYAFFLICTION", "Upper Case");

            registry.register(lower);

            IllegalArgumentException ex = assertThrows(
                    IllegalArgumentException.class,
                    () -> registry.register(upper)
            );

            assertTrue(ex.getMessage().contains("case-insensitive"));
        }

        @Test
        @DisplayName("Mixed case IDs are normalized to lowercase")
        void register_mixedCase_normalizedToLowercase() {
            Affliction affliction = createTestAffliction("MixedCaseId", "Mixed Case");

            registry.register(affliction);

            assertTrue(registry.isRegistered("mixedcaseid"));
            assertTrue(registry.isRegistered("MIXEDCASEID"));
            assertTrue(registry.isRegistered("MixedCaseId"));
        }

        @Test
        @DisplayName("Error message includes both affliction names")
        void register_collision_errorIncludesNames() {
            Affliction first = createTestAffliction("collision_test", "Original Name");
            Affliction second = createTestAffliction("collision_test", "Conflicting Name");

            registry.register(first);

            IllegalArgumentException ex = assertThrows(
                    IllegalArgumentException.class,
                    () -> registry.register(second)
            );

            assertTrue(ex.getMessage().contains("Original Name"));
            assertTrue(ex.getMessage().contains("Conflicting Name"));
        }

        @Test
        @DisplayName("Error message includes class names for debugging")
        void register_collision_errorIncludesClassNames() {
            Affliction first = createTestAffliction("class_test", "First");
            Affliction second = createTestAffliction("class_test", "Second");

            registry.register(first);

            IllegalArgumentException ex = assertThrows(
                    IllegalArgumentException.class,
                    () -> registry.register(second)
            );

            assertTrue(ex.getMessage().contains(first.getClass().getName()));
        }
    }

    @Nested
    @DisplayName("Lookup")
    class Lookup {

        @Test
        @DisplayName("get() returns Optional.empty() for non-existent ID")
        void get_nonExistent_returnsEmpty() {
            Optional<Affliction> result = registry.get("does_not_exist");
            assertTrue(result.isEmpty());
        }

        @Test
        @DisplayName("get() is case-insensitive")
        void get_caseInsensitive() {
            Affliction affliction = createTestAffliction("lookup_test", "Lookup Test");
            registry.register(affliction);

            assertTrue(registry.get("lookup_test").isPresent());
            assertTrue(registry.get("LOOKUP_TEST").isPresent());
            assertTrue(registry.get("Lookup_Test").isPresent());
        }

        @Test
        @DisplayName("isRegistered() returns false for non-existent ID")
        void isRegistered_nonExistent_returnsFalse() {
            assertFalse(registry.isRegistered("nonexistent"));
        }

        @Test
        @DisplayName("isRegistered() is case-insensitive")
        void isRegistered_caseInsensitive() {
            Affliction affliction = createTestAffliction("registered", "Registered");
            registry.register(affliction);

            assertTrue(registry.isRegistered("registered"));
            assertTrue(registry.isRegistered("REGISTERED"));
            assertTrue(registry.isRegistered("ReGiStErEd"));
        }

        @Test
        @DisplayName("getAll() returns all registered afflictions")
        void getAll_returnsAllAfflictions() {
            Affliction a = createTestAffliction("a", "A");
            Affliction b = createTestAffliction("b", "B");
            registry.register(a);
            registry.register(b);

            Collection<Affliction> all = registry.getAll();

            assertEquals(2, all.size());
            assertTrue(all.contains(a));
            assertTrue(all.contains(b));
        }

        @Test
        @DisplayName("getAll() returns unmodifiable collection")
        void getAll_returnsUnmodifiable() {
            Affliction affliction = createTestAffliction("unmod_test", "Test");
            registry.register(affliction);

            Collection<Affliction> all = registry.getAll();

            assertThrows(UnsupportedOperationException.class, () -> all.clear());
        }

        @Test
        @DisplayName("getAllIds() returns all registered IDs")
        void getAllIds_returnsAllIds() {
            registry.register(createTestAffliction("id_one", "One"));
            registry.register(createTestAffliction("id_two", "Two"));

            Collection<String> ids = registry.getAllIds();

            assertEquals(2, ids.size());
            assertTrue(ids.contains("id_one"));
            assertTrue(ids.contains("id_two"));
        }

        @Test
        @DisplayName("getAllIds() returns unmodifiable collection")
        void getAllIds_returnsUnmodifiable() {
            registry.register(createTestAffliction("test", "Test"));

            Collection<String> ids = registry.getAllIds();

            assertThrows(UnsupportedOperationException.class, () -> ids.clear());
        }
    }

    @Nested
    @DisplayName("Unregister")
    class Unregister {

        @Test
        @DisplayName("unregister() removes affliction")
        void unregister_removesAffliction() {
            Affliction affliction = createTestAffliction("to_remove", "To Remove");
            registry.register(affliction);

            boolean removed = registry.unregister("to_remove");

            assertTrue(removed);
            assertFalse(registry.isRegistered("to_remove"));
        }

        @Test
        @DisplayName("unregister() returns false for non-existent ID")
        void unregister_nonExistent_returnsFalse() {
            boolean removed = registry.unregister("never_registered");
            assertFalse(removed);
        }

        @Test
        @DisplayName("unregister() is case-insensitive")
        void unregister_caseInsensitive() {
            registry.register(createTestAffliction("case_test", "Case Test"));

            boolean removed = registry.unregister("CASE_TEST");

            assertTrue(removed);
            assertFalse(registry.isRegistered("case_test"));
        }

        @Test
        @DisplayName("clear() removes all afflictions")
        void clear_removesAllAfflictions() {
            registry.register(createTestAffliction("one", "One"));
            registry.register(createTestAffliction("two", "Two"));
            registry.register(createTestAffliction("three", "Three"));

            registry.clear();

            assertEquals(0, registry.getAll().size());
            assertFalse(registry.isRegistered("one"));
            assertFalse(registry.isRegistered("two"));
            assertFalse(registry.isRegistered("three"));
        }
    }

    @Nested
    @DisplayName("Logger Behavior")
    class LoggerBehavior {

        @Test
        @DisplayName("Works without logger set")
        void register_withoutLogger_works() {
            AfflictionRegistry noLoggerRegistry = new AfflictionRegistry();
            Affliction affliction = createTestAffliction("no_logger", "No Logger");

            assertDoesNotThrow(() -> noLoggerRegistry.register(affliction));
            assertTrue(noLoggerRegistry.isRegistered("no_logger"));
        }

        @Test
        @DisplayName("Collision throws exception even without logger")
        void register_collisionWithoutLogger_throwsException() {
            AfflictionRegistry noLoggerRegistry = new AfflictionRegistry();
            noLoggerRegistry.register(createTestAffliction("dupe", "First"));

            assertThrows(
                    IllegalArgumentException.class,
                    () -> noLoggerRegistry.register(createTestAffliction("dupe", "Second"))
            );
        }
    }

    /**
     * Helper method to create a test affliction.
     */
    private Affliction createTestAffliction(String id, String displayName) {
        return new TestAffliction.Builder(id)
                .displayName(displayName)
                .build();
    }

    /**
     * Simple test affliction implementation.
     */
    private static class TestAffliction extends AbstractAffliction {

        private TestAffliction(Builder builder) {
            super(builder);
        }

        public static class Builder extends AbstractAffliction.Builder {

            public Builder(@NotNull String id) {
                super(id);
                this.category = AfflictionCategory.SUPERNATURAL;
            }

            @Override
            public @NotNull TestAffliction build() {
                validate();
                return new TestAffliction(this);
            }
        }
    }
}
