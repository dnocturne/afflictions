package com.dnocturne.afflictions.component.effect;

import com.dnocturne.afflictions.api.affliction.AfflictionInstance;
import com.dnocturne.basalt.component.Tickable;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

/**
 * Applies a potion effect to the player.
 * Only reapplies when effect is missing or about to expire to avoid allocation overhead.
 */
public class PotionEffectComponent implements Tickable<Player, AfflictionInstance> {

    /**
     * Threshold in ticks before effect expiry to trigger reapplication.
     * Using 40 ticks (2 seconds) provides buffer for tick interval variations.
     */
    private static final int REAPPLY_THRESHOLD_TICKS = 40;

    private final String id;
    private final PotionEffectType effectType;
    private final int amplifier;
    private final int duration;
    private final boolean ambient;
    private final boolean particles;
    private final boolean icon;

    public PotionEffectComponent(String id, PotionEffectType effectType, int amplifier) {
        this(id, effectType, amplifier, 100, true, false, true);
    }

    public PotionEffectComponent(String id, PotionEffectType effectType, int amplifier,
                                  int duration, boolean ambient, boolean particles, boolean icon) {
        this.id = id;
        this.effectType = effectType;
        this.amplifier = amplifier;
        this.duration = duration;
        this.ambient = ambient;
        this.particles = particles;
        this.icon = icon;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public void onTick(Player player, AfflictionInstance instance) {
        // Only reapply if effect is missing or about to expire
        if (!needsReapplication(player)) {
            return;
        }

        player.addPotionEffect(new PotionEffect(
                effectType,
                duration,
                amplifier,
                ambient,
                particles,
                icon
        ));
    }

    /**
     * Check if the effect needs to be reapplied.
     * Returns true if effect is missing, has lower amplifier, or is about to expire.
     */
    private boolean needsReapplication(Player player) {
        PotionEffect active = player.getPotionEffect(effectType);
        if (active == null) {
            return true;
        }
        // Reapply if current amplifier is lower than desired
        if (active.getAmplifier() < amplifier) {
            return true;
        }
        // Reapply if effect is about to expire
        return active.getDuration() <= REAPPLY_THRESHOLD_TICKS;
    }

    @Override
    public void onRemove(Player player, AfflictionInstance instance) {
        player.removePotionEffect(effectType);
    }

    public PotionEffectType getEffectType() {
        return effectType;
    }

    public int getAmplifier() {
        return amplifier;
    }
}
