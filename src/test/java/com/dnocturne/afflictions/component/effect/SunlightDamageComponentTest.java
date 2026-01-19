package com.dnocturne.afflictions.component.effect;

import com.dnocturne.afflictions.TestAffliction;
import com.dnocturne.afflictions.api.affliction.AfflictionInstance;
import com.dnocturne.basalt.util.TimeUtil;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.MockBukkit;
import org.mockbukkit.mockbukkit.ServerMock;
import org.mockbukkit.mockbukkit.entity.PlayerMock;
import org.mockbukkit.mockbukkit.world.WorldMock;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for SunlightDamageComponent.
 */
@DisplayName("SunlightDamageComponent")
class SunlightDamageComponentTest {

    private ServerMock server;
    private WorldMock world;
    private PlayerMock player;
    private SunlightDamageComponent component;
    private AfflictionInstance instance;

    @BeforeEach
    void setUp() {
        server = MockBukkit.mock();
        world = server.addSimpleWorld("test_world");
        player = server.addPlayer("TestPlayer");
        player.teleport(world.getSpawnLocation());

        // Default component with standard settings
        component = new SunlightDamageComponent("test_sun_damage", 2.0, 1, true, 0.5);

        // Create affliction instance
        TestAffliction affliction = TestAffliction.builder("vampirism").maxLevel(5).build();
        instance = new AfflictionInstance(player.getUniqueId(), affliction, 1, -1);
    }

    @AfterEach
    void tearDown() {
        MockBukkit.unmock();
    }

    @Nested
    @DisplayName("Constructor")
    class Constructor {

        @Test
        @DisplayName("creates with all parameters")
        void allParameters() {
            SunlightDamageComponent comp = new SunlightDamageComponent(
                    "custom_id", 3.0, 5, false, 0.75
            );

            assertEquals("custom_id", comp.getId());
            assertEquals(3.0, comp.getBaseDamage());
            assertEquals(5, comp.getTickInterval());
            assertFalse(comp.isCheckWeather());
            assertEquals(0.75, comp.getHelmetDamageReduction());
        }

        @Test
        @DisplayName("creates with minimal parameters (defaults)")
        void minimalParameters() {
            SunlightDamageComponent comp = new SunlightDamageComponent("simple", 1.5, 2);

            assertEquals("simple", comp.getId());
            assertEquals(1.5, comp.getBaseDamage());
            assertEquals(2, comp.getTickInterval());
            assertTrue(comp.isCheckWeather()); // default
            assertEquals(0.5, comp.getHelmetDamageReduction()); // default
        }
    }

    @Nested
    @DisplayName("Sunlight Detection")
    class SunlightDetection {

        @Test
        @DisplayName("damages player during daytime exposed to sun")
        void daytime_exposed_dealsDamage() {
            world.setTime(TimeUtil.NOON); // Clear daytime
            world.setStorm(false);
            double initialHealth = player.getHealth();

            component.onTick(player, instance);

            assertTrue(player.getHealth() < initialHealth);
            assertTrue(instance.getData("burning", Boolean.class));
        }

        @Test
        @DisplayName("no damage during nighttime")
        void nighttime_noDamage() {
            world.setTime(TimeUtil.MIDNIGHT); // Nighttime
            double initialHealth = player.getHealth();

            component.onTick(player, instance);

            assertEquals(initialHealth, player.getHealth());
            assertFalse(instance.getData("burning", Boolean.class));
        }

        @Test
        @DisplayName("no damage during storm when weather check enabled")
        void storm_withWeatherCheck_noDamage() {
            world.setTime(TimeUtil.NOON);
            world.setStorm(true);
            double initialHealth = player.getHealth();

            component.onTick(player, instance);

            assertEquals(initialHealth, player.getHealth());
            assertFalse(instance.getData("burning", Boolean.class));
        }

        @Test
        @DisplayName("takes damage during storm when weather check disabled")
        void storm_withoutWeatherCheck_takesDamage() {
            SunlightDamageComponent noWeatherCheck = new SunlightDamageComponent(
                    "test", 2.0, 1, false, 0.5
            );
            world.setTime(TimeUtil.NOON);
            world.setStorm(true);
            double initialHealth = player.getHealth();

            noWeatherCheck.onTick(player, instance);

            assertTrue(player.getHealth() < initialHealth);
        }
    }

    @Nested
    @DisplayName("Damage Calculation")
    class DamageCalculation {

        @Test
        @DisplayName("base damage at level 1 without helmet")
        void baseDamage_level1_noHelmet() {
            world.setTime(TimeUtil.NOON);
            world.setStorm(false);
            instance.setLevel(1);
            double initialHealth = player.getHealth();

            component.onTick(player, instance);

            // Level 1 = 100% damage = 2.0
            double expectedDamage = 2.0;
            assertEquals(initialHealth - expectedDamage, player.getHealth(), 0.01);
        }

        @Test
        @DisplayName("reduced damage at higher levels")
        void reducedDamage_higherLevels() {
            world.setTime(TimeUtil.NOON);
            world.setStorm(false);

            // Level 3 = 80% damage (1.0 - (3-1) * 0.1 = 0.8)
            instance.setLevel(3);
            double initialHealth = player.getHealth();

            component.onTick(player, instance);

            double expectedDamage = 2.0 * 0.8; // 1.6
            assertEquals(initialHealth - expectedDamage, player.getHealth(), 0.01);
        }

        @Test
        @DisplayName("level 5 has 60% damage")
        void level5_60PercentDamage() {
            world.setTime(TimeUtil.NOON);
            world.setStorm(false);

            // Level 5 = 60% damage (1.0 - (5-1) * 0.1 = 0.6)
            instance.setLevel(5);
            double initialHealth = player.getHealth();

            component.onTick(player, instance);

            double expectedDamage = 2.0 * 0.6; // 1.2
            assertEquals(initialHealth - expectedDamage, player.getHealth(), 0.01);
        }

        @Test
        @DisplayName("helmet reduces damage by 50%")
        void helmet_reducesDamage() {
            world.setTime(TimeUtil.NOON);
            world.setStorm(false);
            instance.setLevel(1);

            // Give player a helmet
            player.getInventory().setHelmet(new ItemStack(Material.IRON_HELMET));
            double initialHealth = player.getHealth();

            component.onTick(player, instance);

            // Level 1 base = 2.0, with helmet = 2.0 * 0.5 = 1.0
            double expectedDamage = 1.0;
            assertEquals(initialHealth - expectedDamage, player.getHealth(), 0.01);
            assertTrue(instance.getData("has_helmet", Boolean.class));
        }

        @Test
        @DisplayName("helmet + level reduction stacks")
        void helmet_and_level_stack() {
            world.setTime(TimeUtil.NOON);
            world.setStorm(false);

            // Level 3 + helmet
            instance.setLevel(3);
            player.getInventory().setHelmet(new ItemStack(Material.DIAMOND_HELMET));
            double initialHealth = player.getHealth();

            component.onTick(player, instance);

            // Level 3 = 80%, helmet = 50% of that
            // 2.0 * 0.8 * 0.5 = 0.8
            double expectedDamage = 0.8;
            assertEquals(initialHealth - expectedDamage, player.getHealth(), 0.01);
        }

        @Test
        @DisplayName("minimum damage is 0.5")
        void minimumDamage() {
            // Create component with very low base damage
            SunlightDamageComponent lowDamage = new SunlightDamageComponent(
                    "low", 0.1, 1, true, 0.5
            );
            world.setTime(TimeUtil.NOON);
            world.setStorm(false);
            instance.setLevel(5); // Max reduction
            player.getInventory().setHelmet(new ItemStack(Material.NETHERITE_HELMET));
            double initialHealth = player.getHealth();

            lowDamage.onTick(player, instance);

            // Damage would be 0.1 * 0.6 * 0.5 = 0.03, but minimum is 0.5
            assertEquals(initialHealth - 0.5, player.getHealth(), 0.01);
        }
    }

    @Nested
    @DisplayName("Visual Effects")
    class VisualEffects {

        @Test
        @DisplayName("sets player on fire when burning")
        void setsOnFire() {
            world.setTime(TimeUtil.NOON);
            world.setStorm(false);
            player.setFireTicks(0);

            component.onTick(player, instance);

            assertTrue(player.getFireTicks() > 0);
            assertEquals(40, player.getFireTicks()); // 2 seconds
        }

        @Test
        @DisplayName("extends fire if already burning with low ticks")
        void extendsFireIfLow() {
            world.setTime(TimeUtil.NOON);
            world.setStorm(false);
            player.setFireTicks(10); // Less than 20

            component.onTick(player, instance);

            assertEquals(40, player.getFireTicks());
        }

        @Test
        @DisplayName("does not reduce existing high fire ticks")
        void doesNotReduceHighFire() {
            world.setTime(TimeUtil.NOON);
            world.setStorm(false);
            player.setFireTicks(100); // More than 20

            component.onTick(player, instance);

            assertEquals(100, player.getFireTicks()); // Should not be reduced
        }
    }

    @Nested
    @DisplayName("Data Keys")
    class DataKeys {

        @Test
        @DisplayName("sets burning=true when exposed")
        void setBurningTrue() {
            world.setTime(TimeUtil.NOON);
            world.setStorm(false);

            component.onTick(player, instance);

            assertTrue(instance.getData("burning", Boolean.class));
        }

        @Test
        @DisplayName("sets burning=false when not exposed")
        void setBurningFalse() {
            world.setTime(TimeUtil.MIDNIGHT);

            component.onTick(player, instance);

            assertFalse(instance.getData("burning", Boolean.class));
        }

        @Test
        @DisplayName("sets has_helmet=true when wearing helmet")
        void setHasHelmetTrue() {
            world.setTime(TimeUtil.NOON);
            world.setStorm(false);
            player.getInventory().setHelmet(new ItemStack(Material.LEATHER_HELMET));

            component.onTick(player, instance);

            assertTrue(instance.getData("has_helmet", Boolean.class));
        }

        @Test
        @DisplayName("sets has_helmet=false when not wearing helmet")
        void setHasHelmetFalse() {
            world.setTime(TimeUtil.NOON);
            world.setStorm(false);
            player.getInventory().setHelmet(null);

            component.onTick(player, instance);

            assertFalse(instance.getData("has_helmet", Boolean.class));
        }
    }
}
