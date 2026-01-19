package com.dnocturne.afflictions.component.curse;

import com.dnocturne.afflictions.affliction.config.curse.ParticleConfig;
import com.dnocturne.afflictions.api.affliction.AfflictionInstance;
import com.dnocturne.basalt.component.Tickable;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

/**
 * Curse particle component that spawns particles around the player.
 *
 * <p>This component spawns configured particles at regular intervals.
 * Particles are spawned at the player's location with a height offset.</p>
 */
public class CurseParticleComponent implements Tickable<Player, AfflictionInstance> {

    private final String id;
    private final ParticleConfig config;

    /**
     * Create a curse particle component from a config.
     *
     * @param id     Component ID
     * @param config The particle configuration
     */
    public CurseParticleComponent(@NotNull String id, @NotNull ParticleConfig config) {
        this.id = id;
        this.config = config;
    }

    @Override
    public @NotNull String getId() {
        return id;
    }

    @Override
    public int getTickInterval() {
        return config.getTickInterval();
    }

    @Override
    public void onTick(@NotNull Player player, @NotNull AfflictionInstance instance) {
        if (!config.isEnabled()) {
            return;
        }

        player.getWorld().spawnParticle(
                config.getType(),
                player.getLocation().add(0, config.getHeightOffset(), 0),
                config.getCount(),
                config.getOffsetX(),
                config.getOffsetY(),
                config.getOffsetZ(),
                config.getSpeed()
        );
    }

    /**
     * Get the configuration.
     */
    public @NotNull ParticleConfig getConfig() {
        return config;
    }
}
