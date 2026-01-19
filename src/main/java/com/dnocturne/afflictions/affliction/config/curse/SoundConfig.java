package com.dnocturne.afflictions.affliction.config.curse;

import dev.dejvokep.boostedyaml.block.implementation.Section;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.Sound;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Locale;

/**
 * Configuration for curse sounds.
 *
 * <p>Example YAML:</p>
 * <pre>
 * effects:
 *   sound:
 *     enabled: true
 *     type: ENTITY_WITHER_AMBIENT
 *     volume: 0.5
 *     pitch: 1.0
 *     tick-interval: 100
 *     play-on-apply: true
 *     play-on-remove: false
 * </pre>
 */
public class SoundConfig {

    private final boolean enabled;
    private final Sound type;
    private final float volume;
    private final float pitch;
    private final int tickInterval;
    private final boolean playOnApply;
    private final boolean playOnRemove;

    /**
     * Create a sound config.
     */
    public SoundConfig(
            boolean enabled,
            @NotNull Sound type,
            float volume,
            float pitch,
            int tickInterval,
            boolean playOnApply,
            boolean playOnRemove) {
        this.enabled = enabled;
        this.type = type;
        this.volume = volume;
        this.pitch = pitch;
        this.tickInterval = tickInterval;
        this.playOnApply = playOnApply;
        this.playOnRemove = playOnRemove;
    }

    /**
     * Load a sound config from a YAML section.
     *
     * @param section The YAML section containing effect settings
     * @return The loaded sound config, or a disabled config if section is null
     */
    public static @NotNull SoundConfig fromSection(@Nullable Section section) {
        if (section == null) {
            return disabled();
        }

        Section soundSection = section.getSection("sound");
        if (soundSection == null) {
            return disabled();
        }

        boolean enabled = soundSection.getBoolean("enabled", false);
        String typeName = soundSection.getString("type", "ENTITY_WITHER_AMBIENT");
        Sound type = parseSound(typeName);

        float volume = soundSection.getFloat("volume", 0.5f);
        float pitch = soundSection.getFloat("pitch", 1.0f);
        int tickInterval = soundSection.getInt("tick-interval", 100);
        boolean playOnApply = soundSection.getBoolean("play-on-apply", false);
        boolean playOnRemove = soundSection.getBoolean("play-on-remove", false);

        return new SoundConfig(enabled, type, volume, pitch, tickInterval, playOnApply, playOnRemove);
    }

    /**
     * Create a disabled sound config.
     */
    public static @NotNull SoundConfig disabled() {
        return new SoundConfig(false, Sound.ENTITY_WITHER_AMBIENT, 0, 1, 100, false, false);
    }

    /**
     * Parse a sound type from a string name.
     * Supports both formats: "ENTITY_WITHER_AMBIENT" and "entity.wither.ambient"
     */
    private static @NotNull Sound parseSound(@NotNull String name) {
        // Convert to lowercase and replace underscores with dots for NamespacedKey format
        String keyName = name.toLowerCase(Locale.ROOT).replace('_', '.');
        NamespacedKey key = NamespacedKey.minecraft(keyName);
        Sound sound = Registry.SOUNDS.get(key);
        return sound != null ? sound : Sound.ENTITY_WITHER_AMBIENT;
    }

    /**
     * Check if sound is enabled.
     */
    public boolean isEnabled() {
        return enabled;
    }

    /**
     * Get the sound type.
     */
    public @NotNull Sound getType() {
        return type;
    }

    /**
     * Get the volume.
     */
    public float getVolume() {
        return volume;
    }

    /**
     * Get the pitch.
     */
    public float getPitch() {
        return pitch;
    }

    /**
     * Get the tick interval for periodic sounds.
     */
    public int getTickInterval() {
        return tickInterval;
    }

    /**
     * Check if sound plays on apply.
     */
    public boolean isPlayOnApply() {
        return playOnApply;
    }

    /**
     * Check if sound plays on remove.
     */
    public boolean isPlayOnRemove() {
        return playOnRemove;
    }

    /**
     * Check if sound should play periodically.
     */
    public boolean isPeriodicSound() {
        return enabled && tickInterval > 0 && !playOnApply && !playOnRemove;
    }
}
