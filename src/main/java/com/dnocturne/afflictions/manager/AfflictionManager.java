package com.dnocturne.afflictions.manager;

import com.dnocturne.afflictions.Afflictions;
import com.dnocturne.afflictions.api.affliction.Affliction;
import com.dnocturne.afflictions.api.affliction.AfflictionInstance;
import com.dnocturne.afflictions.player.AfflictedPlayer;
import com.dnocturne.basalt.component.Component;
import com.dnocturne.basalt.component.Tickable;
import com.dnocturne.basalt.manager.PlayerManager;
import com.dnocturne.basalt.registry.Registry;
import com.dnocturne.basalt.util.TaskUtil;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.util.Optional;
import java.util.UUID;
import java.util.logging.Logger;

/**
 * Central manager for all affliction operations.
 */
public class AfflictionManager {

    private final Logger logger;
    private final Registry<Affliction> registry;
    private final PlayerManager<AfflictedPlayer> playerManager;

    private BukkitTask tickTask;
    private long tickRate = 20L; // Default: 1 second
    private volatile long tickCount = 0;

    public AfflictionManager(Afflictions plugin) {
        this.logger = plugin.getLogger();
        this.registry = Registry.<Affliction>forIdentifiable("affliction")
                .setDisplayNameExtractor(Affliction::getDisplayName)
                .setLogger(logger);
        this.playerManager = new PlayerManager<>(AfflictedPlayer::new, AfflictedPlayer::hasAnyAffliction);
    }

    /**
     * Start the affliction tick loop.
     */
    public void start() {
        if (tickTask != null) {
            tickTask.cancel();
        }
        tickTask = TaskUtil.runTimer(this::tick, tickRate, tickRate);
        logger.info("Affliction tick loop started (rate: " + tickRate + " ticks)");
    }

    /**
     * Stop the affliction tick loop.
     */
    public void stop() {
        if (tickTask != null) {
            tickTask.cancel();
            tickTask = null;
            logger.info("Affliction tick loop stopped");
        }
    }

    /**
     * Process one tick for all afflicted players.
     */
    private void tick() {
        tickCount++;

        for (AfflictedPlayer afflictedPlayer : playerManager.getFiltered()) {
            Optional<Player> playerOpt = afflictedPlayer.getPlayer();
            if (playerOpt.isEmpty()) continue;

            Player player = playerOpt.get();

            for (AfflictionInstance instance : afflictedPlayer.getAfflictions()) {
                Affliction affliction = instance.getAffliction();

                // Process tickable components
                for (Component<Player, AfflictionInstance> component : affliction.getComponents()) {
                    if (component instanceof Tickable<Player, AfflictionInstance> tickable) {
                        if (tickCount % tickable.getTickInterval() == 0) {
                            tickable.onTick(player, instance);
                        }
                    }
                }
            }
        }
    }

    /**
     * Apply an affliction to a player.
     *
     * @param player       The player to afflict
     * @param afflictionId The affliction type ID
     * @return true if applied, false if already has this affliction or affliction doesn't exist
     */
    public boolean applyAffliction(Player player, String afflictionId) {
        return applyAffliction(player, afflictionId, 1);
    }

    /**
     * Apply an affliction to a player with a specific level.
     *
     * @param player       The player to afflict
     * @param afflictionId The affliction type ID
     * @param level        The affliction level
     * @return true if applied, false if already has this affliction or affliction doesn't exist
     */
    public boolean applyAffliction(Player player, String afflictionId, int level) {
        Optional<Affliction> afflictionOpt = registry.get(afflictionId);
        if (afflictionOpt.isEmpty()) {
            logger.warning("Attempted to apply unknown affliction: " + afflictionId);
            return false;
        }

        Affliction affliction = afflictionOpt.get();
        AfflictedPlayer afflictedPlayer = playerManager.getOrCreate(player.getUniqueId());

        if (afflictedPlayer.hasAffliction(afflictionId)) {
            return false;
        }

        AfflictionInstance instance = new AfflictionInstance(player.getUniqueId(), affliction, level, -1);
        afflictedPlayer.addAffliction(instance);
        playerManager.invalidateFilterCache();

        // Call onApply for all components
        for (Component<Player, AfflictionInstance> component : affliction.getComponents()) {
            component.onApply(player, instance);
        }

        logger.info(player.getName() + " (" + player.getUniqueId() + ") contracted " + affliction.getDisplayName());
        return true;
    }

    /**
     * Remove an affliction from a player.
     *
     * @param player       The player to cure
     * @param afflictionId The affliction type ID
     * @param reason       The reason for removal
     * @return true if removed, false if player didn't have this affliction
     */
    public boolean removeAffliction(Player player, String afflictionId, RemovalReason reason) {
        Optional<AfflictedPlayer> afflictedPlayerOpt = playerManager.get(player.getUniqueId());
        if (afflictedPlayerOpt.isEmpty()) {
            return false;
        }

        AfflictedPlayer afflictedPlayer = afflictedPlayerOpt.get();
        Optional<AfflictionInstance> instanceOpt = afflictedPlayer.removeAffliction(afflictionId);
        if (instanceOpt.isEmpty()) {
            return false;
        }

        playerManager.invalidateFilterCache();

        AfflictionInstance instance = instanceOpt.get();
        Affliction affliction = instance.getAffliction();

        // Call onRemove for all components
        for (Component<Player, AfflictionInstance> component : affliction.getComponents()) {
            component.onRemove(player, instance);
        }

        logger.info(player.getName() + " (" + player.getUniqueId() + ") was cured of " + affliction.getDisplayName() + " (" + reason + ")");
        return true;
    }

    /**
     * Remove all afflictions from a player.
     *
     * @param player The player to clear
     * @param reason The reason for removal
     */
    public void clearAfflictions(Player player, RemovalReason reason) {
        Optional<AfflictedPlayer> afflictedPlayerOpt = playerManager.get(player.getUniqueId());
        if (afflictedPlayerOpt.isEmpty()) return;

        AfflictedPlayer afflictedPlayer = afflictedPlayerOpt.get();
        for (AfflictionInstance instance : afflictedPlayer.getAfflictions()) {
            Affliction affliction = instance.getAffliction();
            for (Component<Player, AfflictionInstance> component : affliction.getComponents()) {
                component.onRemove(player, instance);
            }
        }
        afflictedPlayer.clearAfflictions();
        playerManager.invalidateFilterCache();
    }

    /**
     * Check if a player has a specific affliction.
     */
    public boolean hasAffliction(UUID uuid, String afflictionId) {
        return playerManager.get(uuid)
                .map(p -> p.hasAffliction(afflictionId))
                .orElse(false);
    }

    /**
     * Get the affliction registry.
     */
    public Registry<Affliction> getRegistry() {
        return registry;
    }

    /**
     * Get the player manager.
     */
    public PlayerManager<AfflictedPlayer> getPlayerManager() {
        return playerManager;
    }

    /**
     * Set the tick rate (in ticks).
     */
    public void setTickRate(long tickRate) {
        this.tickRate = tickRate;
    }

    /**
     * Reasons an affliction can be removed.
     */
    public enum RemovalReason {
        CURED,
        EXPIRED,
        ADMIN,
        DEATH,
        OTHER
    }
}
