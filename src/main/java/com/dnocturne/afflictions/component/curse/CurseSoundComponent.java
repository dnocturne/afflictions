package com.dnocturne.afflictions.component.curse;

import com.dnocturne.afflictions.affliction.config.curse.SoundConfig;
import com.dnocturne.afflictions.api.affliction.AfflictionInstance;
import com.dnocturne.basalt.component.Tickable;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

/**
 * Curse sound component that plays sounds to the player.
 *
 * <p>This component can play sounds periodically, on apply, and/or on remove,
 * based on the configuration.</p>
 */
public class CurseSoundComponent implements Tickable<Player, AfflictionInstance> {

    private final String id;
    private final SoundConfig config;

    /**
     * Create a curse sound component from a config.
     *
     * @param id     Component ID
     * @param config The sound configuration
     */
    public CurseSoundComponent(@NotNull String id, @NotNull SoundConfig config) {
        this.id = id;
        this.config = config;
    }

    @Override
    public @NotNull String getId() {
        return id;
    }

    @Override
    public int getTickInterval() {
        return config.getTickInterval() > 0 ? config.getTickInterval() : 1;
    }

    @Override
    public void onApply(@NotNull Player player, @NotNull AfflictionInstance instance) {
        if (config.isEnabled() && config.isPlayOnApply()) {
            playSound(player);
        }
    }

    @Override
    public void onTick(@NotNull Player player, @NotNull AfflictionInstance instance) {
        if (config.isEnabled() && config.isPeriodicSound()) {
            playSound(player);
        }
    }

    @Override
    public void onRemove(@NotNull Player player, @NotNull AfflictionInstance instance) {
        if (config.isEnabled() && config.isPlayOnRemove()) {
            playSound(player);
        }
    }

    /**
     * Play the configured sound to the player.
     */
    private void playSound(@NotNull Player player) {
        player.playSound(
                player.getLocation(),
                config.getType(),
                config.getVolume(),
                config.getPitch()
        );
    }

    /**
     * Get the configuration.
     */
    public @NotNull SoundConfig getConfig() {
        return config;
    }
}
