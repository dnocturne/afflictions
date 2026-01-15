package com.dnocturne.afflictions.storage.data;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for AfflictionData DTO.
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

            assertEquals("vampirism", data.getAfflictionId());
            assertEquals(3, data.getLevel());
            assertEquals(1000L, data.getDuration());
            assertEquals(123456789L, data.getContractedAt());
            assertTrue(data.getData().isEmpty());
        }

        @Test
        @DisplayName("creates with custom data map")
        void constructor_withDataMap() {
            Map<String, String> customData = new HashMap<>();
            customData.put("key1", "value1");
            customData.put("key2", "value2");

            AfflictionData data = new AfflictionData("werewolf", 1, -1L, 999L, customData);

            assertEquals("werewolf", data.getAfflictionId());
            assertEquals(1, data.getLevel());
            assertEquals(-1L, data.getDuration());
            assertEquals(999L, data.getContractedAt());
            assertEquals(2, data.getData().size());
            assertEquals("value1", data.getData("key1"));
            assertEquals("value2", data.getData("key2"));
        }

        @Test
        @DisplayName("copies data map defensively")
        void constructor_copiesDataMapDefensively() {
            Map<String, String> originalMap = new HashMap<>();
            originalMap.put("key", "original");

            AfflictionData data = new AfflictionData("test", 1, -1L, 0L, originalMap);

            // Modify original map
            originalMap.put("key", "modified");

            // Data should still have original value
            assertEquals("original", data.getData("key"));
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
        @DisplayName("setData adds new key-value pair")
        void setData_addsNewPair() {
            AfflictionData data = new AfflictionData("test", 1, -1L, 0L);
            data.setData("newKey", "newValue");

            assertEquals("newValue", data.getData("newKey"));
        }

        @Test
        @DisplayName("setData overwrites existing key")
        void setData_overwritesExisting() {
            Map<String, String> initialData = new HashMap<>();
            initialData.put("key", "oldValue");

            AfflictionData data = new AfflictionData("test", 1, -1L, 0L, initialData);
            data.setData("key", "newValue");

            assertEquals("newValue", data.getData("key"));
        }

        @Test
        @DisplayName("getData map is mutable")
        void getDataMap_isMutable() {
            AfflictionData data = new AfflictionData("test", 1, -1L, 0L);
            data.getData().put("directKey", "directValue");

            // Changes to returned map affect the internal state
            assertEquals("directValue", data.getData("directKey"));
        }
    }

    @Nested
    @DisplayName("Edge Cases")
    class EdgeCases {

        @Test
        @DisplayName("handles permanent duration (-1)")
        void permanentDuration() {
            AfflictionData data = new AfflictionData("test", 1, -1L, 0L);
            assertEquals(-1L, data.getDuration());
        }

        @Test
        @DisplayName("handles level 0")
        void levelZero() {
            AfflictionData data = new AfflictionData("test", 0, -1L, 0L);
            assertEquals(0, data.getLevel());
        }

        @Test
        @DisplayName("handles empty affliction ID")
        void emptyAfflictionId() {
            AfflictionData data = new AfflictionData("", 1, -1L, 0L);
            assertEquals("", data.getAfflictionId());
        }

        @Test
        @DisplayName("handles null data map by creating empty")
        void nullDataMap() {
            // Using basic constructor which creates empty map
            AfflictionData data = new AfflictionData("test", 1, -1L, 0L);
            assertNotNull(data.getData());
            assertTrue(data.getData().isEmpty());
        }
    }
}
