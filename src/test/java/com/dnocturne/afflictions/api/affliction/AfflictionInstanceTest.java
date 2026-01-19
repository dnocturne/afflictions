package com.dnocturne.afflictions.api.affliction;

import com.dnocturne.afflictions.TestAffliction;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.MockBukkit;
import org.mockbukkit.mockbukkit.ServerMock;
import org.mockbukkit.mockbukkit.entity.PlayerMock;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for AfflictionInstance.
 */
@DisplayName("AfflictionInstance")
class AfflictionInstanceTest {

    private ServerMock server;
    private PlayerMock player;
    private Affliction testAffliction;

    @BeforeEach
    void setUp() {
        server = MockBukkit.mock();
        player = server.addPlayer("TestPlayer");
        testAffliction = TestAffliction.builder("test")
                .displayName("Test Affliction")
                .maxLevel(5)
                .build();
    }

    @AfterEach
    void tearDown() {
        MockBukkit.unmock();
    }

    @Nested
    @DisplayName("Constructor")
    class Constructor {

        @Test
        @DisplayName("creates with minimal parameters")
        void constructor_minimalParams() {
            AfflictionInstance instance = new AfflictionInstance(player.getUniqueId(), testAffliction);

            assertEquals(player.getUniqueId(), instance.getPlayerUuid());
            assertEquals(testAffliction, instance.getAffliction());
            assertEquals("test", instance.getAfflictionId());
            assertEquals(1, instance.getLevel());
            assertEquals(-1, instance.getDuration());
            assertTrue(instance.isPermanent());
            assertTrue(instance.getAllData().isEmpty());
        }

        @Test
        @DisplayName("creates with level and duration")
        void constructor_withLevelAndDuration() {
            AfflictionInstance instance = new AfflictionInstance(player.getUniqueId(), testAffliction, 3, 5000L);

            assertEquals(3, instance.getLevel());
            assertEquals(5000L, instance.getDuration());
            assertFalse(instance.isPermanent());
        }

        @Test
        @DisplayName("creates with all parameters including contractedAt")
        void constructor_withAllParams() {
            long contractedAt = 1234567890L;
            AfflictionInstance instance = new AfflictionInstance(
                    player.getUniqueId(), testAffliction, 2, 3000L, contractedAt
            );

            assertEquals(2, instance.getLevel());
            assertEquals(3000L, instance.getDuration());
            assertEquals(contractedAt, instance.getContractedAt());
        }

        @Test
        @DisplayName("sets contractedAt to current time by default")
        void constructor_setsDefaultContractedAt() {
            long before = System.currentTimeMillis();
            AfflictionInstance instance = new AfflictionInstance(player.getUniqueId(), testAffliction);
            long after = System.currentTimeMillis();

            assertTrue(instance.getContractedAt() >= before);
            assertTrue(instance.getContractedAt() <= after);
        }
    }

    @Nested
    @DisplayName("Level Management")
    class LevelManagement {

        @Test
        @DisplayName("setLevel sets valid level")
        void setLevel_validLevel() {
            AfflictionInstance instance = new AfflictionInstance(player.getUniqueId(), testAffliction);
            instance.setLevel(3);
            assertEquals(3, instance.getLevel());
        }

        @Test
        @DisplayName("setLevel caps at maxLevel")
        void setLevel_capsAtMax() {
            AfflictionInstance instance = new AfflictionInstance(player.getUniqueId(), testAffliction);
            instance.setLevel(10); // maxLevel is 5
            assertEquals(5, instance.getLevel());
        }

        @Test
        @DisplayName("incrementLevel increases by 1")
        void incrementLevel_increasesByOne() {
            AfflictionInstance instance = new AfflictionInstance(player.getUniqueId(), testAffliction);
            assertEquals(1, instance.getLevel());

            instance.incrementLevel();
            assertEquals(2, instance.getLevel());

            instance.incrementLevel();
            assertEquals(3, instance.getLevel());
        }

        @Test
        @DisplayName("incrementLevel caps at maxLevel")
        void incrementLevel_capsAtMax() {
            AfflictionInstance instance = new AfflictionInstance(player.getUniqueId(), testAffliction, 5, -1);
            instance.incrementLevel();
            assertEquals(5, instance.getLevel()); // Should stay at 5
        }
    }

    @Nested
    @DisplayName("Duration Management")
    class DurationManagement {

        @Test
        @DisplayName("setDuration changes duration")
        void setDuration_changesDuration() {
            AfflictionInstance instance = new AfflictionInstance(player.getUniqueId(), testAffliction);
            assertTrue(instance.isPermanent());

            instance.setDuration(5000L);
            assertEquals(5000L, instance.getDuration());
            assertFalse(instance.isPermanent());
        }

        @Test
        @DisplayName("isPermanent returns true for negative duration")
        void isPermanent_negativeDuration() {
            AfflictionInstance instance = new AfflictionInstance(player.getUniqueId(), testAffliction, 1, -1);
            assertTrue(instance.isPermanent());

            instance.setDuration(-100);
            assertTrue(instance.isPermanent());
        }

        @Test
        @DisplayName("isPermanent returns false for non-negative duration")
        void isPermanent_nonNegativeDuration() {
            AfflictionInstance instance = new AfflictionInstance(player.getUniqueId(), testAffliction, 1, 0);
            assertFalse(instance.isPermanent());

            instance.setDuration(1000);
            assertFalse(instance.isPermanent());
        }
    }

    @Nested
    @DisplayName("Time Tracking")
    class TimeTracking {

        @Test
        @DisplayName("getTimeAfflicted returns elapsed time")
        void getTimeAfflicted_returnsElapsedTime() throws InterruptedException {
            long contractedAt = System.currentTimeMillis() - 5000; // 5 seconds ago
            AfflictionInstance instance = new AfflictionInstance(
                    player.getUniqueId(), testAffliction, 1, -1, contractedAt
            );

            long timeAfflicted = instance.getTimeAfflicted();
            assertTrue(timeAfflicted >= 5000);
            assertTrue(timeAfflicted < 6000); // Should be close to 5000
        }
    }

    @Nested
    @DisplayName("Custom Data")
    class CustomData {

        @Test
        @DisplayName("setData and getData work correctly")
        void setAndGetData() {
            AfflictionInstance instance = new AfflictionInstance(player.getUniqueId(), testAffliction);

            instance.setData("testKey", "testValue");
            assertEquals("testValue", instance.getData("testKey"));
        }

        @Test
        @DisplayName("getData returns null for missing key")
        void getData_missingKey_returnsNull() {
            AfflictionInstance instance = new AfflictionInstance(player.getUniqueId(), testAffliction);
            assertNull(instance.getData("nonexistent"));
        }

        @Test
        @DisplayName("getData with type returns typed value")
        void getData_withType_returnsTypedValue() {
            AfflictionInstance instance = new AfflictionInstance(player.getUniqueId(), testAffliction);
            instance.setData("key", "value");
            assertEquals("value", instance.getData("key", String.class));
        }

        @Test
        @DisplayName("getData with type returns null for missing key")
        void getData_withType_missingKey_returnsNull() {
            AfflictionInstance instance = new AfflictionInstance(player.getUniqueId(), testAffliction);
            assertNull(instance.getData("nonexistent", String.class));
        }

        @Test
        @DisplayName("getData with type returns null for wrong type")
        void getData_withType_wrongType_returnsNull() {
            AfflictionInstance instance = new AfflictionInstance(player.getUniqueId(), testAffliction);
            instance.setData("key", 123);
            assertNull(instance.getData("key", String.class));
        }

        @Test
        @DisplayName("getData with default returns default for missing key")
        void getData_withDefault_returnDefault() {
            AfflictionInstance instance = new AfflictionInstance(player.getUniqueId(), testAffliction);
            assertEquals("default", instance.getData("nonexistent", String.class, "default"));
        }

        @Test
        @DisplayName("getData with default returns value if exists")
        void getData_withDefault_returnsValueIfExists() {
            AfflictionInstance instance = new AfflictionInstance(player.getUniqueId(), testAffliction);
            instance.setData("key", "value");
            assertEquals("value", instance.getData("key", String.class, "default"));
        }

        @Test
        @DisplayName("getData with default returns default for wrong type")
        void getData_withDefault_wrongType_returnsDefault() {
            AfflictionInstance instance = new AfflictionInstance(player.getUniqueId(), testAffliction);
            instance.setData("key", 123);
            assertEquals("default", instance.getData("key", String.class, "default"));
        }

        @Test
        @DisplayName("hasData returns correct result")
        void hasData_returnsCorrectResult() {
            AfflictionInstance instance = new AfflictionInstance(player.getUniqueId(), testAffliction);

            assertFalse(instance.hasData("key"));

            instance.setData("key", "value");
            assertTrue(instance.hasData("key"));
        }

        @Test
        @DisplayName("removeData removes the key")
        void removeData_removesKey() {
            AfflictionInstance instance = new AfflictionInstance(player.getUniqueId(), testAffliction);
            instance.setData("key", "value");
            assertTrue(instance.hasData("key"));

            instance.removeData("key");
            assertFalse(instance.hasData("key"));
        }

        @Test
        @DisplayName("getAllData returns unmodifiable view of data map")
        void getAllData_returnsUnmodifiableView() {
            AfflictionInstance instance = new AfflictionInstance(player.getUniqueId(), testAffliction);
            instance.setData("key1", "value1");
            instance.setData("key2", "value2");

            var allData = instance.getAllData();
            assertEquals(2, allData.size());
            assertEquals("value1", allData.get("key1"));
            assertEquals("value2", allData.get("key2"));

            // Returned map should be unmodifiable
            assertThrows(UnsupportedOperationException.class, () -> allData.put("key3", "value3"));
        }

        @Test
        @DisplayName("can store different types")
        void data_storesDifferentTypes() {
            AfflictionInstance instance = new AfflictionInstance(player.getUniqueId(), testAffliction);

            instance.setData("string", "text");
            instance.setData("integer", 42);
            instance.setData("boolean", true);
            instance.setData("double", 3.14);

            assertEquals("text", instance.getData("string"));
            assertEquals(42, instance.getData("integer"));
            assertEquals(true, instance.getData("boolean"));
            assertEquals(3.14, instance.getData("double"));
        }
    }

    @Nested
    @DisplayName("Player Reference")
    class PlayerReference {

        @Test
        @DisplayName("getPlayer returns online player")
        void getPlayer_returnsOnlinePlayer() {
            AfflictionInstance instance = new AfflictionInstance(player.getUniqueId(), testAffliction);

            assertTrue(instance.getPlayer().isPresent());
            assertEquals(player, instance.getPlayer().get());
        }

        @Test
        @DisplayName("getPlayer returns empty for offline player")
        void getPlayer_returnsEmptyForOffline() {
            UUID offlineUuid = UUID.randomUUID();
            AfflictionInstance instance = new AfflictionInstance(offlineUuid, testAffliction);

            assertTrue(instance.getPlayer().isEmpty());
        }
    }
}
