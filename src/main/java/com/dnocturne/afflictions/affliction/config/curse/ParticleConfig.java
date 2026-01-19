package com.dnocturne.afflictions.affliction.config.curse;

import dev.dejvokep.boostedyaml.block.implementation.Section;
import org.bukkit.Particle;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Locale;

/**
 * Configuration for curse particles.
 *
 * <p>Example YAML:</p>
 * <pre>
 * effects:
 *   particles:
 *     enabled: true
 *     type: WITCH
 *     count: 5
 *     offset-x: 0.3
 *     offset-y: 0.5
 *     offset-z: 0.3
 *     speed: 0.01
 *     height-offset: 1.0
 *     tick-interval: 20
 * </pre>
 */
public class ParticleConfig {

    private final boolean enabled;
    private final Particle type;
    private final int count;
    private final double offsetX;
    private final double offsetY;
    private final double offsetZ;
    private final double speed;
    private final double heightOffset;
    private final int tickInterval;

    /**
     * Create a particle config.
     */
    public ParticleConfig(
            boolean enabled,
            @NotNull Particle type,
            int count,
            double offsetX,
            double offsetY,
            double offsetZ,
            double speed,
            double heightOffset,
            int tickInterval) {
        this.enabled = enabled;
        this.type = type;
        this.count = count;
        this.offsetX = offsetX;
        this.offsetY = offsetY;
        this.offsetZ = offsetZ;
        this.speed = speed;
        this.heightOffset = heightOffset;
        this.tickInterval = tickInterval;
    }

    /**
     * Load a particle config from a YAML section.
     *
     * @param section The YAML section containing effect settings
     * @return The loaded particle config, or a disabled config if section is null
     */
    public static @NotNull ParticleConfig fromSection(@Nullable Section section) {
        if (section == null) {
            return disabled();
        }

        Section particleSection = section.getSection("particles");
        if (particleSection == null) {
            return disabled();
        }

        boolean enabled = particleSection.getBoolean("enabled", false);
        String typeName = particleSection.getString("type", "WITCH");
        Particle type = parseParticle(typeName);

        int count = particleSection.getInt("count", 5);
        double offsetX = particleSection.getDouble("offset-x", 0.3);
        double offsetY = particleSection.getDouble("offset-y", 0.5);
        double offsetZ = particleSection.getDouble("offset-z", 0.3);
        double speed = particleSection.getDouble("speed", 0.01);
        double heightOffset = particleSection.getDouble("height-offset", 1.0);
        int tickInterval = particleSection.getInt("tick-interval", 20);

        return new ParticleConfig(enabled, type, count, offsetX, offsetY, offsetZ,
                speed, heightOffset, tickInterval);
    }

    /**
     * Create a disabled particle config.
     */
    public static @NotNull ParticleConfig disabled() {
        return new ParticleConfig(false, Particle.WITCH, 0, 0, 0, 0, 0, 0, 20);
    }

    /**
     * Parse a particle type from a string name.
     */
    private static @NotNull Particle parseParticle(@NotNull String name) {
        try {
            return Particle.valueOf(name.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            return Particle.WITCH;
        }
    }

    /**
     * Check if particles are enabled.
     */
    public boolean isEnabled() {
        return enabled;
    }

    /**
     * Get the particle type.
     */
    public @NotNull Particle getType() {
        return type;
    }

    /**
     * Get the particle count.
     */
    public int getCount() {
        return count;
    }

    /**
     * Get the X offset.
     */
    public double getOffsetX() {
        return offsetX;
    }

    /**
     * Get the Y offset.
     */
    public double getOffsetY() {
        return offsetY;
    }

    /**
     * Get the Z offset.
     */
    public double getOffsetZ() {
        return offsetZ;
    }

    /**
     * Get the particle speed.
     */
    public double getSpeed() {
        return speed;
    }

    /**
     * Get the height offset from player location.
     */
    public double getHeightOffset() {
        return heightOffset;
    }

    /**
     * Get the tick interval.
     */
    public int getTickInterval() {
        return tickInterval;
    }
}
