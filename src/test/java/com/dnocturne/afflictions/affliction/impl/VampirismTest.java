package com.dnocturne.afflictions.affliction.impl;

import com.dnocturne.afflictions.api.affliction.AfflictionCategory;
import com.dnocturne.afflictions.component.effect.SunlightDamageComponent;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for Vampirism affliction.
 */
@DisplayName("Vampirism")
class VampirismTest {

    @Nested
    @DisplayName("Default Creation")
    class DefaultCreation {

        @Test
        @DisplayName("has correct ID")
        void hasCorrectId() {
            Vampirism vampirism = Vampirism.create();
            assertEquals("vampirism", vampirism.getId());
        }

        @Test
        @DisplayName("has correct display name")
        void hasCorrectDisplayName() {
            Vampirism vampirism = Vampirism.create();
            assertEquals("Vampirism", vampirism.getDisplayName());
        }

        @Test
        @DisplayName("has correct description")
        void hasCorrectDescription() {
            Vampirism vampirism = Vampirism.create();
            assertTrue(vampirism.getDescription().contains("sunlight"));
        }

        @Test
        @DisplayName("has supernatural category")
        void hasSupernaturalCategory() {
            Vampirism vampirism = Vampirism.create();
            assertEquals(AfflictionCategory.SUPERNATURAL, vampirism.getCategory());
        }

        @Test
        @DisplayName("has max level 5")
        void hasMaxLevel5() {
            Vampirism vampirism = Vampirism.create();
            assertEquals(5, vampirism.getMaxLevel());
        }

        @Test
        @DisplayName("is curable by default")
        void isCurable() {
            Vampirism vampirism = Vampirism.create();
            assertTrue(vampirism.isCurable());
        }

        @Test
        @DisplayName("has SunlightDamageComponent")
        void hasSunlightDamageComponent() {
            Vampirism vampirism = Vampirism.create();

            assertTrue(vampirism.hasComponent(SunlightDamageComponent.class));
            assertNotNull(vampirism.getComponent(SunlightDamageComponent.class));
        }

        @Test
        @DisplayName("has exactly one component")
        void hasOneComponent() {
            Vampirism vampirism = Vampirism.create();
            assertEquals(1, vampirism.getComponents().size());
        }
    }

    @Nested
    @DisplayName("Builder Configuration")
    class BuilderConfiguration {

        @Test
        @DisplayName("can customize sun damage")
        void customSunDamage() {
            Vampirism vampirism = Vampirism.builder()
                    .sunDamage(4.0)
                    .build();

            SunlightDamageComponent component = vampirism.getComponent(SunlightDamageComponent.class);
            assertEquals(4.0, component.getBaseDamage());
        }

        @Test
        @DisplayName("can customize damage tick interval")
        void customTickInterval() {
            Vampirism vampirism = Vampirism.builder()
                    .damageTickInterval(5)
                    .build();

            SunlightDamageComponent component = vampirism.getComponent(SunlightDamageComponent.class);
            assertEquals(5, component.getTickInterval());
        }

        @Test
        @DisplayName("can disable weather protection")
        void disableWeatherProtection() {
            Vampirism vampirism = Vampirism.builder()
                    .weatherProtection(false)
                    .build();

            SunlightDamageComponent component = vampirism.getComponent(SunlightDamageComponent.class);
            assertFalse(component.isCheckWeather());
        }

        @Test
        @DisplayName("can customize helmet reduction")
        void customHelmetReduction() {
            Vampirism vampirism = Vampirism.builder()
                    .helmetReduction(0.75)
                    .build();

            SunlightDamageComponent component = vampirism.getComponent(SunlightDamageComponent.class);
            assertEquals(0.75, component.getHelmetDamageReduction());
        }

        @Test
        @DisplayName("can combine multiple settings")
        void combineSettings() {
            Vampirism vampirism = Vampirism.builder()
                    .sunDamage(3.0)
                    .damageTickInterval(2)
                    .weatherProtection(false)
                    .helmetReduction(0.25)
                    .build();

            SunlightDamageComponent component = vampirism.getComponent(SunlightDamageComponent.class);
            assertEquals(3.0, component.getBaseDamage());
            assertEquals(2, component.getTickInterval());
            assertFalse(component.isCheckWeather());
            assertEquals(0.25, component.getHelmetDamageReduction());
        }
    }

    @Nested
    @DisplayName("Default Component Values")
    class DefaultComponentValues {

        @Test
        @DisplayName("default sun damage is 2.0")
        void defaultSunDamage() {
            Vampirism vampirism = Vampirism.create();
            SunlightDamageComponent component = vampirism.getComponent(SunlightDamageComponent.class);
            assertEquals(2.0, component.getBaseDamage());
        }

        @Test
        @DisplayName("default tick interval is 1")
        void defaultTickInterval() {
            Vampirism vampirism = Vampirism.create();
            SunlightDamageComponent component = vampirism.getComponent(SunlightDamageComponent.class);
            assertEquals(1, component.getTickInterval());
        }

        @Test
        @DisplayName("weather protection enabled by default")
        void defaultWeatherProtection() {
            Vampirism vampirism = Vampirism.create();
            SunlightDamageComponent component = vampirism.getComponent(SunlightDamageComponent.class);
            assertTrue(component.isCheckWeather());
        }

        @Test
        @DisplayName("default helmet reduction is 0.5 (50%)")
        void defaultHelmetReduction() {
            Vampirism vampirism = Vampirism.create();
            SunlightDamageComponent component = vampirism.getComponent(SunlightDamageComponent.class);
            assertEquals(0.5, component.getHelmetDamageReduction());
        }
    }

    @Nested
    @DisplayName("Constants")
    class Constants {

        @Test
        @DisplayName("ID constant matches actual ID")
        void idConstant() {
            assertEquals("vampirism", Vampirism.ID);
            assertEquals(Vampirism.ID, Vampirism.create().getId());
        }
    }
}
