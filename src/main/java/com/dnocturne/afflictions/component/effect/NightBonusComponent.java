package com.dnocturne.afflictions.component.effect;

import com.dnocturne.afflictions.Afflictions;
import com.dnocturne.afflictions.api.affliction.AfflictionInstance;
import com.dnocturne.afflictions.locale.MessageKey;
import com.dnocturne.basalt.component.Tickable;
import com.dnocturne.basalt.condition.Condition;
import com.dnocturne.basalt.condition.PlayerConditions;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;

/**
 * Applies configurable potion effect bonuses while it's nighttime.
 *
 * <p>Effects are applied with slightly longer duration than the tick interval
 * to prevent flickering. Effects are removed when day arrives.</p>
 *
 * <p>Supports: Speed, Strength, Jump Boost, Night Vision</p>
 */
public class NightBonusComponent implements Tickable<Player, AfflictionInstance> {

    private static final Condition<Player> NIGHT_CONDITION = PlayerConditions.isNight();

    // Effect duration buffer in ticks (added to tick interval to prevent flickering)
    private static final int EFFECT_DURATION_BUFFER = 10;

    // Night vision needs longer duration to avoid the "waning" visual effect
    // Minecraft shows waning when duration < 200 ticks (10 seconds)
    private static final int NIGHT_VISION_MIN_DURATION = 400; // 20 seconds

    private final String id;
    private final int tickInterval;

    // Speed settings
    private final boolean speedEnabled;
    private final int speedBaseAmplifier;
    private final double speedLevelScaling;

    // Strength settings
    private final boolean strengthEnabled;
    private final int strengthBaseAmplifier;
    private final double strengthLevelScaling;

    // Jump settings
    private final boolean jumpEnabled;
    private final int jumpBaseAmplifier;
    private final double jumpLevelScaling;

    // Night vision setting
    private final boolean nightVisionEnabled;

    /**
     * Create a night bonus component with all configurable options.
     */
    public NightBonusComponent(
            @NotNull String id,
            int tickInterval,
            boolean speedEnabled, int speedBaseAmplifier, double speedLevelScaling,
            boolean strengthEnabled, int strengthBaseAmplifier, double strengthLevelScaling,
            boolean jumpEnabled, int jumpBaseAmplifier, double jumpLevelScaling,
            boolean nightVisionEnabled) {
        this.id = id;
        this.tickInterval = tickInterval;
        this.speedEnabled = speedEnabled;
        this.speedBaseAmplifier = speedBaseAmplifier;
        this.speedLevelScaling = speedLevelScaling;
        this.strengthEnabled = strengthEnabled;
        this.strengthBaseAmplifier = strengthBaseAmplifier;
        this.strengthLevelScaling = strengthLevelScaling;
        this.jumpEnabled = jumpEnabled;
        this.jumpBaseAmplifier = jumpBaseAmplifier;
        this.jumpLevelScaling = jumpLevelScaling;
        this.nightVisionEnabled = nightVisionEnabled;
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
        boolean isNight = NIGHT_CONDITION.test(player);
        boolean wasNight = Boolean.TRUE.equals(instance.getData("night_bonuses_active"));
        boolean isHungry = Boolean.TRUE.equals(instance.getData("blood_hunger_active"));

        // Track if it's night (separate from bonuses active, since hunger can suppress bonuses)
        boolean wasNightTime = Boolean.TRUE.equals(instance.getData("is_night_time"));

        // Don't apply night bonuses if blood hunger debuffs are active
        // Blood starvation overrides the benefits of nighttime
        if (isNight && !isHungry) {
            // Night just started (and not hungry) - send night message
            if (!wasNightTime) {
                sendMessage(player, MessageKey.VAMPIRISM_NIGHT_FALLS);
            }
            applyNightBonuses(player, instance);
            instance.setData("night_bonuses_active", true);
        } else if (wasNight) {
            // Day arrived OR hunger kicked in - remove effects
            removeNightBonuses(player);
            instance.setData("night_bonuses_active", false);
        }

        // Track day/night transitions for messages (independent of hunger state)
        if (isNight && !wasNightTime) {
            instance.setData("is_night_time", true);
            // Message already sent above if not hungry, send if hungry too
            if (isHungry) {
                sendMessage(player, MessageKey.VAMPIRISM_NIGHT_FALLS);
            }
        } else if (!isNight && wasNightTime) {
            instance.setData("is_night_time", false);
            sendMessage(player, MessageKey.VAMPIRISM_DAWN_APPROACHES);
        }
    }

    /**
     * Send a message to the player.
     */
    private void sendMessage(@NotNull Player player, @NotNull String messageKey) {
        Afflictions plugin = Afflictions.getInstance();
        if (plugin != null) {
            plugin.getLocalizationManager().send(player, messageKey);
        }
    }

    @Override
    public void onRemove(@NotNull Player player, @NotNull AfflictionInstance instance) {
        // Clean up effects when affliction is removed
        removeNightBonuses(player);
    }

    /**
     * Apply all enabled night bonuses to the player.
     */
    private void applyNightBonuses(@NotNull Player player, @NotNull AfflictionInstance instance) {
        int level = instance.getLevel();
        // Duration slightly longer than tick interval to prevent flickering
        int duration = (tickInterval * 20) + EFFECT_DURATION_BUFFER;

        if (speedEnabled) {
            int amplifier = calculateAmplifier(speedBaseAmplifier, speedLevelScaling, level);
            applyEffect(player, PotionEffectType.SPEED, duration, amplifier);
        }

        if (strengthEnabled) {
            int amplifier = calculateAmplifier(strengthBaseAmplifier, strengthLevelScaling, level);
            applyEffect(player, PotionEffectType.STRENGTH, duration, amplifier);
        }

        if (jumpEnabled) {
            int amplifier = calculateAmplifier(jumpBaseAmplifier, jumpLevelScaling, level);
            applyEffect(player, PotionEffectType.JUMP_BOOST, duration, amplifier);
        }

        if (nightVisionEnabled) {
            // Night vision needs longer duration to avoid the waning visual effect
            int nightVisionDuration = Math.max(duration, NIGHT_VISION_MIN_DURATION);
            applyEffect(player, PotionEffectType.NIGHT_VISION, nightVisionDuration, 0);
        }
    }

    /**
     * Remove all night bonus effects from the player.
     */
    private void removeNightBonuses(@NotNull Player player) {
        if (speedEnabled) {
            player.removePotionEffect(PotionEffectType.SPEED);
        }
        if (strengthEnabled) {
            player.removePotionEffect(PotionEffectType.STRENGTH);
        }
        if (jumpEnabled) {
            player.removePotionEffect(PotionEffectType.JUMP_BOOST);
        }
        if (nightVisionEnabled) {
            player.removePotionEffect(PotionEffectType.NIGHT_VISION);
        }
    }

    /**
     * Apply a potion effect with hidden particles and no icon.
     */
    private void applyEffect(@NotNull Player player, @NotNull PotionEffectType type, int duration, int amplifier) {
        player.addPotionEffect(new PotionEffect(
                type,
                duration,
                amplifier,
                true,   // ambient (less visible particles)
                false,  // no particles
                true    // show icon
        ));
    }

    /**
     * Calculate the amplifier for an effect based on level scaling.
     */
    private int calculateAmplifier(int baseAmplifier, double levelScaling, int level) {
        double amplifier = baseAmplifier + ((level - 1) * levelScaling);
        return Math.max(0, Math.min(255, (int) amplifier));
    }

    // Getters for testing

    public boolean isSpeedEnabled() {
        return speedEnabled;
    }

    public boolean isStrengthEnabled() {
        return strengthEnabled;
    }

    public boolean isJumpEnabled() {
        return jumpEnabled;
    }

    public boolean isNightVisionEnabled() {
        return nightVisionEnabled;
    }
}
