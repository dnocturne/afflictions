package com.dnocturne.afflictions.component.effect;

import com.dnocturne.afflictions.Afflictions;
import com.dnocturne.afflictions.api.affliction.AfflictionInstance;
import com.dnocturne.afflictions.locale.MessageKey;
import com.dnocturne.basalt.component.Tickable;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;

/**
 * Applies debuff effects when blood is critically low (hunger).
 *
 * <p>When blood drops below the configured threshold, slowness and weakness
 * effects are applied. The severity increases as blood approaches zero.</p>
 *
 * <p>Amplifier calculation: base + (1 - blood_percent/threshold) * max_scaling</p>
 * <p>Example: At 10% blood with 20% threshold and max_scaling of 2:</p>
 * <p>amplifier = base + (1 - 0.5) * 2 = base + 1</p>
 */
public class BloodHungerComponent implements Tickable<Player, AfflictionInstance> {

    // Effect duration buffer in ticks (added to tick interval to prevent flickering)
    private static final int EFFECT_DURATION_BUFFER = 10;

    private final String id;
    private final int tickInterval;
    private final double maxBlood;
    private final double threshold;

    // Slowness settings
    private final boolean slownessEnabled;
    private final int slownessBaseAmplifier;
    private final double slownessMaxScaling;

    // Weakness settings
    private final boolean weaknessEnabled;
    private final int weaknessBaseAmplifier;
    private final double weaknessMaxScaling;

    /**
     * Create a blood hunger component with all configurable options.
     *
     * @param id                    Component ID
     * @param tickInterval          How often to check and apply effects (in seconds)
     * @param maxBlood              Maximum blood capacity (for percentage calculation)
     * @param threshold             Blood percentage threshold to trigger hunger (0-100)
     * @param slownessEnabled       Whether slowness effect is enabled
     * @param slownessBaseAmplifier Base slowness amplifier (0 = Slowness I)
     * @param slownessMaxScaling    Additional amplifier scaling as blood approaches 0
     * @param weaknessEnabled       Whether weakness effect is enabled
     * @param weaknessBaseAmplifier Base weakness amplifier (0 = Weakness I)
     * @param weaknessMaxScaling    Additional amplifier scaling as blood approaches 0
     */
    public BloodHungerComponent(
            @NotNull String id,
            int tickInterval,
            double maxBlood,
            double threshold,
            boolean slownessEnabled, int slownessBaseAmplifier, double slownessMaxScaling,
            boolean weaknessEnabled, int weaknessBaseAmplifier, double weaknessMaxScaling) {
        this.id = id;
        this.tickInterval = tickInterval;
        this.maxBlood = maxBlood;
        this.threshold = threshold;
        this.slownessEnabled = slownessEnabled;
        this.slownessBaseAmplifier = slownessBaseAmplifier;
        this.slownessMaxScaling = slownessMaxScaling;
        this.weaknessEnabled = weaknessEnabled;
        this.weaknessBaseAmplifier = weaknessBaseAmplifier;
        this.weaknessMaxScaling = weaknessMaxScaling;
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
    public void onTick(@NotNull Player player, @NotNull AfflictionInstance instance) {
        double currentBlood = BloodComponent.getBlood(instance);
        double bloodPercent = (maxBlood > 0) ? (currentBlood / maxBlood) * 100.0 : 0;

        boolean isHungry = bloodPercent <= threshold;
        boolean wasHungry = Boolean.TRUE.equals(instance.getData("blood_hunger_active"));

        if (isHungry && !wasHungry) {
            // Just started starving - send message
            applyHungerEffects(player, bloodPercent);
            instance.setData("blood_hunger_active", true);
            sendHungerMessage(player, MessageKey.VAMPIRISM_HUNGER_START);
        } else if (isHungry) {
            // Still hungry - just refresh effects
            applyHungerEffects(player, bloodPercent);
        } else if (wasHungry) {
            // Blood recovered above threshold - remove debuffs and notify
            removeHungerEffects(player);
            instance.setData("blood_hunger_active", false);
            sendHungerMessage(player, MessageKey.VAMPIRISM_HUNGER_END);
        }
    }

    /**
     * Send a hunger-related message to the player.
     */
    private void sendHungerMessage(@NotNull Player player, @NotNull String messageKey) {
        Afflictions plugin = Afflictions.getInstance();
        if (plugin != null) {
            plugin.getLocalizationManager().send(player, messageKey);
        }
    }

    @Override
    public void onRemove(@NotNull Player player, @NotNull AfflictionInstance instance) {
        // Clean up effects when affliction is removed
        removeHungerEffects(player);
    }

    /**
     * Apply hunger debuff effects based on current blood percentage.
     *
     * @param player       The player to affect
     * @param bloodPercent Current blood as percentage of max (0-100)
     */
    private void applyHungerEffects(@NotNull Player player, double bloodPercent) {
        // Duration slightly longer than tick interval to prevent flickering
        int duration = (tickInterval * 20) + EFFECT_DURATION_BUFFER;

        // Calculate severity factor: 0 at threshold, 1 at 0% blood
        double severityFactor = (threshold > 0) ? (1.0 - (bloodPercent / threshold)) : 1.0;
        severityFactor = Math.max(0, Math.min(1, severityFactor)); // Clamp to 0-1

        if (slownessEnabled) {
            int amplifier = calculateAmplifier(slownessBaseAmplifier, slownessMaxScaling, severityFactor);
            applyEffect(player, PotionEffectType.SLOWNESS, duration, amplifier);
        }

        if (weaknessEnabled) {
            int amplifier = calculateAmplifier(weaknessBaseAmplifier, weaknessMaxScaling, severityFactor);
            applyEffect(player, PotionEffectType.WEAKNESS, duration, amplifier);
        }
    }

    /**
     * Remove all hunger debuff effects from the player.
     */
    private void removeHungerEffects(@NotNull Player player) {
        if (slownessEnabled) {
            player.removePotionEffect(PotionEffectType.SLOWNESS);
        }
        if (weaknessEnabled) {
            player.removePotionEffect(PotionEffectType.WEAKNESS);
        }
    }

    /**
     * Apply a potion effect with visible particles (debuff indicator).
     */
    private void applyEffect(@NotNull Player player, @NotNull PotionEffectType type, int duration, int amplifier) {
        player.addPotionEffect(new PotionEffect(
                type,
                duration,
                amplifier,
                false,  // not ambient (visible particles for debuff warning)
                true,   // show particles
                true    // show icon
        ));
    }

    /**
     * Calculate the amplifier for an effect based on severity.
     *
     * @param baseAmplifier Base amplifier value
     * @param maxScaling    Maximum additional amplifier at full severity
     * @param severityFactor Severity from 0 (at threshold) to 1 (at 0% blood)
     * @return Final amplifier clamped to 0-255
     */
    private int calculateAmplifier(int baseAmplifier, double maxScaling, double severityFactor) {
        double amplifier = baseAmplifier + (severityFactor * maxScaling);
        return Math.max(0, Math.min(255, (int) amplifier));
    }

    // Getters for testing

    public double getThreshold() {
        return threshold;
    }

    public boolean isSlownessEnabled() {
        return slownessEnabled;
    }

    public boolean isWeaknessEnabled() {
        return weaknessEnabled;
    }

    public double getMaxBlood() {
        return maxBlood;
    }
}
