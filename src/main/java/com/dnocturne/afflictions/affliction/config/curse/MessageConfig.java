package com.dnocturne.afflictions.affliction.config.curse;

import dev.dejvokep.boostedyaml.block.implementation.Section;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Configuration for curse messages.
 *
 * <p>Messages can be displayed when the curse is applied, removed,
 * when effects activate/deactivate, and periodically.</p>
 *
 * <p>Example YAML:</p>
 * <pre>
 * messages:
 *   on-apply: "&cYou have been cursed with {curse}!"
 *   on-remove: "&aThe curse of {curse} has been lifted."
 *   on-effect-start: "&7You feel the curse taking hold..."
 *   on-effect-end: "&7The curse's effects subside momentarily."
 *   periodic: "&8*dark whispers*"
 *   periodic-interval: 600
 * </pre>
 *
 * <p>Placeholders:</p>
 * <ul>
 *   <li>{curse} - The curse display name</li>
 *   <li>{level} - The curse level</li>
 *   <li>{player} - The player name</li>
 * </ul>
 */
public class MessageConfig {

    private final String onApply;
    private final String onRemove;
    private final String onEffectStart;
    private final String onEffectEnd;
    private final String periodic;
    private final int periodicInterval;

    /**
     * Create a message config.
     */
    public MessageConfig(
            @Nullable String onApply,
            @Nullable String onRemove,
            @Nullable String onEffectStart,
            @Nullable String onEffectEnd,
            @Nullable String periodic,
            int periodicInterval) {
        this.onApply = onApply;
        this.onRemove = onRemove;
        this.onEffectStart = onEffectStart;
        this.onEffectEnd = onEffectEnd;
        this.periodic = periodic;
        this.periodicInterval = periodicInterval;
    }

    /**
     * Load a message config from a YAML section.
     *
     * @param section The YAML section containing message settings
     * @return The loaded message config
     */
    public static @NotNull MessageConfig fromSection(@Nullable Section section) {
        if (section == null) {
            return empty();
        }

        Section messageSection = section.getSection("messages");
        if (messageSection == null) {
            return empty();
        }

        String onApply = messageSection.getString("on-apply");
        String onRemove = messageSection.getString("on-remove");
        String onEffectStart = messageSection.getString("on-effect-start");
        String onEffectEnd = messageSection.getString("on-effect-end");
        String periodic = messageSection.getString("periodic");
        int periodicInterval = messageSection.getInt("periodic-interval", 600);

        return new MessageConfig(onApply, onRemove, onEffectStart, onEffectEnd,
                periodic, periodicInterval);
    }

    /**
     * Create an empty message config with no messages.
     */
    public static @NotNull MessageConfig empty() {
        return new MessageConfig(null, null, null, null, null, 600);
    }

    /**
     * Get the message to display when the curse is applied.
     */
    public @Nullable String getOnApply() {
        return onApply;
    }

    /**
     * Get the message to display when the curse is removed.
     */
    public @Nullable String getOnRemove() {
        return onRemove;
    }

    /**
     * Get the message to display when effects activate.
     */
    public @Nullable String getOnEffectStart() {
        return onEffectStart;
    }

    /**
     * Get the message to display when effects deactivate.
     */
    public @Nullable String getOnEffectEnd() {
        return onEffectEnd;
    }

    /**
     * Get the periodic message.
     */
    public @Nullable String getPeriodic() {
        return periodic;
    }

    /**
     * Get the interval for periodic messages (in ticks).
     */
    public int getPeriodicInterval() {
        return periodicInterval;
    }

    /**
     * Check if there is an on-apply message.
     */
    public boolean hasOnApply() {
        return onApply != null && !onApply.isEmpty();
    }

    /**
     * Check if there is an on-remove message.
     */
    public boolean hasOnRemove() {
        return onRemove != null && !onRemove.isEmpty();
    }

    /**
     * Check if there is an on-effect-start message.
     */
    public boolean hasOnEffectStart() {
        return onEffectStart != null && !onEffectStart.isEmpty();
    }

    /**
     * Check if there is an on-effect-end message.
     */
    public boolean hasOnEffectEnd() {
        return onEffectEnd != null && !onEffectEnd.isEmpty();
    }

    /**
     * Check if there is a periodic message.
     */
    public boolean hasPeriodic() {
        return periodic != null && !periodic.isEmpty();
    }
}
