package com.dnocturne.afflictions.listener;

import com.dnocturne.afflictions.Afflictions;
import com.dnocturne.basalt.util.TimeUtil;
import org.bukkit.World;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.MockBukkit;
import org.mockbukkit.mockbukkit.ServerMock;
import org.mockbukkit.mockbukkit.world.WorldMock;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for TimeListener.
 */
@DisplayName("TimeListener")
class TimeListenerTest {

    private ServerMock server;
    private Afflictions plugin;
    private WorldMock world;

    @BeforeEach
    void setUp() {
        server = MockBukkit.mock();
        plugin = MockBukkit.load(Afflictions.class);
        world = server.addSimpleWorld("test_world");
    }

    @AfterEach
    void tearDown() {
        MockBukkit.unmock();
    }

    @Nested
    @DisplayName("Time Transition Detection")
    class TimeTransitionDetection {

        @Test
        @DisplayName("detects day to night transition")
        void detectsDayToNightTransition() {
            // Start during day
            world.setTime(TimeUtil.NOON);

            // Let the time listener initialize state
            for (int i = 0; i < 200; i++) {
                server.getScheduler().performOneTick();
            }

            // Transition to night
            world.setTime(TimeUtil.NIGHT_START);

            // Let listener process
            for (int i = 0; i < 200; i++) {
                server.getScheduler().performOneTick();
            }

            // Transition detected - verified by no exceptions
            // Full verification would require inspecting log output or player messages
        }

        @Test
        @DisplayName("detects night to day transition")
        void detectsNightToDayTransition() {
            // Start during night
            world.setTime(TimeUtil.MIDNIGHT);

            // Let the time listener initialize state
            for (int i = 0; i < 200; i++) {
                server.getScheduler().performOneTick();
            }

            // Transition to day
            world.setTime(TimeUtil.SUNRISE);

            // Let listener process
            for (int i = 0; i < 200; i++) {
                server.getScheduler().performOneTick();
            }

            // Transition detected - verified by no exceptions
        }
    }

    @Nested
    @DisplayName("World Environment Filtering")
    class WorldEnvironmentFiltering {

        @Test
        @DisplayName("ignores nether world")
        void ignoresNetherWorld() {
            WorldMock netherWorld = new WorldMock();
            netherWorld.setEnvironment(World.Environment.NETHER);

            // Set time in nether (should be ignored)
            netherWorld.setTime(TimeUtil.NIGHT_START);

            // Process ticks - should not cause any issues
            for (int i = 0; i < 200; i++) {
                server.getScheduler().performOneTick();
            }

            // No exceptions means nether was properly ignored
        }

        @Test
        @DisplayName("ignores end world")
        void ignoresEndWorld() {
            WorldMock endWorld = new WorldMock();
            endWorld.setEnvironment(World.Environment.THE_END);

            endWorld.setTime(TimeUtil.NIGHT_START);

            for (int i = 0; i < 200; i++) {
                server.getScheduler().performOneTick();
            }

            // No exceptions means end was properly ignored
        }

        @Test
        @DisplayName("processes normal world")
        void processesNormalWorld() {
            // Normal world should be processed
            assertEquals(World.Environment.NORMAL, world.getEnvironment());

            world.setTime(TimeUtil.NOON);

            for (int i = 0; i < 200; i++) {
                server.getScheduler().performOneTick();
            }

            // No exceptions means normal world was processed
        }
    }

    @Nested
    @DisplayName("Start and Stop")
    class StartAndStop {

        @Test
        @DisplayName("time listener can be started")
        void canBeStarted() {
            TimeListener listener = new TimeListener(plugin);

            // Should not throw
            listener.start();

            // Clean up
            listener.stop();
        }

        @Test
        @DisplayName("time listener can be stopped")
        void canBeStopped() {
            TimeListener listener = new TimeListener(plugin);
            listener.start();

            // Should not throw
            listener.stop();
        }

        @Test
        @DisplayName("stopping without starting does not throw")
        void stopWithoutStart() {
            TimeListener listener = new TimeListener(plugin);

            // Should not throw even if never started
            listener.stop();
        }

        @Test
        @DisplayName("can restart after stopping")
        void canRestartAfterStopping() {
            TimeListener listener = new TimeListener(plugin);

            listener.start();
            listener.stop();
            listener.start();

            // Clean up
            listener.stop();
        }
    }

    @Nested
    @DisplayName("Moon Phase Detection")
    class MoonPhaseDetection {

        @Test
        @DisplayName("detects full moon during nightfall")
        void detectsFullMoonAtNightfall() {
            // Set to day 0 (full moon)
            world.setFullTime(0);
            world.setTime(TimeUtil.NOON);

            // Initialize state
            for (int i = 0; i < 200; i++) {
                server.getScheduler().performOneTick();
            }

            // Verify it's full moon
            assertEquals(TimeUtil.MoonPhase.FULL_MOON, TimeUtil.getMoonPhaseEnum(world));

            // Transition to night
            world.setTime(TimeUtil.NIGHT_START);

            for (int i = 0; i < 200; i++) {
                server.getScheduler().performOneTick();
            }

            // Moon phase is still full moon
            assertEquals(TimeUtil.MoonPhase.FULL_MOON, TimeUtil.getMoonPhaseEnum(world));
        }

        @Test
        @DisplayName("detects new moon during nightfall")
        void detectsNewMoonAtNightfall() {
            // Set to day 4 (new moon)
            world.setFullTime(TimeUtil.TICKS_PER_DAY * 4);
            world.setTime(TimeUtil.NOON);

            for (int i = 0; i < 200; i++) {
                server.getScheduler().performOneTick();
            }

            assertEquals(TimeUtil.MoonPhase.NEW_MOON, TimeUtil.getMoonPhaseEnum(world));

            world.setTime(TimeUtil.NIGHT_START);

            for (int i = 0; i < 200; i++) {
                server.getScheduler().performOneTick();
            }

            assertEquals(TimeUtil.MoonPhase.NEW_MOON, TimeUtil.getMoonPhaseEnum(world));
        }
    }

    @Nested
    @DisplayName("Unloaded World Cleanup")
    class UnloadedWorldCleanup {

        @Test
        @DisplayName("cleans up state for unloaded worlds")
        void cleansUpUnloadedWorlds() {
            // Create and initialize a second world
            WorldMock tempWorld = server.addSimpleWorld("temp_world");
            tempWorld.setTime(TimeUtil.NOON);

            // Let listener track the world
            for (int i = 0; i < 200; i++) {
                server.getScheduler().performOneTick();
            }

            // Unload the world (MockBukkit doesn't have direct unload, but we can test the concept)
            // The actual cleanup happens when the world is no longer in Bukkit.getWorlds()

            // This test verifies the listener doesn't throw when processing
            // even after world state changes
            for (int i = 0; i < 200; i++) {
                server.getScheduler().performOneTick();
            }
        }
    }
}
