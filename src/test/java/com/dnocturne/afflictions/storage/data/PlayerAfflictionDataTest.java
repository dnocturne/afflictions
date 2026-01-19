package com.dnocturne.afflictions.storage.data;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for PlayerAfflictionData record.
 */
@DisplayName("PlayerAfflictionData")
class PlayerAfflictionDataTest {

    private static final UUID TEST_UUID = UUID.fromString("12345678-1234-1234-1234-123456789abc");
    private static final String TEST_USERNAME = "TestPlayer";

    @Nested
    @DisplayName("Constructor")
    class Constructor {

        @Test
        @DisplayName("creates with UUID and username only")
        void constructor_uuidAndUsernameOnly() {
            PlayerAfflictionData data = new PlayerAfflictionData(TEST_UUID, TEST_USERNAME);

            assertEquals(TEST_UUID, data.uuid());
            assertEquals(TEST_USERNAME, data.username());
            assertTrue(data.afflictions().isEmpty());
        }

        @Test
        @DisplayName("creates with affliction list")
        void constructor_withAfflictionList() {
            List<AfflictionData> afflictions = new ArrayList<>();
            afflictions.add(new AfflictionData("vampirism", 2, -1L, 1000L));
            afflictions.add(new AfflictionData("werewolf", 1, 5000L, 2000L));

            PlayerAfflictionData data = new PlayerAfflictionData(TEST_UUID, TEST_USERNAME, afflictions);

            assertEquals(TEST_UUID, data.uuid());
            assertEquals(TEST_USERNAME, data.username());
            assertEquals(2, data.afflictions().size());
        }

        @Test
        @DisplayName("copies affliction list defensively (immutable)")
        void constructor_copiesListDefensively() {
            List<AfflictionData> originalList = new ArrayList<>();
            originalList.add(new AfflictionData("vampirism", 1, -1L, 0L));

            PlayerAfflictionData data = new PlayerAfflictionData(TEST_UUID, TEST_USERNAME, originalList);

            // Modify original list
            originalList.clear();

            // Data should still have the affliction (immutable copy)
            assertEquals(1, data.afflictions().size());
        }

        @Test
        @DisplayName("afflictions list is immutable")
        void constructor_afflictionsListIsImmutable() {
            List<AfflictionData> afflictions = new ArrayList<>();
            afflictions.add(new AfflictionData("vampirism", 1, -1L, 0L));

            PlayerAfflictionData data = new PlayerAfflictionData(TEST_UUID, TEST_USERNAME, afflictions);

            // Attempting to modify should throw
            assertThrows(UnsupportedOperationException.class, () ->
                data.afflictions().add(new AfflictionData("test", 1, -1L, 0L))
            );
        }
    }

    @Nested
    @DisplayName("hasAffliction")
    class HasAffliction {

        @Test
        @DisplayName("returns true for existing (case-insensitive)")
        void hasAffliction_exists_returnsTrue() {
            List<AfflictionData> afflictions = List.of(
                new AfflictionData("vampirism", 1, -1L, 0L)
            );
            PlayerAfflictionData data = new PlayerAfflictionData(TEST_UUID, TEST_USERNAME, afflictions);

            assertTrue(data.hasAffliction("vampirism"));
            assertTrue(data.hasAffliction("VAMPIRISM"));
            assertTrue(data.hasAffliction("Vampirism"));
        }

        @Test
        @DisplayName("returns false for non-existent")
        void hasAffliction_notExists_returnsFalse() {
            PlayerAfflictionData data = new PlayerAfflictionData(TEST_UUID, TEST_USERNAME);

            assertFalse(data.hasAffliction("vampirism"));
        }
    }

    @Nested
    @DisplayName("Multiple Afflictions")
    class MultipleAfflictions {

        @Test
        @DisplayName("can have multiple different afflictions")
        void multipleAfflictions() {
            List<AfflictionData> afflictions = List.of(
                new AfflictionData("vampirism", 1, -1L, 0L),
                new AfflictionData("werewolf", 2, -1L, 0L),
                new AfflictionData("curse", 3, 1000L, 0L)
            );
            PlayerAfflictionData data = new PlayerAfflictionData(TEST_UUID, TEST_USERNAME, afflictions);

            assertEquals(3, data.afflictions().size());
            assertTrue(data.hasAffliction("vampirism"));
            assertTrue(data.hasAffliction("werewolf"));
            assertTrue(data.hasAffliction("curse"));
        }
    }

    @Nested
    @DisplayName("Edge Cases")
    class EdgeCases {

        @Test
        @DisplayName("handles empty username")
        void emptyUsername() {
            PlayerAfflictionData data = new PlayerAfflictionData(TEST_UUID, "");
            assertEquals("", data.username());
        }
    }

    @Nested
    @DisplayName("Record Features")
    class RecordFeatures {

        @Test
        @DisplayName("equals works correctly")
        void equals_worksCorrectly() {
            PlayerAfflictionData data1 = new PlayerAfflictionData(TEST_UUID, TEST_USERNAME);
            PlayerAfflictionData data2 = new PlayerAfflictionData(TEST_UUID, TEST_USERNAME);
            PlayerAfflictionData data3 = new PlayerAfflictionData(UUID.randomUUID(), TEST_USERNAME);

            assertEquals(data1, data2);
            assertNotEquals(data1, data3);
        }

        @Test
        @DisplayName("hashCode is consistent")
        void hashCode_isConsistent() {
            PlayerAfflictionData data1 = new PlayerAfflictionData(TEST_UUID, TEST_USERNAME);
            PlayerAfflictionData data2 = new PlayerAfflictionData(TEST_UUID, TEST_USERNAME);

            assertEquals(data1.hashCode(), data2.hashCode());
        }

        @Test
        @DisplayName("toString contains all fields")
        void toString_containsAllFields() {
            PlayerAfflictionData data = new PlayerAfflictionData(TEST_UUID, TEST_USERNAME);
            String str = data.toString();

            assertTrue(str.contains(TEST_UUID.toString()));
            assertTrue(str.contains(TEST_USERNAME));
        }
    }
}
