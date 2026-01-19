package com.dnocturne.afflictions.storage;

import com.dnocturne.afflictions.Afflictions;
import com.dnocturne.afflictions.storage.data.PlayerAfflictionData;
import com.dnocturne.afflictions.storage.impl.SQLiteStorage;
import com.dnocturne.basalt.storage.Storage;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Manages storage initialization and provides access to the active storage implementation.
 */
public class StorageManager {

    private static final long INIT_TIMEOUT_SECONDS = 30;
    private static final long SHUTDOWN_TIMEOUT_SECONDS = 10;

    private final Afflictions plugin;
    private final Logger logger;
    private @Nullable Storage<PlayerAfflictionData> storage;

    public StorageManager(Afflictions plugin) {
        this.plugin = plugin;
        this.logger = plugin.getLogger();
    }

    /**
     * Initialize storage based on configuration.
     * Uses a timeout to prevent indefinite blocking if storage is unresponsive.
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

        try {
            boolean success = storage.init().get(INIT_TIMEOUT_SECONDS, TimeUnit.SECONDS);

            if (success) {
                logger.info("Storage initialized: " + storage.getType());
            } else {
                logger.severe("Failed to initialize storage!");
            }

            return success;
        } catch (TimeoutException e) {
            logger.severe("Storage initialization timed out after " + INIT_TIMEOUT_SECONDS + " seconds!");
            return false;
        } catch (ExecutionException e) {
            logger.log(Level.SEVERE, "Storage initialization failed with exception", e.getCause());
            return false;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            logger.severe("Storage initialization was interrupted!");
            return false;
        }
    }

    /**
     * Shutdown the storage connection.
     * Uses a timeout to prevent indefinite blocking during server shutdown.
     */
    public void shutdown() {
        if (storage == null) {
            return;
        }

        try {
            storage.shutdown().get(SHUTDOWN_TIMEOUT_SECONDS, TimeUnit.SECONDS);
            logger.info("Storage shutdown complete");
        } catch (TimeoutException e) {
            logger.warning("Storage shutdown timed out after " + SHUTDOWN_TIMEOUT_SECONDS
                    + " seconds - connection may not be properly closed");
        } catch (ExecutionException e) {
            logger.log(Level.WARNING, "Storage shutdown failed with exception", e.getCause());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            logger.warning("Storage shutdown was interrupted");
        }
    }

    /**
     * Get the active storage implementation.
     */
    public @Nullable Storage<PlayerAfflictionData> getStorage() {
        return storage;
    }

    /**
     * Get the storage type name.
     */
    public @NotNull String getType() {
        return storage != null ? storage.getType() : "none";
    }
}
