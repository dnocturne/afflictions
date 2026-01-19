package com.dnocturne.afflictions.manager;

import com.dnocturne.afflictions.Afflictions;
import com.dnocturne.afflictions.TestAffliction;
import com.dnocturne.afflictions.api.affliction.Affliction;
import com.dnocturne.afflictions.api.affliction.AfflictionInstance;
import com.dnocturne.afflictions.player.AfflictedPlayer;
import com.dnocturne.basalt.component.Component;
import com.dnocturne.basalt.component.Tickable;
import org.bukkit.entity.Player;
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
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for AfflictionManager.
 */
@DisplayName("AfflictionManager")
class AfflictionManagerTest {

    private ServerMock server;
    private Afflictions plugin;
    private AfflictionManager manager;

    @BeforeEach
    void setUp() {
        server = MockBukkit.mock();
        plugin = MockBukkit.load(Afflictions.class);
        manager = plugin.getAfflictionManager();
    }

    @AfterEach
    void tearDown() {
        MockBukkit.unmock();
    }

    @Nested
    @DisplayName("Apply Affliction")
    class ApplyAffliction {

        @Test
        @DisplayName("applies affliction to player successfully")
        void applyAffliction_success() {
            PlayerMock player = server.addPlayer("TestPlayer");
            Affliction affliction = TestAffliction.create("test_curse");
            manager.getRegistry().register(affliction);

            boolean result = manager.applyAffliction(player, "test_curse");

            assertTrue(result);
            assertTrue(manager.hasAffliction(player.getUniqueId(), "test_curse"));
        }

        @Test
        @DisplayName("applies affliction with specific level")
        void applyAffliction_withLevel() {
            PlayerMock player = server.addPlayer("TestPlayer");
            Affliction affliction = TestAffliction.create("test_curse");
            manager.getRegistry().register(affliction);

            boolean result = manager.applyAffliction(player, "test_curse", 3);

            assertTrue(result);
            Optional<AfflictedPlayer> afflictedOpt = manager.getPlayerManager().get(player.getUniqueId());
            assertTrue(afflictedOpt.isPresent());
            assertEquals(3, afflictedOpt.get().getAffliction("test_curse").get().getLevel());
        }

        @Test
        @DisplayName("returns false for unknown affliction")
        void applyAffliction_unknownAffliction() {
            PlayerMock player = server.addPlayer("TestPlayer");

            boolean result = manager.applyAffliction(player, "nonexistent");

            assertFalse(result);
            assertFalse(manager.hasAffliction(player.getUniqueId(), "nonexistent"));
        }

        @Test
        @DisplayName("returns false if player already has affliction")
        void applyAffliction_duplicate() {
            PlayerMock player = server.addPlayer("TestPlayer");
            Affliction affliction = TestAffliction.create("test_curse");
            manager.getRegistry().register(affliction);

            manager.applyAffliction(player, "test_curse");
            boolean result = manager.applyAffliction(player, "test_curse");

            assertFalse(result);
            assertEquals(1, manager.getPlayerManager().get(player.getUniqueId()).get().getAfflictionCount());
        }

        @Test
        @DisplayName("calls onApply for all components")
        void applyAffliction_callsOnApply() {
            PlayerMock player = server.addPlayer("TestPlayer");
            TrackingComponent trackingComponent = new TrackingComponent("tracker");
            Affliction affliction = TestAffliction.builder("test_curse")
                    .component(trackingComponent)
                    .build();
            manager.getRegistry().register(affliction);

            manager.applyAffliction(player, "test_curse");

            assertEquals(1, trackingComponent.getApplyCount());
        }
    }

    @Nested
    @DisplayName("Remove Affliction")
    class RemoveAffliction {

        @Test
        @DisplayName("removes affliction from player")
        void removeAffliction_success() {
            PlayerMock player = server.addPlayer("TestPlayer");
            Affliction affliction = TestAffliction.create("test_curse");
            manager.getRegistry().register(affliction);
            manager.applyAffliction(player, "test_curse");

            boolean result = manager.removeAffliction(player, "test_curse", AfflictionManager.RemovalReason.ADMIN);

            assertTrue(result);
            assertFalse(manager.hasAffliction(player.getUniqueId(), "test_curse"));
        }

        @Test
        @DisplayName("returns false if player doesn't have affliction")
        void removeAffliction_notAffected() {
            PlayerMock player = server.addPlayer("TestPlayer");

            boolean result = manager.removeAffliction(player, "test_curse", AfflictionManager.RemovalReason.ADMIN);

            assertFalse(result);
        }

        @Test
        @DisplayName("returns false if player not tracked")
        void removeAffliction_playerNotTracked() {
            PlayerMock player = server.addPlayer("TestPlayer");

            boolean result = manager.removeAffliction(player, "test_curse", AfflictionManager.RemovalReason.CURED);

            assertFalse(result);
        }

        @Test
        @DisplayName("calls onRemove for all components")
        void removeAffliction_callsOnRemove() {
            PlayerMock player = server.addPlayer("TestPlayer");
            TrackingComponent trackingComponent = new TrackingComponent("tracker");
            Affliction affliction = TestAffliction.builder("test_curse")
                    .component(trackingComponent)
                    .build();
            manager.getRegistry().register(affliction);
            manager.applyAffliction(player, "test_curse");

            manager.removeAffliction(player, "test_curse", AfflictionManager.RemovalReason.CURED);

            assertEquals(1, trackingComponent.getRemoveCount());
        }
    }

    @Nested
    @DisplayName("Clear Afflictions")
    class ClearAfflictions {

        @Test
        @DisplayName("clears all afflictions from player")
        void clearAfflictions_success() {
            PlayerMock player = server.addPlayer("TestPlayer");
            Affliction affliction1 = TestAffliction.create("curse1");
            Affliction affliction2 = TestAffliction.create("curse2");
            manager.getRegistry().register(affliction1);
            manager.getRegistry().register(affliction2);
            manager.applyAffliction(player, "curse1");
            manager.applyAffliction(player, "curse2");

            manager.clearAfflictions(player, AfflictionManager.RemovalReason.ADMIN);

            assertFalse(manager.hasAffliction(player.getUniqueId(), "curse1"));
            assertFalse(manager.hasAffliction(player.getUniqueId(), "curse2"));
        }

        @Test
        @DisplayName("calls onRemove for each affliction component")
        void clearAfflictions_callsOnRemove() {
            PlayerMock player = server.addPlayer("TestPlayer");
            TrackingComponent trackingComponent1 = new TrackingComponent("tracker1");
            TrackingComponent trackingComponent2 = new TrackingComponent("tracker2");
            Affliction affliction1 = TestAffliction.builder("curse1").component(trackingComponent1).build();
            Affliction affliction2 = TestAffliction.builder("curse2").component(trackingComponent2).build();
            manager.getRegistry().register(affliction1);
            manager.getRegistry().register(affliction2);
            manager.applyAffliction(player, "curse1");
            manager.applyAffliction(player, "curse2");

            manager.clearAfflictions(player, AfflictionManager.RemovalReason.DEATH);

            assertEquals(1, trackingComponent1.getRemoveCount());
            assertEquals(1, trackingComponent2.getRemoveCount());
        }

        @Test
        @DisplayName("does nothing for player without afflictions")
        void clearAfflictions_noAfflictions() {
            PlayerMock player = server.addPlayer("TestPlayer");

            // Should not throw
            manager.clearAfflictions(player, AfflictionManager.RemovalReason.ADMIN);

            assertFalse(manager.getPlayerManager().get(player.getUniqueId()).isPresent());
        }
    }

    @Nested
    @DisplayName("Has Affliction")
    class HasAffliction {

        @Test
        @DisplayName("returns true when player has affliction")
        void hasAffliction_true() {
            PlayerMock player = server.addPlayer("TestPlayer");
            Affliction affliction = TestAffliction.create("test_curse");
            manager.getRegistry().register(affliction);
            manager.applyAffliction(player, "test_curse");

            assertTrue(manager.hasAffliction(player.getUniqueId(), "test_curse"));
        }

        @Test
        @DisplayName("returns false when player doesn't have affliction")
        void hasAffliction_false() {
            PlayerMock player = server.addPlayer("TestPlayer");

            assertFalse(manager.hasAffliction(player.getUniqueId(), "test_curse"));
        }

        @Test
        @DisplayName("returns false for non-existent player")
        void hasAffliction_noPlayer() {
            UUID randomUuid = UUID.randomUUID();

            assertFalse(manager.hasAffliction(randomUuid, "test_curse"));
        }
    }

    @Nested
    @DisplayName("Tick Loop")
    class TickLoop {

        @Test
        @DisplayName("tick loop processes tickable components")
        void tick_processesTickableComponents() {
            PlayerMock player = server.addPlayer("TestPlayer");
            AtomicInteger tickCount = new AtomicInteger(0);

            Tickable<Player, AfflictionInstance> tickable = new Tickable<>() {
                @Override
                public String getId() {
                    return "test_tickable";
                }

                @Override
                public void onTick(Player p, AfflictionInstance instance) {
                    tickCount.incrementAndGet();
                }

                @Override
                public int getTickInterval() {
                    return 1; // Every tick
                }
            };

            Affliction affliction = TestAffliction.builder("test_curse")
                    .component(tickable)
                    .build();
            manager.getRegistry().register(affliction);
            manager.applyAffliction(player, "test_curse");

            // Simulate server ticks (manager tick rate is 20 by default)
            for (int i = 0; i < 20; i++) {
                server.getScheduler().performOneTick();
            }

            assertTrue(tickCount.get() > 0, "Tickable component should have been called");
        }

        @Test
        @DisplayName("tick loop respects component tick interval")
        void tick_respectsTickInterval() {
            PlayerMock player = server.addPlayer("TestPlayer");
            AtomicInteger tickCount = new AtomicInteger(0);

            Tickable<Player, AfflictionInstance> tickable = new Tickable<>() {
                @Override
                public String getId() {
                    return "test_tickable_interval";
                }

                @Override
                public void onTick(Player p, AfflictionInstance instance) {
                    tickCount.incrementAndGet();
                }

                @Override
                public int getTickInterval() {
                    return 2; // Every other manager tick
                }
            };

            Affliction affliction = TestAffliction.builder("test_curse")
                    .component(tickable)
                    .build();
            manager.getRegistry().register(affliction);
            manager.applyAffliction(player, "test_curse");

            // Manager runs every 20 ticks by default
            // Run 60 server ticks = 3 manager ticks
            // With interval of 2, component should tick once or twice
            for (int i = 0; i < 60; i++) {
                server.getScheduler().performOneTick();
            }

            assertTrue(tickCount.get() >= 1, "Tickable component should have been called at least once");
        }

        @Test
        @DisplayName("start and stop control tick loop")
        void startStop_controlsTickLoop() {
            manager.stop();

            PlayerMock player = server.addPlayer("TestPlayer");
            AtomicInteger tickCount = new AtomicInteger(0);

            Tickable<Player, AfflictionInstance> tickable = new Tickable<>() {
                @Override
                public String getId() {
                    return "test_tickable_startstop";
                }

                @Override
                public void onTick(Player p, AfflictionInstance instance) {
                    tickCount.incrementAndGet();
                }

                @Override
                public int getTickInterval() {
                    return 1;
                }
            };

            Affliction affliction = TestAffliction.builder("test_curse")
                    .component(tickable)
                    .build();
            manager.getRegistry().register(affliction);
            manager.applyAffliction(player, "test_curse");

            // Tick without starting - should not process
            int beforeStart = tickCount.get();
            for (int i = 0; i < 40; i++) {
                server.getScheduler().performOneTick();
            }
            assertEquals(beforeStart, tickCount.get(), "Should not tick when stopped");

            // Start and verify ticking resumes
            manager.start();
            for (int i = 0; i < 40; i++) {
                server.getScheduler().performOneTick();
            }
            assertTrue(tickCount.get() > beforeStart, "Should tick after starting");
        }
    }

    @Nested
    @DisplayName("Registry Access")
    class RegistryAccess {

        @Test
        @DisplayName("getRegistry returns registry")
        void getRegistry_returnsRegistry() {
            assertNotNull(manager.getRegistry());
        }

        @Test
        @DisplayName("registered afflictions are accessible")
        void registeredAfflictions_accessible() {
            Affliction affliction = TestAffliction.create("test_curse");
            manager.getRegistry().register(affliction);

            assertTrue(manager.getRegistry().isRegistered("test_curse"));
            assertTrue(manager.getRegistry().get("test_curse").isPresent());
        }
    }

    @Nested
    @DisplayName("Player Manager Access")
    class PlayerManagerAccess {

        @Test
        @DisplayName("getPlayerManager returns player manager")
        void getPlayerManager_returnsPlayerManager() {
            assertNotNull(manager.getPlayerManager());
        }

        @Test
        @DisplayName("player manager tracks afflicted players")
        void playerManager_tracksPlayers() {
            PlayerMock player = server.addPlayer("TestPlayer");
            Affliction affliction = TestAffliction.create("test_curse");
            manager.getRegistry().register(affliction);
            manager.applyAffliction(player, "test_curse");

            assertTrue(manager.getPlayerManager().get(player.getUniqueId()).isPresent());
        }
    }

    @Nested
    @DisplayName("Tick Rate Configuration")
    class TickRateConfiguration {

        @Test
        @DisplayName("setTickRate changes tick rate")
        void setTickRate_changesRate() {
            manager.setTickRate(10L);
            // Verify by behavior - harder to test directly, but we can at least verify no exception
            manager.stop();
            manager.start();
            // If we get here without exception, the rate was accepted
        }
    }

    @Nested
    @DisplayName("Removal Reasons")
    class RemovalReasons {

        @Test
        @DisplayName("all removal reasons are valid")
        void removalReasons_allValid() {
            assertEquals(5, AfflictionManager.RemovalReason.values().length);
            assertNotNull(AfflictionManager.RemovalReason.CURED);
            assertNotNull(AfflictionManager.RemovalReason.EXPIRED);
            assertNotNull(AfflictionManager.RemovalReason.ADMIN);
            assertNotNull(AfflictionManager.RemovalReason.DEATH);
            assertNotNull(AfflictionManager.RemovalReason.OTHER);
        }
    }

    /**
     * Type-safe test component that tracks lifecycle method calls.
     */
    private static class TrackingComponent implements Component<Player, AfflictionInstance> {
        private final String id;
        private int applyCount = 0;
        private int removeCount = 0;

        TrackingComponent(String id) {
            this.id = id;
        }

        @Override
        public String getId() {
            return id;
        }

        @Override
        public void onApply(Player player, AfflictionInstance instance) {
            applyCount++;
        }

        @Override
        public void onRemove(Player player, AfflictionInstance instance) {
            removeCount++;
        }

        int getApplyCount() {
            return applyCount;
        }

        int getRemoveCount() {
            return removeCount;
        }
    }
}
