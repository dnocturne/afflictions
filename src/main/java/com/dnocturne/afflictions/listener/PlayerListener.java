package com.dnocturne.afflictions.listener;

import com.dnocturne.afflictions.Afflictions;
import com.dnocturne.afflictions.api.affliction.Affliction;
import com.dnocturne.afflictions.api.affliction.AfflictionInstance;
import com.dnocturne.afflictions.manager.AfflictionManager;
import com.dnocturne.afflictions.player.AfflictedPlayer;
import com.dnocturne.afflictions.storage.Storage;
import com.dnocturne.afflictions.storage.data.AfflictionData;
import com.dnocturne.afflictions.storage.data.PlayerAfflictionData;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

/**
 * Handles player join/quit for loading and saving affliction data.
 */
public class PlayerListener implements Listener {

    private final Afflictions plugin;

    public PlayerListener(Afflictions plugin) {
        this.plugin = plugin;
    }

    /**
     * Register this listener with Bukkit.
     */
    public void register() {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        loadPlayerData(player);
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        savePlayerData(player);
    }

    private void loadPlayerData(Player player) {
        // Null safety checks for managers
        var storageManager = plugin.getStorageManager();
        AfflictionManager afflictionManager = plugin.getAfflictionManager();

        if (storageManager == null || afflictionManager == null) {
            plugin.getLogger().warning("Cannot load player data: managers not initialized");
            return;
        }

        Storage storage = storageManager.getStorage();
        if (storage == null) {
            plugin.getLogger().warning("Cannot load player data: storage not initialized");
            return;
        }

        // Determine lookup method based on config
        CompletableFuture<Optional<PlayerAfflictionData>> loadFuture = getLoadFuture(player, storage);

        loadFuture.thenAccept(dataOpt -> dataOpt.ifPresent(data -> {
            // Run on main thread to interact with Bukkit
            plugin.getServer().getScheduler().runTask(plugin, () -> {
                for (AfflictionData afflictionData : data.getAfflictions()) {
                    Affliction affliction = afflictionManager.getRegistry()
                            .get(afflictionData.getAfflictionId())
                            .orElse(null);

                    if (affliction == null) {
                        plugin.getLogger().warning("Unknown affliction '" + afflictionData.getAfflictionId()
                                + "' for player " + player.getName() + ", skipping");
                        continue;
                    }

                    // Create instance with stored data
                    AfflictionInstance instance = new AfflictionInstance(
                            player.getUniqueId(),
                            affliction,
                            afflictionData.getLevel(),
                            afflictionData.getDuration(),
                            afflictionData.getContractedAt()
                    );

                    // Restore custom data
                    for (Map.Entry<String, String> entry : afflictionData.getData().entrySet()) {
                        instance.setData(entry.getKey(), entry.getValue());
                    }

                    // Add to player
                    AfflictedPlayer afflictedPlayer = afflictionManager.getPlayerManager()
                            .getOrCreate(player.getUniqueId());
                    afflictedPlayer.addAffliction(instance);
                }

                if (!data.getAfflictions().isEmpty()) {
                    plugin.getLogger().info("Loaded " + data.getAfflictions().size()
                            + " affliction(s) for " + player.getName());
                }
            });
        }));
    }

    /**
     * Get the appropriate load future based on player-lookup config setting.
     */
    private CompletableFuture<Optional<PlayerAfflictionData>> getLoadFuture(Player player, Storage storage) {
        var configManager = plugin.getConfigManager();

        // Default to "auto" if config not available
        String lookupMode = configManager != null
                ? configManager.getMainConfig().getString("storage.player-lookup", "auto")
                : "auto";

        boolean useNameLookup = switch (lookupMode.toLowerCase()) {
            case "name" -> true;
            case "uuid" -> false;
            default -> !Bukkit.getOnlineMode(); // "auto" - use name lookup if offline mode
        };

        if (useNameLookup) {
            // First try by name, then fall back to UUID
            return storage.loadPlayerByName(player.getName()).thenCompose(dataOpt -> {
                if (dataOpt.isPresent()) {
                    return CompletableFuture.completedFuture(dataOpt);
                }
                // Fall back to UUID lookup
                return storage.loadPlayer(player.getUniqueId());
            });
        } else {
            return storage.loadPlayer(player.getUniqueId());
        }
    }

    private void savePlayerData(Player player) {
        // Null safety checks for managers
        AfflictionManager afflictionManager = plugin.getAfflictionManager();
        var storageManager = plugin.getStorageManager();

        if (afflictionManager == null || storageManager == null) {
            plugin.getLogger().warning("Cannot save player data: managers not initialized");
            return;
        }

        Storage storage = storageManager.getStorage();
        if (storage == null) {
            plugin.getLogger().warning("Cannot save player data: storage not initialized");
            return;
        }

        AfflictedPlayer afflicted = afflictionManager.getPlayerManager()
                .get(player.getUniqueId())
                .orElse(null);

        if (afflicted == null || !afflicted.hasAnyAffliction()) {
            // No data to save, clean up tracking
            afflictionManager.getPlayerManager().remove(player.getUniqueId());
            return;
        }
        List<AfflictionData> afflictionDataList = new ArrayList<>();

        for (AfflictionInstance instance : afflicted.getAfflictions()) {
            // Convert Object values to Strings for storage
            Map<String, String> stringData = new java.util.HashMap<>();
            for (Map.Entry<String, Object> entry : instance.getAllData().entrySet()) {
                stringData.put(entry.getKey(), String.valueOf(entry.getValue()));
            }

            AfflictionData data = new AfflictionData(
                    instance.getAfflictionId(),
                    instance.getLevel(),
                    instance.getDuration(),
                    instance.getContractedAt(),
                    stringData
            );
            afflictionDataList.add(data);
        }

        // Save with current username for offline-mode support
        PlayerAfflictionData playerData = new PlayerAfflictionData(
                player.getUniqueId(),
                player.getName(),
                afflictionDataList
        );
        storage.savePlayer(playerData).thenRun(() -> {
            plugin.getLogger().info("Saved " + afflictionDataList.size()
                    + " affliction(s) for " + player.getName());
        });

        // Clean up in-memory data
        afflictionManager.getPlayerManager().remove(player.getUniqueId());
    }
}
