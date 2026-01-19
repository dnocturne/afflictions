package com.dnocturne.afflictions.storage;

import com.dnocturne.afflictions.Afflictions;
import com.dnocturne.afflictions.storage.data.PlayerAfflictionData;
import com.dnocturne.basalt.storage.Storage;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.MockBukkit;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for StorageManager.
 */
@DisplayName("StorageManager")
class StorageManagerTest {

    private Afflictions plugin;
    private StorageManager storageManager;

    @BeforeEach
    void setUp() {
        MockBukkit.mock();
        plugin = MockBukkit.load(Afflictions.class);
        storageManager = plugin.getStorageManager();
    }

    @AfterEach
    void tearDown() {
        MockBukkit.unmock();
    }

    @Nested
    @DisplayName("Initialization")
    class Initialization {

        @Test
        @DisplayName("storage manager is initialized on plugin enable")
        void storageManager_initializedOnEnable() {
            assertNotNull(storageManager);
        }

        @Test
        @DisplayName("storage is available after initialization")
        void storage_availableAfterInit() {
            assertNotNull(storageManager.getStorage());
        }

        @Test
        @DisplayName("storage type is sqlite by default")
        void storageType_defaultSQLite() {
            assertEquals("sqlite", storageManager.getType());
        }
    }

    @Nested
    @DisplayName("Storage Access")
    class StorageAccess {

        @Test
        @DisplayName("getStorage returns storage implementation")
        void getStorage_returnsStorage() {
            Storage<PlayerAfflictionData> storage = storageManager.getStorage();

            assertNotNull(storage);
            assertEquals("sqlite", storage.getType());
        }

        @Test
        @DisplayName("getType returns storage type name")
        void getType_returnsTypeName() {
            String type = storageManager.getType();

            assertNotNull(type);
            assertFalse(type.isEmpty());
        }
    }

    @Nested
    @DisplayName("Shutdown")
    class Shutdown {

        @Test
        @DisplayName("shutdown completes without error")
        void shutdown_completesWithoutError() {
            // Shutdown should complete gracefully
            storageManager.shutdown();

            // After shutdown, storage may still be accessible but closed
            // The important thing is no exception was thrown
        }

        @Test
        @DisplayName("shutdown handles null storage gracefully")
        void shutdown_handlesNullStorage() {
            // Create a fresh storage manager without initializing
            StorageManager freshManager = new StorageManager(plugin);

            // Should not throw even without initialization
            freshManager.shutdown();
        }
    }

    @Nested
    @DisplayName("Storage Type Detection")
    class StorageTypeDetection {

        @Test
        @DisplayName("getType returns 'none' when storage is null")
        void getType_returnsNoneWhenNull() {
            StorageManager freshManager = new StorageManager(plugin);

            assertEquals("none", freshManager.getType());
        }
    }
}
