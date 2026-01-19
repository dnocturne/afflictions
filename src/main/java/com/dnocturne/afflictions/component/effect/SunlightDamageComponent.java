package com.dnocturne.afflictions.component.effect;

import com.dnocturne.afflictions.Afflictions;
import com.dnocturne.afflictions.api.affliction.AfflictionInstance;
import com.dnocturne.afflictions.locale.MessageKey;
import com.dnocturne.basalt.component.Tickable;
import com.dnocturne.basalt.condition.Condition;
import com.dnocturne.basalt.condition.PlayerConditions;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

/**
 * Deals damage to the player when exposed to sunlight.
 * Checks for daytime, sky visibility, weather, and helmet protection.
 *
 * <p>Uses the Condition system for reusable environment checks.</p>
 *
 * <p>Integrates with the blood system:</p>
 * <ul>
 *   <li>Drains blood while in sunlight</li>
 *   <li>Applies damage multiplier when blood is empty</li>
 * </ul>
 */
public class SunlightDamageComponent implements Tickable<Player, AfflictionInstance> {

    /**
     * Cached helmet condition to avoid repeated method calls.
     */
    private static final Condition<Player> HELMET_CONDITION = PlayerConditions.hasHelmet();

    private final String id;
    private final double baseDamage;
    private final int tickInterval;
    private final Condition<Player> exposureCondition;
    private final double helmetDamageReduction;
    private final boolean checkWeather;

    // Blood integration settings
    private final boolean bloodEnabled;
    private final double sunDrain;
    private final double emptySunMultiplier;

    // Grace period settings
    private final boolean gracePeriodEnabled;
    private final double graceBaseDuration;
    private final double graceLevelScaling;
    private final boolean graceParticles;
    private final int graceParticleCount;

    /**
     * Create a sunlight damage component with default weather checking.
     *
     * @param id           The component ID
     * @param baseDamage   Base damage per tick
     * @param tickInterval Tick interval (in server ticks)
     */
    public SunlightDamageComponent(@NotNull String id, double baseDamage, int tickInterval) {
        this(id, baseDamage, tickInterval, true, 0.5, false, 0.0, 1.0,
             false, 0.0, 0.0, false, 0);
    }

    /**
     * Create a sunlight damage component with configurable options.
     *
     * @param id                    The component ID
     * @param baseDamage            Base damage per tick
     * @param tickInterval          Tick interval (in server ticks)
     * @param checkWeather          Whether weather (storm) should provide protection
     * @param helmetDamageReduction Damage reduction when wearing helmet (0.0 to 1.0)
     */
    public SunlightDamageComponent(@NotNull String id, double baseDamage, int tickInterval,
                                   boolean checkWeather, double helmetDamageReduction) {
        this(id, baseDamage, tickInterval, checkWeather, helmetDamageReduction, false, 0.0, 1.0,
             false, 0.0, 0.0, false, 0);
    }

    /**
     * Create a sunlight damage component with blood integration.
     *
     * @param id                    The component ID
     * @param baseDamage            Base damage per tick
     * @param tickInterval          Tick interval (in server ticks)
     * @param checkWeather          Whether weather (storm) should provide protection
     * @param helmetDamageReduction Damage reduction when wearing helmet (0.0 to 1.0)
     * @param bloodEnabled          Whether blood system integration is enabled
     * @param sunDrain              Blood drained per tick in sunlight
     * @param emptySunMultiplier    Damage multiplier when blood is empty
     */
    public SunlightDamageComponent(@NotNull String id, double baseDamage, int tickInterval,
                                   boolean checkWeather, double helmetDamageReduction,
                                   boolean bloodEnabled, double sunDrain, double emptySunMultiplier) {
        this(id, baseDamage, tickInterval, checkWeather, helmetDamageReduction,
             bloodEnabled, sunDrain, emptySunMultiplier,
             false, 0.0, 0.0, false, 0);
    }

    /**
     * Create a sunlight damage component with blood integration and grace period.
     *
     * @param id                    The component ID
     * @param baseDamage            Base damage per tick
     * @param tickInterval          Tick interval (in server ticks)
     * @param checkWeather          Whether weather (storm) should provide protection
     * @param helmetDamageReduction Damage reduction when wearing helmet (0.0 to 1.0)
     * @param bloodEnabled          Whether blood system integration is enabled
     * @param sunDrain              Blood drained per tick in sunlight
     * @param emptySunMultiplier    Damage multiplier when blood is empty
     * @param gracePeriodEnabled    Whether grace period before burning is enabled
     * @param graceBaseDuration     Base grace period in seconds
     * @param graceLevelScaling     Additional grace seconds per vampire level
     * @param graceParticles        Whether to show particles during grace period
     * @param graceParticleCount    Number of particles per tick during grace
     */
    public SunlightDamageComponent(@NotNull String id, double baseDamage, int tickInterval,
                                   boolean checkWeather, double helmetDamageReduction,
                                   boolean bloodEnabled, double sunDrain, double emptySunMultiplier,
                                   boolean gracePeriodEnabled, double graceBaseDuration,
                                   double graceLevelScaling, boolean graceParticles, int graceParticleCount) {
        this.id = id;
        this.baseDamage = baseDamage;
        this.tickInterval = tickInterval;
        this.helmetDamageReduction = helmetDamageReduction;
        this.checkWeather = checkWeather;
        this.bloodEnabled = bloodEnabled;
        this.sunDrain = sunDrain;
        this.emptySunMultiplier = emptySunMultiplier;
        this.gracePeriodEnabled = gracePeriodEnabled;
        this.graceBaseDuration = graceBaseDuration;
        this.graceLevelScaling = graceLevelScaling;
        this.graceParticles = graceParticles;
        this.graceParticleCount = graceParticleCount;

        // Build the exposure condition (daytime + sky access + optionally clear weather)
        Condition<Player> condition = PlayerConditions.isDay().and(PlayerConditions.hasSkyAccess());
        if (checkWeather) {
            condition = condition.and(PlayerConditions.isClearWeather());
        }
        this.exposureCondition = condition;
    }

    /**
     * Create a sunlight damage component with a custom exposure condition.
     *
     * @param id                    The component ID
     * @param baseDamage            Base damage per tick
     * @param tickInterval          Tick interval (in server ticks)
     * @param exposureCondition     Custom condition for sunlight exposure
     * @param helmetDamageReduction Damage reduction when wearing helmet (0.0 to 1.0)
     */
    public SunlightDamageComponent(@NotNull String id, double baseDamage, int tickInterval,
                                   @NotNull Condition<Player> exposureCondition, double helmetDamageReduction) {
        this.id = id;
        this.baseDamage = baseDamage;
        this.tickInterval = tickInterval;
        this.exposureCondition = exposureCondition;
        this.helmetDamageReduction = helmetDamageReduction;
        this.checkWeather = true; // Default when using custom condition
        this.bloodEnabled = false;
        this.sunDrain = 0;
        this.emptySunMultiplier = 1.0;
        this.gracePeriodEnabled = false;
        this.graceBaseDuration = 0;
        this.graceLevelScaling = 0;
        this.graceParticles = false;
        this.graceParticleCount = 0;
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
        boolean wasBurning = Boolean.TRUE.equals(instance.getData("burning"));

        if (!exposureCondition.test(player)) {
            // Left sunlight - reset exposure tracking
            instance.setData("burning", false);
            instance.setData("sun_exposure_start", null);
            return;
        }

        // Handle grace period
        if (gracePeriodEnabled) {
            long now = System.currentTimeMillis();
            Long exposureStart = (Long) instance.getData("sun_exposure_start");

            if (exposureStart == null) {
                // Just entered sunlight - start grace period
                instance.setData("sun_exposure_start", now);
                sendMessage(player, MessageKey.VAMPIRISM_SUN_WARNING);
                exposureStart = now;
            }

            // Calculate grace duration based on level
            double graceDuration = graceBaseDuration + ((instance.getLevel() - 1) * graceLevelScaling);
            long graceMillis = (long) (graceDuration * 1000);

            if ((now - exposureStart) < graceMillis) {
                // Still in grace period - show particles but no damage
                if (graceParticles && graceParticleCount > 0) {
                    spawnWarningParticles(player);
                }
                return;
            }
        }

        // Grace period expired (or disabled) - apply burning damage

        // Just started burning - send message
        if (!wasBurning) {
            sendMessage(player, MessageKey.VAMPIRISM_SUN_BURNING);
        }

        instance.setData("burning", true);

        // Drain blood while in sunlight
        if (bloodEnabled && sunDrain > 0) {
            BloodComponent.drainBlood(instance, sunDrain);
        }

        // Calculate damage based on level and helmet
        double damage = calculateDamage(player, instance);

        // Apply damage
        player.damage(damage);

        // Visual feedback - set player on fire briefly
        if (player.getFireTicks() < 20) {
            player.setFireTicks(40); // 2 seconds of fire effect
        }
    }

    /**
     * Spawn warning particles around the player during grace period.
     */
    private void spawnWarningParticles(@NotNull Player player) {
        player.getWorld().spawnParticle(
                Particle.SMOKE,
                player.getLocation().add(0, 1, 0),
                graceParticleCount,
                0.3, 0.5, 0.3, // offset
                0.01 // speed
        );
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

    /**
     * Calculate damage based on affliction level and protection.
     */
    private double calculateDamage(@NotNull Player player, @NotNull AfflictionInstance instance) {
        double damage = baseDamage;

        // Scale damage with level
        int level = instance.getLevel();
        // Higher level = less damage (more control over vampirism)
        // Level 1: full damage, Level 5: 60% damage
        double levelReduction = 1.0 - ((level - 1) * 0.1);
        damage *= levelReduction;

        // Helmet provides damage reduction (uses cached condition)
        boolean hasHelmet = HELMET_CONDITION.test(player);
        instance.setData("has_helmet", hasHelmet);
        if (hasHelmet) {
            damage *= (1.0 - helmetDamageReduction);
        }

        // Apply empty blood multiplier
        if (bloodEnabled && BloodComponent.isEmpty(instance)) {
            damage *= emptySunMultiplier;
        }

        return Math.max(0.5, damage); // Minimum 0.5 damage
    }

    /**
     * Get the base damage per tick.
     *
     * @return The base damage
     */
    public double getBaseDamage() {
        return baseDamage;
    }

    /**
     * Get the condition used for sunlight exposure checks.
     *
     * @return The exposure condition
     */
    public @NotNull Condition<Player> getExposureCondition() {
        return exposureCondition;
    }

    /**
     * Get the helmet damage reduction multiplier.
     *
     * @return The damage reduction (0.0 to 1.0)
     */
    public double getHelmetDamageReduction() {
        return helmetDamageReduction;
    }

    /**
     * Check if weather protection is enabled.
     *
     * @return true if storm/rain provides protection from sunlight
     */
    public boolean isCheckWeather() {
        return checkWeather;
    }
}
