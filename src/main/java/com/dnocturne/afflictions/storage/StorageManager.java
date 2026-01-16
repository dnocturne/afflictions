package com.dnocturne.afflictions.storage;

import com.dnocturne.afflictions.Afflictions;
import com.dnocturne.afflictions.storage.impl.SQLiteStorage;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.logging.Logger;

/**
 * Manages storage initialization and provides access to the active storage implementation.
 */
public class StorageManager {

    private final Afflictions plugin;
    private final Logger logger;
    private @Nullable Storage storage;

    public StorageManager(Afflictions plugin) {
        this.plugin = plugin;
        this.logger = plugin.getLogger();
    }

    /**
     * Initialize storage based on configuration.
     *
     * @return true if successful
     */
    public boolean init() {
        String type = plugin.getConfigManager().getMainConfig()
                .getString("storage.type", "sqlite").toLowerCase();

        storage = switch (type) {
            case "mysql", "mariadb" -> {
                logger.warning("MySQL/MariaDB storage not yet implemented, falling back to SQLite");
                yield new SQLiteStorage(plugin);
            }
            default -> new SQLiteStorage(plugin);
        };

        boolean success = storage.init().join();

        if (success) {
            logger.info("Storage initialized: " + storage.getType());
        } else {
            logger.severe("Failed to initialize storage!");
        }

        return success;
    }

    /**
     * Shutdown the storage connection.
     */
    public void shutdown() {
        if (storage != null) {
            storage.shutdown().join();
        }
    }

    /**
     * Get the active storage implementation.
     */
    public @Nullable Storage getStorage() {
        return storage;
    }

    /**
     * Get the storage type name.
     */
    public @NotNull String getType() {
        return storage != null ? storage.getType() : "none";
    }
}
