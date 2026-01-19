package com.dnocturne.afflictions.component.curse;

import com.dnocturne.afflictions.affliction.config.curse.PotionEffectConfig;
import com.dnocturne.afflictions.api.affliction.AfflictionInstance;
import com.dnocturne.basalt.component.Tickable;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;

/**
 * Curse potion effect component with level-based amplifier scaling.
 *
 * <p>This component applies potion effects that scale with the curse level.
 * It efficiently manages effect reapplication, only refreshing when the
 * effect is missing or about to expire.</p>
 */
public class CursePotionComponent implements Tickable<Player, AfflictionInstance> {

    private static final int REAPPLY_THRESHOLD_TICKS = 40;
    private static final int EFFECT_DURATION_TICKS = 100;

    private final String id;
    private final PotionEffectConfig config;
    private final int tickInterval;

    /**
     * Create a curse potion component from a config.
     *
     * @param id           Component ID
     * @param config       The potion effect configuration
     * @param tickInterval How often to check/apply the effect
     */
    public CursePotionComponent(
            @NotNull String id,
            @NotNull PotionEffectConfig config,
            int tickInterval) {
        this.id = id;
        this.config = config;
        this.tickInterval = tickInterval;
    }

    /**
     * Create a curse potion component from a config with default tick interval.
     *
     * @param id     Component ID
     * @param config The potion effect configuration
     */
    public CursePotionComponent(@NotNull String id, @NotNull PotionEffectConfig config) {
        this(id, config, 1);
    }

    @Override
    public @NotNull String getId() {
        return id;
    }

    @Override
    public int getTickInterval() {
        return tickInterval;
    }

    @Override
    public void onApply(@NotNull Player player, @NotNull AfflictionInstance instance) {
        applyEffect(player, instance);
    }

    @Override
    public void onTick(@NotNull Player player, @NotNull AfflictionInstance instance) {
        if (needsReapplication(player)) {
            applyEffect(player, instance);
        }
    }

    @Override
    public void onRemove(@NotNull Player player, @NotNull AfflictionInstance instance) {
        player.removePotionEffect(config.getType());
    }

    /**
     * Check if the effect needs to be reapplied.
     */
    private boolean needsReapplication(@NotNull Player player) {
        PotionEffect existing = player.getPotionEffect(config.getType());
        if (existing == null) {
            return true;
        }
        return existing.getDuration() <= REAPPLY_THRESHOLD_TICKS;
    }

    /**
     * Apply the potion effect with level-scaled amplifier.
     */
    private void applyEffect(@NotNull Player player, @NotNull AfflictionInstance instance) {
        int amplifier = config.calculateAmplifier(instance.getLevel());

        player.addPotionEffect(new PotionEffect(
                config.getType(),
                EFFECT_DURATION_TICKS,
                amplifier,
                config.isAmbient(),
                config.hasParticles(),
                config.hasIcon()
        ));
    }

    /**
     * Get the potion effect type.
     */
    public @NotNull PotionEffectType getEffectType() {
        return config.getType();
    }

    /**
     * Get the configuration.
     */
    public @NotNull PotionEffectConfig getConfig() {
        return config;
    }
}
