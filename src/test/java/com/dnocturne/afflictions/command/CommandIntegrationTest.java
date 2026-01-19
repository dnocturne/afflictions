package com.dnocturne.afflictions.command;

import com.dnocturne.afflictions.Afflictions;
import com.dnocturne.afflictions.TestAffliction;
import com.dnocturne.afflictions.api.affliction.Affliction;
import com.dnocturne.afflictions.manager.AfflictionManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.MockBukkit;
import org.mockbukkit.mockbukkit.ServerMock;
import org.mockbukkit.mockbukkit.entity.PlayerMock;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for command functionality.
 * <p>
 * Note: These tests verify command behavior through the AfflictionManager
 * rather than directly invoking Cloud commands, since MockBukkit doesn't
 * fully support the Paper Brigadier command API that Cloud uses.
 * <p>
 * The tests validate the same logic that commands execute.
 */
@DisplayName("Command Integration")
class CommandIntegrationTest {

    private ServerMock server;
    private Afflictions plugin;
    private AfflictionManager afflictionManager;

    @BeforeEach
    void setUp() {
        server = MockBukkit.mock();
        plugin = MockBukkit.load(Afflictions.class);
        afflictionManager = plugin.getAfflictionManager();
    }

    @AfterEach
    void tearDown() {
        MockBukkit.unmock();
    }

    @Nested
    @DisplayName("Give Command Logic")
    class GiveCommandLogic {

        @Test
        @DisplayName("can give affliction to player")
        void canGiveAfflictionToPlayer() {
            PlayerMock player = server.addPlayer("TestPlayer");
            Affliction affliction = TestAffliction.create("test_curse");
            afflictionManager.getRegistry().register(affliction);

            // Simulate give command logic
            boolean success = afflictionManager.applyAffliction(player, "test_curse", 1);

            assertTrue(success);
            assertTrue(afflictionManager.hasAffliction(player.getUniqueId(), "test_curse"));
        }

        @Test
        @DisplayName("can give affliction with specific level")
        void canGiveAfflictionWithLevel() {
            PlayerMock player = server.addPlayer("TestPlayer");
            Affliction affliction = TestAffliction.create("test_curse");
            afflictionManager.getRegistry().register(affliction);

            // Simulate give command with level
            boolean success = afflictionManager.applyAffliction(player, "test_curse", 3);

            assertTrue(success);
            var instance = afflictionManager.getPlayerManager()
                    .get(player.getUniqueId()).get()
                    .getAffliction("test_curse").get();
            assertEquals(3, instance.getLevel());
        }

        @Test
        @DisplayName("fails for unknown affliction")
        void failsForUnknownAffliction() {
            PlayerMock player = server.addPlayer("TestPlayer");

            // Simulate give command with invalid affliction
            boolean success = afflictionManager.applyAffliction(player, "nonexistent", 1);

            assertFalse(success);
            assertFalse(afflictionManager.hasAffliction(player.getUniqueId(), "nonexistent"));
        }

        @Test
        @DisplayName("fails if player already has affliction")
        void failsIfPlayerAlreadyHasAffliction() {
            PlayerMock player = server.addPlayer("TestPlayer");
            Affliction affliction = TestAffliction.create("test_curse");
            afflictionManager.getRegistry().register(affliction);

            // First give
            afflictionManager.applyAffliction(player, "test_curse", 1);

            // Second give should fail
            boolean success = afflictionManager.applyAffliction(player, "test_curse", 2);

            assertFalse(success);
            // Level should remain at 1
            assertEquals(1, afflictionManager.getPlayerManager()
                    .get(player.getUniqueId()).get()
                    .getAffliction("test_curse").get().getLevel());
        }
    }

    @Nested
    @DisplayName("Remove Command Logic")
    class RemoveCommandLogic {

        @Test
        @DisplayName("can remove affliction from player")
        void canRemoveAfflictionFromPlayer() {
            PlayerMock player = server.addPlayer("TestPlayer");
            Affliction affliction = TestAffliction.create("test_curse");
            afflictionManager.getRegistry().register(affliction);
            afflictionManager.applyAffliction(player, "test_curse");

            // Simulate remove command logic
            boolean success = afflictionManager.removeAffliction(
                    player, "test_curse", AfflictionManager.RemovalReason.ADMIN);

            assertTrue(success);
            assertFalse(afflictionManager.hasAffliction(player.getUniqueId(), "test_curse"));
        }

        @Test
        @DisplayName("fails if player doesn't have affliction")
        void failsIfPlayerDoesntHaveAffliction() {
            PlayerMock player = server.addPlayer("TestPlayer");

            // Simulate remove command with no affliction
            boolean success = afflictionManager.removeAffliction(
                    player, "test_curse", AfflictionManager.RemovalReason.ADMIN);

            assertFalse(success);
        }

        @Test
        @DisplayName("can remove one of multiple afflictions")
        void canRemoveOneOfMultipleAfflictions() {
            PlayerMock player = server.addPlayer("TestPlayer");
            Affliction curse1 = TestAffliction.create("curse1");
            Affliction curse2 = TestAffliction.create("curse2");
            afflictionManager.getRegistry().register(curse1);
            afflictionManager.getRegistry().register(curse2);
            afflictionManager.applyAffliction(player, "curse1");
            afflictionManager.applyAffliction(player, "curse2");

            // Remove only one
            boolean success = afflictionManager.removeAffliction(
                    player, "curse1", AfflictionManager.RemovalReason.ADMIN);

            assertTrue(success);
            assertFalse(afflictionManager.hasAffliction(player.getUniqueId(), "curse1"));
            assertTrue(afflictionManager.hasAffliction(player.getUniqueId(), "curse2"));
        }
    }

    @Nested
    @DisplayName("Clear Command Logic")
    class ClearCommandLogic {

        @Test
        @DisplayName("can clear all afflictions from player")
        void canClearAllAfflictions() {
            PlayerMock player = server.addPlayer("TestPlayer");
            Affliction curse1 = TestAffliction.create("curse1");
            Affliction curse2 = TestAffliction.create("curse2");
            afflictionManager.getRegistry().register(curse1);
            afflictionManager.getRegistry().register(curse2);
            afflictionManager.applyAffliction(player, "curse1");
            afflictionManager.applyAffliction(player, "curse2");

            // Simulate clear command logic
            afflictionManager.clearAfflictions(player, AfflictionManager.RemovalReason.ADMIN);

            assertFalse(afflictionManager.hasAffliction(player.getUniqueId(), "curse1"));
            assertFalse(afflictionManager.hasAffliction(player.getUniqueId(), "curse2"));
        }

        @Test
        @DisplayName("clear on player with no afflictions does not throw")
        void clearOnPlayerWithNoAfflictions() {
            PlayerMock player = server.addPlayer("TestPlayer");

            // Should not throw
            afflictionManager.clearAfflictions(player, AfflictionManager.RemovalReason.ADMIN);

            assertFalse(afflictionManager.getPlayerManager().get(player.getUniqueId()).isPresent());
        }
    }

    @Nested
    @DisplayName("Reload Command Logic")
    class ReloadCommandLogic {

        @Test
        @DisplayName("config can be reloaded")
        void configCanBeReloaded() {
            // Simulate reload command logic
            plugin.getConfigManager().reload();
            plugin.getLocalizationManager().reload();

            // Should not throw and managers should still be accessible
            assertNotNull(plugin.getConfigManager().getMainConfig());
        }
    }

    @Nested
    @DisplayName("Info Command Logic")
    class InfoCommandLogic {

        @Test
        @DisplayName("can get player affliction info")
        void canGetPlayerAfflictionInfo() {
            PlayerMock player = server.addPlayer("TestPlayer");
            Affliction affliction = TestAffliction.create("test_curse");
            afflictionManager.getRegistry().register(affliction);
            afflictionManager.applyAffliction(player, "test_curse", 2);

            // Simulate info command logic - get affliction details
            var afflictedPlayer = afflictionManager.getPlayerManager().get(player.getUniqueId());

            assertTrue(afflictedPlayer.isPresent());
            assertTrue(afflictedPlayer.get().hasAnyAffliction());
            assertEquals(1, afflictedPlayer.get().getAfflictionCount());

            var instance = afflictedPlayer.get().getAffliction("test_curse");
            assertTrue(instance.isPresent());
            assertEquals(2, instance.get().getLevel());
            assertEquals("test_curse", instance.get().getAfflictionId());
        }

        @Test
        @DisplayName("info returns empty for player without afflictions")
        void infoReturnsEmptyForPlayerWithoutAfflictions() {
            PlayerMock player = server.addPlayer("TestPlayer");

            var afflictedPlayer = afflictionManager.getPlayerManager().get(player.getUniqueId());

            // Player not tracked if no afflictions
            assertTrue(afflictedPlayer.isEmpty());
        }
    }

    @Nested
    @DisplayName("List Command Logic")
    class ListCommandLogic {

        @Test
        @DisplayName("can list all registered afflictions")
        void canListAllRegisteredAfflictions() {
            Affliction curse1 = TestAffliction.create("curse1");
            Affliction curse2 = TestAffliction.create("curse2");
            afflictionManager.getRegistry().register(curse1);
            afflictionManager.getRegistry().register(curse2);

            // Simulate list command logic
            var allAfflictions = afflictionManager.getRegistry().getAll();

            // Vampirism is registered by default + our 2 test afflictions
            assertTrue(allAfflictions.size() >= 2);
            assertTrue(afflictionManager.getRegistry().isRegistered("curse1"));
            assertTrue(afflictionManager.getRegistry().isRegistered("curse2"));
        }

        @Test
        @DisplayName("can list player's afflictions")
        void canListPlayersAfflictions() {
            PlayerMock player = server.addPlayer("TestPlayer");
            Affliction curse1 = TestAffliction.create("curse1");
            Affliction curse2 = TestAffliction.create("curse2");
            afflictionManager.getRegistry().register(curse1);
            afflictionManager.getRegistry().register(curse2);
            afflictionManager.applyAffliction(player, "curse1");
            afflictionManager.applyAffliction(player, "curse2");

            // Simulate list command for player
            var afflictedPlayer = afflictionManager.getPlayerManager().get(player.getUniqueId());

            assertTrue(afflictedPlayer.isPresent());
            var afflictions = afflictedPlayer.get().getAfflictions();
            assertEquals(2, afflictions.size());
        }
    }

    @Nested
    @DisplayName("Registry Access")
    class RegistryAccess {

        @Test
        @DisplayName("registry contains default vampirism affliction")
        void registryContainsVampirism() {
            // Vampirism is registered by default on plugin enable
            assertTrue(afflictionManager.getRegistry().isRegistered("vampirism"));
        }

        @Test
        @DisplayName("can get all affliction IDs for suggestions")
        void canGetAllAfflictionIds() {
            Affliction testAffliction = TestAffliction.create("test_curse");
            afflictionManager.getRegistry().register(testAffliction);

            var ids = afflictionManager.getRegistry().getAllIds();

            assertTrue(ids.contains("vampirism"));
            assertTrue(ids.contains("test_curse"));
        }
    }
}
