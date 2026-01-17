package com.dnocturne.afflictions.condition;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.MockBukkit;
import org.mockbukkit.mockbukkit.ServerMock;
import org.mockbukkit.mockbukkit.entity.PlayerMock;
import org.mockbukkit.mockbukkit.world.WorldMock;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for CompositeCondition - logical combination of conditions.
 */
@DisplayName("CompositeCondition")
class CompositeConditionTest {

    private ServerMock server;
    private WorldMock world;
    private PlayerMock player;

    // Simple test conditions
    private final Condition alwaysTrue = Condition.ALWAYS;
    private final Condition alwaysFalse = Condition.NEVER;

    @BeforeEach
    void setUp() {
        server = MockBukkit.mock();
        world = server.addSimpleWorld("test_world");
        player = server.addPlayer();
        player.teleport(world.getSpawnLocation());
    }

    @AfterEach
    void tearDown() {
        MockBukkit.unmock();
    }

    @Nested
    @DisplayName("AND Operations")
    class AndOperations {

        @Test
        @DisplayName("AND with all true returns true")
        void and_allTrue_returnsTrue() {
            Condition result = CompositeCondition.and(alwaysTrue, alwaysTrue, alwaysTrue);
            assertTrue(result.test(player));
        }

        @Test
        @DisplayName("AND with one false returns false")
        void and_oneFalse_returnsFalse() {
            Condition result = CompositeCondition.and(alwaysTrue, alwaysFalse, alwaysTrue);
            assertFalse(result.test(player));
        }

        @Test
        @DisplayName("AND with all false returns false")
        void and_allFalse_returnsFalse() {
            Condition result = CompositeCondition.and(alwaysFalse, alwaysFalse);
            assertFalse(result.test(player));
        }

        @Test
        @DisplayName("AND with empty returns ALWAYS")
        void and_empty_returnsAlways() {
            Condition result = CompositeCondition.and();
            assertTrue(result.test(player));
            assertSame(Condition.ALWAYS, result);
        }

        @Test
        @DisplayName("AND with single condition returns that condition")
        void and_single_returnsSameCondition() {
            Condition[] single = { alwaysTrue };
            Condition result = CompositeCondition.and(single);
            assertSame(alwaysTrue, result);
        }

        @Test
        @DisplayName("Fluent and() chains correctly")
        void fluentAnd_chainsCorrectly() {
            Condition trueCondition = p -> true;
            Condition falseCondition = p -> false;

            Condition result = trueCondition.and(trueCondition).and(trueCondition);
            assertTrue(result.test(player));

            Condition result2 = trueCondition.and(falseCondition);
            assertFalse(result2.test(player));
        }
    }

    @Nested
    @DisplayName("OR Operations")
    class OrOperations {

        @Test
        @DisplayName("OR with all false returns false")
        void or_allFalse_returnsFalse() {
            Condition result = CompositeCondition.or(alwaysFalse, alwaysFalse, alwaysFalse);
            assertFalse(result.test(player));
        }

        @Test
        @DisplayName("OR with one true returns true")
        void or_oneTrue_returnsTrue() {
            Condition result = CompositeCondition.or(alwaysFalse, alwaysTrue, alwaysFalse);
            assertTrue(result.test(player));
        }

        @Test
        @DisplayName("OR with all true returns true")
        void or_allTrue_returnsTrue() {
            Condition result = CompositeCondition.or(alwaysTrue, alwaysTrue);
            assertTrue(result.test(player));
        }

        @Test
        @DisplayName("OR with empty returns NEVER")
        void or_empty_returnsNever() {
            Condition result = CompositeCondition.or();
            assertFalse(result.test(player));
            assertSame(Condition.NEVER, result);
        }

        @Test
        @DisplayName("OR with single condition returns that condition")
        void or_single_returnsSameCondition() {
            Condition[] single = { alwaysTrue };
            Condition result = CompositeCondition.or(single);
            assertSame(alwaysTrue, result);
        }

        @Test
        @DisplayName("Fluent or() chains correctly")
        void fluentOr_chainsCorrectly() {
            Condition result = alwaysFalse.or(alwaysFalse).or(alwaysTrue);
            assertTrue(result.test(player));

            Condition result2 = alwaysFalse.or(alwaysFalse);
            assertFalse(result2.test(player));
        }
    }

    @Nested
    @DisplayName("NOT Operations")
    class NotOperations {

        @Test
        @DisplayName("NOT true returns false")
        void not_true_returnsFalse() {
            Condition result = CompositeCondition.not(alwaysTrue);
            assertFalse(result.test(player));
        }

        @Test
        @DisplayName("NOT false returns true")
        void not_false_returnsTrue() {
            Condition result = CompositeCondition.not(alwaysFalse);
            assertTrue(result.test(player));
        }

        @Test
        @DisplayName("Double negation returns original condition")
        void doubleNegation_returnsOriginal() {
            Condition notNot = CompositeCondition.not(CompositeCondition.not(alwaysTrue));
            // Should optimize and return the original
            assertSame(alwaysTrue, notNot);
        }

        @Test
        @DisplayName("Fluent negate() works correctly")
        void fluentNegate_worksCorrectly() {
            Condition result = alwaysTrue.negate();
            assertFalse(result.test(player));

            Condition result2 = alwaysFalse.negate();
            assertTrue(result2.test(player));
        }
    }

    @Nested
    @DisplayName("Complex Compositions")
    class ComplexCompositions {

        @Test
        @DisplayName("(A AND B) OR C works correctly")
        void andThenOr_worksCorrectly() {
            // (false AND true) OR true = false OR true = true
            Condition result = alwaysFalse.and(alwaysTrue).or(alwaysTrue);
            assertTrue(result.test(player));

            // (false AND true) OR false = false OR false = false
            Condition result2 = alwaysFalse.and(alwaysTrue).or(alwaysFalse);
            assertFalse(result2.test(player));
        }

        @Test
        @DisplayName("A OR (B AND C) works correctly")
        void orWithAndGroup_worksCorrectly() {
            // true OR (false AND false) = true
            Condition result = alwaysTrue.or(alwaysFalse.and(alwaysFalse));
            assertTrue(result.test(player));

            // false OR (true AND true) = true
            Condition result2 = alwaysFalse.or(alwaysTrue.and(alwaysTrue));
            assertTrue(result2.test(player));

            // false OR (true AND false) = false
            Condition result3 = alwaysFalse.or(alwaysTrue.and(alwaysFalse));
            assertFalse(result3.test(player));
        }

        @Test
        @DisplayName("NOT (A AND B) works correctly (De Morgan)")
        void notAnd_deMorgan() {
            // NOT (true AND true) = false
            Condition result = alwaysTrue.and(alwaysTrue).negate();
            assertFalse(result.test(player));

            // NOT (true AND false) = true
            Condition result2 = alwaysTrue.and(alwaysFalse).negate();
            assertTrue(result2.test(player));
        }

        @Test
        @DisplayName("NOT (A OR B) works correctly (De Morgan)")
        void notOr_deMorgan() {
            // NOT (false OR false) = true
            Condition result = alwaysFalse.or(alwaysFalse).negate();
            assertTrue(result.test(player));

            // NOT (true OR false) = false
            Condition result2 = alwaysTrue.or(alwaysFalse).negate();
            assertFalse(result2.test(player));
        }
    }

    @Nested
    @DisplayName("Description")
    class Description {

        @Test
        @DisplayName("AND description shows all conditions")
        void and_description_showsAll() {
            Condition a = new TestCondition("A");
            Condition b = new TestCondition("B");
            Condition result = CompositeCondition.and(a, b);

            String desc = result.describe();
            assertTrue(desc.contains("A"));
            assertTrue(desc.contains("B"));
            assertTrue(desc.contains("AND"));
        }

        @Test
        @DisplayName("OR description shows all conditions")
        void or_description_showsAll() {
            Condition a = new TestCondition("A");
            Condition b = new TestCondition("B");
            Condition result = CompositeCondition.or(a, b);

            String desc = result.describe();
            assertTrue(desc.contains("A"));
            assertTrue(desc.contains("B"));
            assertTrue(desc.contains("OR"));
        }

        @Test
        @DisplayName("NOT description shows negated condition")
        void not_description_showsNegation() {
            Condition a = new TestCondition("A");
            Condition result = CompositeCondition.not(a);

            String desc = result.describe();
            assertTrue(desc.contains("NOT"));
            assertTrue(desc.contains("A"));
        }
    }

    /**
     * Simple test condition with custom description.
     */
    private record TestCondition(String name) implements Condition {
        @Override
        public boolean test(org.bukkit.entity.Player player) {
            return true;
        }

        @Override
        public String describe() {
            return name;
        }
    }
}
