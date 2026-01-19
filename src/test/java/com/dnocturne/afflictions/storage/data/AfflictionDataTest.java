package com.dnocturne.afflictions.storage.data;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for AfflictionData record.
 */
@DisplayName("AfflictionData")
class AfflictionDataTest {

    @Nested
    @DisplayName("Constructor")
    class Constructor {

        @Test
        @DisplayName("creates with basic fields")
        void constructor_withBasicFields() {
            AfflictionData data = new AfflictionData("vampirism", 3, 1000L, 123456789L);

            assertEquals("vampirism", data.afflictionId());
            assertEquals(3, data.level());
            assertEquals(1000L, data.duration());
            assertEquals(123456789L, data.contractedAt());
            assertTrue(data.data().isEmpty());
        }

        @Test
        @DisplayName("creates with custom data map")
        void constructor_withDataMap() {
            Map<String, String> customData = new HashMap<>();
            customData.put("key1", "value1");
            customData.put("key2", "value2");

            AfflictionData data = new AfflictionData("werewolf", 1, -1L, 999L, customData);

            assertEquals("werewolf", data.afflictionId());
            assertEquals(1, data.level());
            assertEquals(-1L, data.duration());
            assertEquals(999L, data.contractedAt());
            assertEquals(2, data.data().size());
            assertEquals("value1", data.getData("key1"));
            assertEquals("value2", data.getData("key2"));
        }

        @Test
        @DisplayName("copies data map defensively (immutable)")
        void constructor_copiesDataMapDefensively() {
            Map<String, String> originalMap = new HashMap<>();
            originalMap.put("key", "original");

            AfflictionData data = new AfflictionData("test", 1, -1L, 0L, originalMap);

            // Modify original map
            originalMap.put("key", "modified");

            // Data should still have original value (immutable copy)
            assertEquals("original", data.getData("key"));
        }

        @Test
        @DisplayName("data map is immutable")
        void constructor_dataMapIsImmutable() {
            Map<String, String> customData = new HashMap<>();
            customData.put("key", "value");

            AfflictionData data = new AfflictionData("test", 1, -1L, 0L, customData);

            // Attempting to modify should throw
            assertThrows(UnsupportedOperationException.class, () ->
                data.data().put("newKey", "newValue")
            );
        }
    }

    @Nested
    @DisplayName("Data Operations")
    class DataOperations {

        @Test
        @DisplayName("getData returns null for missing key")
        void getData_missingKey_returnsNull() {
            AfflictionData data = new AfflictionData("test", 1, -1L, 0L);
            assertNull(data.getData("nonexistent"));
        }

        @Test
        @DisplayName("getData returns value for existing key")
        void getData_existingKey_returnsValue() {
            Map<String, String> customData = Map.of("key", "value");
            AfflictionData data = new AfflictionData("test", 1, -1L, 0L, customData);

            assertEquals("value", data.getData("key"));
        }
    }

    @Nested
    @DisplayName("Edge Cases")
    class EdgeCases {

        @Test
        @DisplayName("handles permanent duration (-1)")
        void permanentDuration() {
            AfflictionData data = new AfflictionData("test", 1, -1L, 0L);
            assertEquals(-1L, data.duration());
        }

        @Test
        @DisplayName("handles level 0")
        void levelZero() {
            AfflictionData data = new AfflictionData("test", 0, -1L, 0L);
            assertEquals(0, data.level());
        }

        @Test
        @DisplayName("handles empty affliction ID")
        void emptyAfflictionId() {
            AfflictionData data = new AfflictionData("", 1, -1L, 0L);
            assertEquals("", data.afflictionId());
        }

        @Test
        @DisplayName("handles empty data map")
        void emptyDataMap() {
            AfflictionData data = new AfflictionData("test", 1, -1L, 0L);
            assertNotNull(data.data());
            assertTrue(data.data().isEmpty());
        }
    }

    @Nested
    @DisplayName("Record Features")
    class RecordFeatures {

        @Test
        @DisplayName("equals works correctly")
        void equals_worksCorrectly() {
            AfflictionData data1 = new AfflictionData("test", 1, -1L, 0L);
            AfflictionData data2 = new AfflictionData("test", 1, -1L, 0L);
            AfflictionData data3 = new AfflictionData("other", 1, -1L, 0L);

            assertEquals(data1, data2);
            assertNotEquals(data1, data3);
        }

        @Test
        @DisplayName("hashCode is consistent")
        void hashCode_isConsistent() {
            AfflictionData data1 = new AfflictionData("test", 1, -1L, 0L);
            AfflictionData data2 = new AfflictionData("test", 1, -1L, 0L);

            assertEquals(data1.hashCode(), data2.hashCode());
        }

        @Test
        @DisplayName("toString contains all fields")
        void toString_containsAllFields() {
            AfflictionData data = new AfflictionData("vampirism", 3, 1000L, 123L);
            String str = data.toString();

            assertTrue(str.contains("vampirism"));
            assertTrue(str.contains("3"));
            assertTrue(str.contains("1000"));
            assertTrue(str.contains("123"));
        }
    }
}
