package com.dnocturne.afflictions.component.curse;

import com.dnocturne.afflictions.affliction.config.curse.DamageConfig;
import com.dnocturne.afflictions.api.affliction.AfflictionInstance;
import com.dnocturne.basalt.component.Tickable;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

/**
 * Curse damage component with level-based damage scaling.
 *
 * <p>This component deals periodic damage that scales with the curse level.
 * Damage can optionally bypass armor.</p>
 */
public class CurseDamageComponent implements Tickable<Player, AfflictionInstance> {

    private final String id;
    private final DamageConfig config;

    /**
     * Create a curse damage component from a config.
     *
     * @param id     Component ID
     * @param config The damage configuration
     */
    public CurseDamageComponent(@NotNull String id, @NotNull DamageConfig config) {
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

        double damage = config.calculateDamage(instance.getLevel());
        if (damage <= 0) {
            return;
        }

        if (config.isBypassArmor()) {
            // Direct health reduction bypasses armor
            double newHealth = Math.max(0.5, player.getHealth() - damage);
            player.setHealth(newHealth);
        } else {
            // Normal damage goes through armor calculations
            player.damage(damage);
        }
    }

    /**
     * Get the configuration.
     */
    public @NotNull DamageConfig getConfig() {
        return config;
    }
}
