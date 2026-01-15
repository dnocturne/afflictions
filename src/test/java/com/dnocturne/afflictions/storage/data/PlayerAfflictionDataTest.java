package com.dnocturne.afflictions.storage.data;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for PlayerAfflictionData DTO.
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

            assertEquals(TEST_UUID, data.getUuid());
            assertEquals(TEST_USERNAME, data.getUsername());
            assertTrue(data.getAfflictions().isEmpty());
        }

        @Test
        @DisplayName("creates with affliction list")
        void constructor_withAfflictionList() {
            List<AfflictionData> afflictions = new ArrayList<>();
            afflictions.add(new AfflictionData("vampirism", 2, -1L, 1000L));
            afflictions.add(new AfflictionData("werewolf", 1, 5000L, 2000L));

            PlayerAfflictionData data = new PlayerAfflictionData(TEST_UUID, TEST_USERNAME, afflictions);

            assertEquals(TEST_UUID, data.getUuid());
            assertEquals(TEST_USERNAME, data.getUsername());
            assertEquals(2, data.getAfflictions().size());
        }

        @Test
        @DisplayName("copies affliction list defensively")
        void constructor_copiesListDefensively() {
            List<AfflictionData> originalList = new ArrayList<>();
            originalList.add(new AfflictionData("vampirism", 1, -1L, 0L));

            PlayerAfflictionData data = new PlayerAfflictionData(TEST_UUID, TEST_USERNAME, originalList);

            // Modify original list
            originalList.clear();

            // Data should still have the affliction
            assertEquals(1, data.getAfflictions().size());
        }
    }

    @Nested
    @DisplayName("Affliction Management")
    class AfflictionManagement {

        @Test
        @DisplayName("addAffliction adds to list")
        void addAffliction_addsToList() {
            PlayerAfflictionData data = new PlayerAfflictionData(TEST_UUID, TEST_USERNAME);

            AfflictionData affliction = new AfflictionData("vampirism", 1, -1L, 0L);
            data.addAffliction(affliction);

            assertEquals(1, data.getAfflictions().size());
            assertEquals("vampirism", data.getAfflictions().get(0).getAfflictionId());
        }

        @Test
        @DisplayName("removeAffliction removes by ID (case-insensitive)")
        void removeAffliction_removesById() {
            PlayerAfflictionData data = new PlayerAfflictionData(TEST_UUID, TEST_USERNAME);
            data.addAffliction(new AfflictionData("vampirism", 1, -1L, 0L));
            data.addAffliction(new AfflictionData("werewolf", 1, -1L, 0L));

            data.removeAffliction("VAMPIRISM"); // uppercase

            assertEquals(1, data.getAfflictions().size());
            assertEquals("werewolf", data.getAfflictions().get(0).getAfflictionId());
        }

        @Test
        @DisplayName("removeAffliction does nothing for non-existent ID")
        void removeAffliction_nonExistent_doesNothing() {
            PlayerAfflictionData data = new PlayerAfflictionData(TEST_UUID, TEST_USERNAME);
            data.addAffliction(new AfflictionData("vampirism", 1, -1L, 0L));

            data.removeAffliction("nonexistent");

            assertEquals(1, data.getAfflictions().size());
        }

        @Test
        @DisplayName("hasAffliction returns true for existing (case-insensitive)")
        void hasAffliction_exists_returnsTrue() {
            PlayerAfflictionData data = new PlayerAfflictionData(TEST_UUID, TEST_USERNAME);
            data.addAffliction(new AfflictionData("vampirism", 1, -1L, 0L));

            assertTrue(data.hasAffliction("vampirism"));
            assertTrue(data.hasAffliction("VAMPIRISM"));
            assertTrue(data.hasAffliction("Vampirism"));
        }

        @Test
        @DisplayName("hasAffliction returns false for non-existent")
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
            PlayerAfflictionData data = new PlayerAfflictionData(TEST_UUID, TEST_USERNAME);
            data.addAffliction(new AfflictionData("vampirism", 1, -1L, 0L));
            data.addAffliction(new AfflictionData("werewolf", 2, -1L, 0L));
            data.addAffliction(new AfflictionData("curse", 3, 1000L, 0L));

            assertEquals(3, data.getAfflictions().size());
            assertTrue(data.hasAffliction("vampirism"));
            assertTrue(data.hasAffliction("werewolf"));
            assertTrue(data.hasAffliction("curse"));
        }

        @Test
        @DisplayName("can add duplicate affliction IDs (no validation)")
        void duplicateAfflictions_noValidation() {
            // Note: This class doesn't prevent duplicates - that's handled at a higher level
            PlayerAfflictionData data = new PlayerAfflictionData(TEST_UUID, TEST_USERNAME);
            data.addAffliction(new AfflictionData("vampirism", 1, -1L, 0L));
            data.addAffliction(new AfflictionData("vampirism", 2, -1L, 0L));

            assertEquals(2, data.getAfflictions().size());
        }
    }

    @Nested
    @DisplayName("Edge Cases")
    class EdgeCases {

        @Test
        @DisplayName("handles empty username")
        void emptyUsername() {
            PlayerAfflictionData data = new PlayerAfflictionData(TEST_UUID, "");
            assertEquals("", data.getUsername());
        }

        @Test
        @DisplayName("handles null username")
        void nullUsername() {
            PlayerAfflictionData data = new PlayerAfflictionData(TEST_UUID, null);
            assertNull(data.getUsername());
        }

        @Test
        @DisplayName("getAfflictions returns mutable list")
        void getAfflictions_returnsMutableList() {
            PlayerAfflictionData data = new PlayerAfflictionData(TEST_UUID, TEST_USERNAME);

            // Can add directly to returned list
            data.getAfflictions().add(new AfflictionData("test", 1, -1L, 0L));

            assertEquals(1, data.getAfflictions().size());
        }
    }
}
