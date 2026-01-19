package com.dnocturne.afflictions;

import com.dnocturne.afflictions.affliction.impl.Vampirism;
import com.dnocturne.afflictions.api.affliction.Affliction;
import com.dnocturne.afflictions.api.affliction.AfflictionInstance;
import com.dnocturne.afflictions.player.AfflictedPlayer;
import com.dnocturne.basalt.manager.PlayerManager;
import com.dnocturne.basalt.registry.Registry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for plugin functionality without requiring full plugin load.
 * These are unit tests for core classes that don't need MockBukkit plugin loading.
 */
@DisplayName("Afflictions Core")
class AfflictionsPluginTest {

    @Nested
    @DisplayName("Affliction Registry")
    class AfflictionRegistryTests {

        private Registry<Affliction> registry;

        @BeforeEach
        void setUp() {
            registry = Registry.<Affliction>forIdentifiable("affliction")
                    .setDisplayNameExtractor(Affliction::getDisplayName);
        }

        @Test
        @DisplayName("can register affliction")
        void registerAffliction() {
            Vampirism vampirism = Vampirism.create();
            registry.register(vampirism);

            assertTrue(registry.isRegistered("vampirism"));
        }

        @Test
        @DisplayName("can get affliction by ID")
        void getById() {
            Vampirism vampirism = Vampirism.create();
            registry.register(vampirism);

            Optional<Affliction> result = registry.get("vampirism");

            assertTrue(result.isPresent());
            assertEquals(Vampirism.ID, result.get().getId());
        }

        @Test
        @DisplayName("get returns empty for unknown ID")
        void getUnknown() {
            Optional<Affliction> result = registry.get("unknown");
            assertTrue(result.isEmpty());
        }

        @Test
        @DisplayName("can check if affliction is registered")
        void isRegistered() {
            Vampirism vampirism = Vampirism.create();
            registry.register(vampirism);

            assertTrue(registry.isRegistered("vampirism"));
            assertFalse(registry.isRegistered("unknown"));
        }

        @Test
        @DisplayName("can register custom affliction")
        void registerCustom() {
            TestAffliction custom = TestAffliction.create("custom_test");
            registry.register(custom);

            assertTrue(registry.isRegistered("custom_test"));
        }

        @Test
        @DisplayName("getAll returns all registered afflictions")
        void getAll() {
            Vampirism vampirism = Vampirism.create();
            TestAffliction custom = TestAffliction.create("custom_test");

            registry.register(vampirism);
            registry.register(custom);

            assertEquals(2, registry.getAll().size());
        }
    }

    @Nested
    @DisplayName("Player Manager")
    class PlayerManagerTests {

        private PlayerManager<AfflictedPlayer> playerManager;

        @BeforeEach
        void setUp() {
            playerManager = new PlayerManager<>(AfflictedPlayer::new, AfflictedPlayer::hasAnyAffliction);
        }

        @Test
        @DisplayName("creates afflicted player on demand")
        void createsAfflictedPlayer() {
            UUID uuid = UUID.randomUUID();

            AfflictedPlayer afflicted = playerManager.getOrCreate(uuid);

            assertNotNull(afflicted);
            assertEquals(uuid, afflicted.getUuid());
        }

        @Test
        @DisplayName("returns same instance for same UUID")
        void sameInstance() {
            UUID uuid = UUID.randomUUID();

            AfflictedPlayer first = playerManager.getOrCreate(uuid);
            AfflictedPlayer second = playerManager.getOrCreate(uuid);

            assertSame(first, second);
        }

        @Test
        @DisplayName("can remove player from manager")
        void removePlayer() {
            UUID uuid = UUID.randomUUID();
            playerManager.getOrCreate(uuid);

            playerManager.remove(uuid);

            assertTrue(playerManager.get(uuid).isEmpty());
        }

        @Test
        @DisplayName("get returns empty for non-existent player")
        void getReturnsEmpty() {
            UUID uuid = UUID.randomUUID();

            assertTrue(playerManager.get(uuid).isEmpty());
        }
    }

    @Nested
    @DisplayName("Afflicted Player")
    class AfflictedPlayerTests {

        private UUID playerUuid;
        private AfflictedPlayer afflictedPlayer;
        private Affliction vampirism;

        @BeforeEach
        void setUp() {
            playerUuid = UUID.randomUUID();
            afflictedPlayer = new AfflictedPlayer(playerUuid);
            vampirism = Vampirism.create();
        }

        @Test
        @DisplayName("has correct UUID")
        void hasCorrectUuid() {
            assertEquals(playerUuid, afflictedPlayer.getUuid());
        }

        @Test
        @DisplayName("can add affliction")
        void addAffliction() {
            AfflictionInstance instance = new AfflictionInstance(playerUuid, vampirism, 1, -1);
            afflictedPlayer.addAffliction(instance);

            assertTrue(afflictedPlayer.hasAffliction("vampirism"));
        }

        @Test
        @DisplayName("can get affliction by ID")
        void getAffliction() {
            AfflictionInstance instance = new AfflictionInstance(playerUuid, vampirism, 1, -1);
            afflictedPlayer.addAffliction(instance);

            Optional<AfflictionInstance> result = afflictedPlayer.getAffliction("vampirism");

            assertTrue(result.isPresent());
            assertEquals("vampirism", result.get().getAfflictionId());
        }

        @Test
        @DisplayName("can remove affliction")
        void removeAffliction() {
            AfflictionInstance instance = new AfflictionInstance(playerUuid, vampirism, 1, -1);
            afflictedPlayer.addAffliction(instance);
            afflictedPlayer.removeAffliction("vampirism");

            assertFalse(afflictedPlayer.hasAffliction("vampirism"));
        }

        @Test
        @DisplayName("can clear all afflictions")
        void clearAfflictions() {
            AfflictionInstance instance1 = new AfflictionInstance(playerUuid, vampirism, 1, -1);
            AfflictionInstance instance2 = new AfflictionInstance(playerUuid, TestAffliction.create("test"), 1, -1);

            afflictedPlayer.addAffliction(instance1);
            afflictedPlayer.addAffliction(instance2);
            afflictedPlayer.clearAfflictions();

            assertTrue(afflictedPlayer.getAfflictions().isEmpty());
        }
    }

    @Nested
    @DisplayName("Affliction Instance Data")
    class AfflictionInstanceData {

        private UUID playerUuid;
        private Affliction vampirism;

        @BeforeEach
        void setUp() {
            playerUuid = UUID.randomUUID();
            vampirism = Vampirism.create();
        }

        @Test
        @DisplayName("instance has correct default values")
        void instanceDefaults() {
            AfflictionInstance instance = new AfflictionInstance(playerUuid, vampirism, 1, -1);

            assertEquals(1, instance.getLevel());
            assertTrue(instance.isPermanent());
            assertEquals("vampirism", instance.getAfflictionId());
        }

        @Test
        @DisplayName("can modify instance level")
        void modifyLevel() {
            AfflictionInstance instance = new AfflictionInstance(playerUuid, vampirism, 1, -1);

            instance.setLevel(4);

            assertEquals(4, instance.getLevel());
        }

        @Test
        @DisplayName("level is capped at affliction max")
        void levelCapped() {
            AfflictionInstance instance = new AfflictionInstance(playerUuid, vampirism, 1, -1);

            instance.setLevel(100);

            assertEquals(5, instance.getLevel()); // Vampirism max is 5
        }

        @Test
        @DisplayName("can store and retrieve custom data")
        void customData() {
            AfflictionInstance instance = new AfflictionInstance(playerUuid, vampirism, 1, -1);

            instance.setData("custom_key", "custom_value");

            assertEquals("custom_value", instance.getData("custom_key"));
        }

        @Test
        @DisplayName("instance with level parameter")
        void instanceWithLevel() {
            AfflictionInstance instance = new AfflictionInstance(playerUuid, vampirism, 3, -1);

            assertEquals(3, instance.getLevel());
        }

        @Test
        @DisplayName("non-permanent instance has duration")
        void nonPermanentInstance() {
            AfflictionInstance instance = new AfflictionInstance(playerUuid, vampirism, 1, 6000L);

            assertFalse(instance.isPermanent());
            assertEquals(6000L, instance.getDuration());
        }
    }
}
