package com.dnocturne.afflictions.config;

import com.dnocturne.afflictions.Afflictions;
import com.dnocturne.basalt.config.ConfigManager;
import dev.dejvokep.boostedyaml.YamlDocument;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.MockBukkit;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for ConfigManager.
 */
@DisplayName("ConfigManager")
class ConfigManagerTest {

    private Afflictions plugin;
    private ConfigManager configManager;

    @BeforeEach
    void setUp() {
        MockBukkit.mock();
        plugin = MockBukkit.load(Afflictions.class);
        configManager = plugin.getConfigManager();
    }

    @AfterEach
    void tearDown() {
        MockBukkit.unmock();
    }

    @Nested
    @DisplayName("Initialization")
    class Initialization {

        @Test
        @DisplayName("config manager is initialized on plugin enable")
        void configManager_initializedOnEnable() {
            assertNotNull(configManager);
        }

        @Test
        @DisplayName("main config is loaded")
        void mainConfig_isLoaded() {
            assertNotNull(configManager.getMainConfig());
        }
    }

    @Nested
    @DisplayName("Main Config Access")
    class MainConfigAccess {

        @Test
        @DisplayName("getMainConfig returns YamlDocument")
        void getMainConfig_returnsYamlDocument() {
            YamlDocument config = configManager.getMainConfig();

            assertNotNull(config);
        }

        @Test
        @DisplayName("can read string values from config")
        void canReadStringValues() {
            YamlDocument config = configManager.getMainConfig();

            // Storage type should have a default
            String storageType = config.getString("storage.type", "sqlite");
            assertNotNull(storageType);
            assertFalse(storageType.isEmpty());
        }

        @Test
        @DisplayName("can read numeric values from config")
        void canReadNumericValues() {
            YamlDocument config = configManager.getMainConfig();

            // Tick rate should have a default
            long tickRate = config.getLong("general.tick-rate", 20L);
            assertTrue(tickRate > 0);
        }

        @Test
        @DisplayName("returns default when key not found")
        void returnsDefaultWhenKeyNotFound() {
            YamlDocument config = configManager.getMainConfig();

            String value = config.getString("nonexistent.key", "default_value");
            assertEquals("default_value", value);
        }
    }

    @Nested
    @DisplayName("Config Reload")
    class ConfigReload {

        @Test
        @DisplayName("reload does not throw")
        void reload_doesNotThrow() {
            // Reload should complete without exception
            configManager.reload();
        }

        @Test
        @DisplayName("config is still accessible after reload")
        void configAccessibleAfterReload() {
            configManager.reload();

            assertNotNull(configManager.getMainConfig());
        }
    }

    @Nested
    @DisplayName("Config Save")
    class ConfigSave {

        @Test
        @DisplayName("save does not throw")
        void save_doesNotThrow() {
            // Save should complete without exception
            configManager.save();
        }
    }

    @Nested
    @DisplayName("Named Config Access")
    class NamedConfigAccess {

        @Test
        @DisplayName("getConfig returns main config by name")
        void getConfig_returnsMainConfig() {
            YamlDocument config = configManager.getConfig("config");

            assertNotNull(config);
            assertSame(configManager.getMainConfig(), config);
        }

        @Test
        @DisplayName("getConfig returns null for unknown name")
        void getConfig_returnsNullForUnknown() {
            YamlDocument config = configManager.getConfig("nonexistent");

            assertNull(config);
        }
    }

    @Nested
    @DisplayName("Storage Configuration")
    class StorageConfiguration {

        @Test
        @DisplayName("storage type defaults to sqlite")
        void storageType_defaultsSqlite() {
            YamlDocument config = configManager.getMainConfig();

            String type = config.getString("storage.type", "sqlite");
            assertEquals("sqlite", type.toLowerCase());
        }

        @Test
        @DisplayName("player lookup mode has default")
        void playerLookup_hasDefault() {
            YamlDocument config = configManager.getMainConfig();

            String lookupMode = config.getString("storage.player-lookup", "auto");
            assertNotNull(lookupMode);
        }
    }

    @Nested
    @DisplayName("General Configuration")
    class GeneralConfiguration {

        @Test
        @DisplayName("tick rate has valid default")
        void tickRate_hasValidDefault() {
            YamlDocument config = configManager.getMainConfig();

            long tickRate = config.getLong("general.tick-rate", 20L);
            assertTrue(tickRate > 0, "Tick rate should be positive");
            assertTrue(tickRate <= 1200, "Tick rate should be reasonable");
        }
    }

    @Nested
    @DisplayName("Fresh ConfigManager")
    class FreshConfigManager {

        @Test
        @DisplayName("new ConfigManager has null main config before load")
        void newConfigManager_nullBeforeLoad() {
            ConfigManager freshManager = new ConfigManager(plugin);

            // Before load() is called, main config should be null
            assertNull(freshManager.getMainConfig());
        }

        @Test
        @DisplayName("load populates main config")
        void load_populatesMainConfig() {
            ConfigManager freshManager = new ConfigManager(plugin);

            freshManager.load();

            assertNotNull(freshManager.getMainConfig());
        }
    }
}
