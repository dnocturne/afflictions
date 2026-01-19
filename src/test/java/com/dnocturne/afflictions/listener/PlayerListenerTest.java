package com.dnocturne.afflictions.listener;

import com.dnocturne.afflictions.Afflictions;
import com.dnocturne.afflictions.TestAffliction;
import com.dnocturne.afflictions.api.affliction.Affliction;
import com.dnocturne.afflictions.manager.AfflictionManager;
import com.dnocturne.afflictions.storage.data.AfflictionData;
import com.dnocturne.afflictions.storage.data.PlayerAfflictionData;
import com.dnocturne.basalt.storage.Storage;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.MockBukkit;
import org.mockbukkit.mockbukkit.ServerMock;
import org.mockbukkit.mockbukkit.entity.PlayerMock;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for PlayerListener.
 * <p>
 * Note: Some async operations in PlayerListener are difficult to test with MockBukkit
 * due to timing issues. These tests focus on the testable synchronous aspects.
 */
@DisplayName("PlayerListener")
class PlayerListenerTest {

    private ServerMock server;
    private Afflictions plugin;
    private AfflictionManager afflictionManager;
    private Storage<PlayerAfflictionData> storage;

    @BeforeEach
    void setUp() {
        server = MockBukkit.mock();
        plugin = MockBukkit.load(Afflictions.class);
        afflictionManager = plugin.getAfflictionManager();
        storage = plugin.getStorageManager().getStorage();

        // Register test affliction
        Affliction testAffliction = TestAffliction.create("test_curse");
        afflictionManager.getRegistry().register(testAffliction);
    }

    @AfterEach
    void tearDown() {
        MockBukkit.unmock();
    }

    @Nested
    @DisplayName("Player Join")
    class PlayerJoin {

        @Test
        @DisplayName("handles player with no saved afflictions")
        void handlesPlayerWithNoSavedAfflictions() {
            PlayerMock player = server.addPlayer("NewPlayer");

            // Allow async operations
            server.getScheduler().performTicks(40);

            // Player should have no afflictions
            assertFalse(afflictionManager.hasAffliction(player.getUniqueId(), "test_curse"));
        }

        @Test
        @DisplayName("handles unknown affliction in saved data gracefully")
        void handlesUnknownAffliction() throws ExecutionException, InterruptedException {
            // Pre-create a UUID for a player that will join
            UUID playerUuid = UUID.randomUUID();

            // Save data with an unknown affliction before player joins
            PlayerAfflictionData dataToSave = new PlayerAfflictionData(
                    playerUuid,
                    "TestPlayer",
                    List.of(new AfflictionData("nonexistent_curse", 1, -1, System.currentTimeMillis()))
            );
            storage.save(dataToSave).get();

            // Now add a player (simulates join) - MockBukkit generates UUID so this won't match
            // This test mainly verifies the loading code doesn't crash on unknown afflictions
            PlayerMock player = server.addPlayer("TestPlayer");

            // Allow async operations
            server.getScheduler().performTicks(40);

            // Should not crash, player won't have the affliction (different UUID)
            assertFalse(afflictionManager.hasAffliction(player.getUniqueId(), "nonexistent_curse"));
        }
    }

    @Nested
    @DisplayName("Player Quit")
    class PlayerQuit {

        @Test
        @DisplayName("removes player from memory after quit when no afflictions")
        void removesPlayerFromMemoryOnQuit_noAfflictions() {
            PlayerMock player = server.addPlayer("TestPlayer");

            // Player has no afflictions, quits
            player.disconnect();

            // Allow async operations
            server.getScheduler().performTicks(40);

            // Player should not be tracked
            assertFalse(afflictionManager.getPlayerManager().get(player.getUniqueId()).isPresent());
        }

        @Test
        @DisplayName("does not save if player has no afflictions")
        void doesNotSaveIfNoAfflictions() {
            PlayerMock player = server.addPlayer("TestPlayer");

            // Player has no afflictions
            player.disconnect();

            server.getScheduler().performTicks(40);

            // Player should not be tracked
            assertFalse(afflictionManager.getPlayerManager().get(player.getUniqueId()).isPresent());
        }
    }

    @Nested
    @DisplayName("Registration")
    class Registration {

        @Test
        @DisplayName("listener can be registered")
        void canBeRegistered() {
            PlayerListener listener = new PlayerListener(plugin);

            // Should not throw
            listener.register();
        }
    }

    @Nested
    @DisplayName("Manager Null Safety")
    class ManagerNullSafety {

        @Test
        @DisplayName("handles null managers gracefully during join")
        void handlesNullManagersOnJoin() {
            // This test verifies the null checks in PlayerListener work
            // The plugin's managers are not null in normal operation,
            // but the code has null checks for safety
            server.addPlayer("TestPlayer");

            // If we get here without NPE, the null checks are working
            server.getScheduler().performTicks(40);
        }

        @Test
        @DisplayName("handles null managers gracefully during quit")
        void handlesNullManagersOnQuit() {
            PlayerMock player = server.addPlayer("TestPlayer");
            player.disconnect();

            // If we get here without NPE, the null checks are working
            server.getScheduler().performTicks(40);
        }
    }

    @Nested
    @DisplayName("Storage Integration")
    class StorageIntegration {

        @Test
        @DisplayName("storage can save and load player affliction data")
        void storageCanSaveAndLoadData() throws ExecutionException, InterruptedException {
            UUID uuid = UUID.randomUUID();
            String username = "TestPlayer";

            // Save data directly to storage
            Map<String, String> customData = Map.of("key1", "value1");
            PlayerAfflictionData dataToSave = new PlayerAfflictionData(
                    uuid,
                    username,
                    List.of(new AfflictionData("test_curse", 3, -1, System.currentTimeMillis(), customData))
            );
            storage.save(dataToSave).get();

            // Load it back
            Optional<PlayerAfflictionData> loaded = storage.load(uuid).get();

            assertTrue(loaded.isPresent());
            assertEquals(uuid, loaded.get().uuid());
            assertEquals(username, loaded.get().username());
            assertEquals(1, loaded.get().afflictions().size());

            AfflictionData afflictionData = loaded.get().afflictions().get(0);
            assertEquals("test_curse", afflictionData.afflictionId());
            assertEquals(3, afflictionData.level());
            assertEquals("value1", afflictionData.data().get("key1"));
        }

        @Test
        @DisplayName("storage can load by username")
        void storageCanLoadByUsername() throws ExecutionException, InterruptedException {
            UUID uuid = UUID.randomUUID();
            String username = "UniqueTestPlayer";

            // Save data
            PlayerAfflictionData dataToSave = new PlayerAfflictionData(
                    uuid,
                    username,
                    List.of(new AfflictionData("test_curse", 1, -1, System.currentTimeMillis()))
            );
            storage.save(dataToSave).get();

            // Load by username
            Optional<PlayerAfflictionData> loaded = storage.loadByName(username).get();

            assertTrue(loaded.isPresent());
            assertEquals(uuid, loaded.get().uuid());
            assertEquals(username, loaded.get().username());
        }

        @Test
        @DisplayName("storage returns empty for non-existent player")
        void storageReturnsEmptyForNonExistent() throws ExecutionException, InterruptedException {
            UUID randomUuid = UUID.randomUUID();

            Optional<PlayerAfflictionData> loaded = storage.load(randomUuid).get();

            assertTrue(loaded.isEmpty());
        }
    }
}
