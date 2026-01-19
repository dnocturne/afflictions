package com.dnocturne.afflictions.player;

import com.dnocturne.afflictions.TestAffliction;
import com.dnocturne.afflictions.api.affliction.Affliction;
import com.dnocturne.afflictions.api.affliction.AfflictionInstance;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.MockBukkit;
import org.mockbukkit.mockbukkit.ServerMock;
import org.mockbukkit.mockbukkit.entity.PlayerMock;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for AfflictedPlayer.
 */
@DisplayName("AfflictedPlayer")
class AfflictedPlayerTest {

    private ServerMock server;
    private PlayerMock player;
    private Affliction vampirism;
    private Affliction werewolf;

    @BeforeEach
    void setUp() {
        server = MockBukkit.mock();
        player = server.addPlayer("TestPlayer");
        vampirism = TestAffliction.create("vampirism");
        werewolf = TestAffliction.create("werewolf");
    }

    @AfterEach
    void tearDown() {
        MockBukkit.unmock();
    }

    @Nested
    @DisplayName("Constructor")
    class Constructor {

        @Test
        @DisplayName("creates with UUID")
        void constructor_withUuid() {
            AfflictedPlayer afflicted = new AfflictedPlayer(player.getUniqueId());

            assertEquals(player.getUniqueId(), afflicted.getUuid());
            assertTrue(afflicted.getAfflictions().isEmpty());
            assertFalse(afflicted.hasAnyAffliction());
            assertEquals(0, afflicted.getAfflictionCount());
        }
    }

    @Nested
    @DisplayName("Player Reference")
    class PlayerReference {

        @Test
        @DisplayName("getPlayer returns online player")
        void getPlayer_returnsOnlinePlayer() {
            AfflictedPlayer afflicted = new AfflictedPlayer(player.getUniqueId());

            Optional<org.bukkit.entity.Player> result = afflicted.getPlayer();

            assertTrue(result.isPresent());
            assertEquals(player, result.get());
        }

        @Test
        @DisplayName("getPlayer returns empty for offline UUID")
        void getPlayer_returnsEmptyForOffline() {
            UUID offlineUuid = UUID.randomUUID();
            AfflictedPlayer afflicted = new AfflictedPlayer(offlineUuid);

            assertTrue(afflicted.getPlayer().isEmpty());
        }

        @Test
        @DisplayName("isOnline returns true for online player")
        void isOnline_returnsTrue() {
            AfflictedPlayer afflicted = new AfflictedPlayer(player.getUniqueId());
            assertTrue(afflicted.isOnline());
        }

        @Test
        @DisplayName("isOnline returns false for offline player")
        void isOnline_returnsFalse() {
            AfflictedPlayer afflicted = new AfflictedPlayer(UUID.randomUUID());
            assertFalse(afflicted.isOnline());
        }
    }

    @Nested
    @DisplayName("Add Affliction")
    class AddAffliction {

        @Test
        @DisplayName("adds affliction successfully")
        void addAffliction_success() {
            AfflictedPlayer afflicted = new AfflictedPlayer(player.getUniqueId());
            AfflictionInstance instance = new AfflictionInstance(player.getUniqueId(), vampirism);

            boolean result = afflicted.addAffliction(instance);

            assertTrue(result);
            assertTrue(afflicted.hasAffliction("vampirism"));
            assertEquals(1, afflicted.getAfflictionCount());
        }

        @Test
        @DisplayName("returns false for duplicate affliction")
        void addAffliction_duplicate_returnsFalse() {
            AfflictedPlayer afflicted = new AfflictedPlayer(player.getUniqueId());
            AfflictionInstance instance1 = new AfflictionInstance(player.getUniqueId(), vampirism, 1, -1);
            AfflictionInstance instance2 = new AfflictionInstance(player.getUniqueId(), vampirism, 2, -1);

            afflicted.addAffliction(instance1);
            boolean result = afflicted.addAffliction(instance2);

            assertFalse(result);
            assertEquals(1, afflicted.getAfflictionCount());
            // Original instance should be kept
            assertEquals(1, afflicted.getAffliction("vampirism").get().getLevel());
        }

        @Test
        @DisplayName("can add multiple different afflictions")
        void addAffliction_multipleDifferent() {
            AfflictedPlayer afflicted = new AfflictedPlayer(player.getUniqueId());

            afflicted.addAffliction(new AfflictionInstance(player.getUniqueId(), vampirism));
            afflicted.addAffliction(new AfflictionInstance(player.getUniqueId(), werewolf));

            assertEquals(2, afflicted.getAfflictionCount());
            assertTrue(afflicted.hasAffliction("vampirism"));
            assertTrue(afflicted.hasAffliction("werewolf"));
        }

        @Test
        @DisplayName("affliction ID is case-insensitive")
        void addAffliction_caseInsensitive() {
            AfflictedPlayer afflicted = new AfflictedPlayer(player.getUniqueId());
            Affliction upperCase = TestAffliction.create("VAMPIRISM");

            afflicted.addAffliction(new AfflictionInstance(player.getUniqueId(), upperCase));

            assertTrue(afflicted.hasAffliction("vampirism"));
            assertTrue(afflicted.hasAffliction("VAMPIRISM"));
            assertTrue(afflicted.hasAffliction("Vampirism"));
        }
    }

    @Nested
    @DisplayName("Remove Affliction")
    class RemoveAffliction {

        @Test
        @DisplayName("removes existing affliction")
        void removeAffliction_exists() {
            AfflictedPlayer afflicted = new AfflictedPlayer(player.getUniqueId());
            afflicted.addAffliction(new AfflictionInstance(player.getUniqueId(), vampirism));

            Optional<AfflictionInstance> removed = afflicted.removeAffliction("vampirism");

            assertTrue(removed.isPresent());
            assertEquals("vampirism", removed.get().getAfflictionId());
            assertFalse(afflicted.hasAffliction("vampirism"));
            assertEquals(0, afflicted.getAfflictionCount());
        }

        @Test
        @DisplayName("returns empty for non-existent affliction")
        void removeAffliction_notExists() {
            AfflictedPlayer afflicted = new AfflictedPlayer(player.getUniqueId());

            Optional<AfflictionInstance> removed = afflicted.removeAffliction("vampirism");

            assertTrue(removed.isEmpty());
        }

        @Test
        @DisplayName("removes by ID case-insensitively")
        void removeAffliction_caseInsensitive() {
            AfflictedPlayer afflicted = new AfflictedPlayer(player.getUniqueId());
            afflicted.addAffliction(new AfflictionInstance(player.getUniqueId(), vampirism));

            Optional<AfflictionInstance> removed = afflicted.removeAffliction("VAMPIRISM");

            assertTrue(removed.isPresent());
            assertFalse(afflicted.hasAffliction("vampirism"));
        }
    }

    @Nested
    @DisplayName("Query Afflictions")
    class QueryAfflictions {

        @Test
        @DisplayName("hasAffliction returns correct result")
        void hasAffliction() {
            AfflictedPlayer afflicted = new AfflictedPlayer(player.getUniqueId());

            assertFalse(afflicted.hasAffliction("vampirism"));

            afflicted.addAffliction(new AfflictionInstance(player.getUniqueId(), vampirism));

            assertTrue(afflicted.hasAffliction("vampirism"));
            assertFalse(afflicted.hasAffliction("werewolf"));
        }

        @Test
        @DisplayName("getAffliction returns instance")
        void getAffliction_exists() {
            AfflictedPlayer afflicted = new AfflictedPlayer(player.getUniqueId());
            AfflictionInstance instance = new AfflictionInstance(player.getUniqueId(), vampirism, 3, -1);
            afflicted.addAffliction(instance);

            Optional<AfflictionInstance> result = afflicted.getAffliction("vampirism");

            assertTrue(result.isPresent());
            assertEquals(3, result.get().getLevel());
        }

        @Test
        @DisplayName("getAffliction returns empty for non-existent")
        void getAffliction_notExists() {
            AfflictedPlayer afflicted = new AfflictedPlayer(player.getUniqueId());

            assertTrue(afflicted.getAffliction("vampirism").isEmpty());
        }

        @Test
        @DisplayName("getAfflictions returns unmodifiable collection")
        void getAfflictions_unmodifiable() {
            AfflictedPlayer afflicted = new AfflictedPlayer(player.getUniqueId());
            afflicted.addAffliction(new AfflictionInstance(player.getUniqueId(), vampirism));

            var afflictions = afflicted.getAfflictions();

            assertThrows(UnsupportedOperationException.class, () ->
                    afflictions.add(new AfflictionInstance(player.getUniqueId(), werewolf))
            );
        }

        @Test
        @DisplayName("hasAnyAffliction returns correct result")
        void hasAnyAffliction() {
            AfflictedPlayer afflicted = new AfflictedPlayer(player.getUniqueId());

            assertFalse(afflicted.hasAnyAffliction());

            afflicted.addAffliction(new AfflictionInstance(player.getUniqueId(), vampirism));

            assertTrue(afflicted.hasAnyAffliction());
        }

        @Test
        @DisplayName("getAfflictionCount returns correct count")
        void getAfflictionCount() {
            AfflictedPlayer afflicted = new AfflictedPlayer(player.getUniqueId());

            assertEquals(0, afflicted.getAfflictionCount());

            afflicted.addAffliction(new AfflictionInstance(player.getUniqueId(), vampirism));
            assertEquals(1, afflicted.getAfflictionCount());

            afflicted.addAffliction(new AfflictionInstance(player.getUniqueId(), werewolf));
            assertEquals(2, afflicted.getAfflictionCount());
        }
    }

    @Nested
    @DisplayName("Clear Afflictions")
    class ClearAfflictions {

        @Test
        @DisplayName("clearAfflictions removes all")
        void clearAfflictions_removesAll() {
            AfflictedPlayer afflicted = new AfflictedPlayer(player.getUniqueId());
            afflicted.addAffliction(new AfflictionInstance(player.getUniqueId(), vampirism));
            afflicted.addAffliction(new AfflictionInstance(player.getUniqueId(), werewolf));
            assertEquals(2, afflicted.getAfflictionCount());

            afflicted.clearAfflictions();

            assertEquals(0, afflicted.getAfflictionCount());
            assertFalse(afflicted.hasAnyAffliction());
            assertFalse(afflicted.hasAffliction("vampirism"));
            assertFalse(afflicted.hasAffliction("werewolf"));
        }

        @Test
        @DisplayName("clearAfflictions on empty does nothing")
        void clearAfflictions_onEmpty() {
            AfflictedPlayer afflicted = new AfflictedPlayer(player.getUniqueId());

            afflicted.clearAfflictions(); // Should not throw

            assertEquals(0, afflicted.getAfflictionCount());
        }
    }
}
