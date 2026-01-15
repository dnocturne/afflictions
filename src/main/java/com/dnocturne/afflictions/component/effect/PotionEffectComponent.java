package com.dnocturne.afflictions.component.effect;

import com.dnocturne.afflictions.api.affliction.AfflictionInstance;
import com.dnocturne.afflictions.api.component.effect.Effect;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

/**
 * Applies a potion effect to the player.
 * Reapplies each tick to maintain the effect.
 */
public class PotionEffectComponent implements Effect {

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
        player.addPotionEffect(new PotionEffect(
                effectType,
                duration,
                amplifier,
                ambient,
                particles,
                icon
        ));
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
