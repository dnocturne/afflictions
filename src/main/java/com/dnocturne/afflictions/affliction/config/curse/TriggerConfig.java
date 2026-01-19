package com.dnocturne.afflictions.affliction.config.curse;

import com.dnocturne.basalt.condition.Condition;
import dev.dejvokep.boostedyaml.block.implementation.Section;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Configuration for a curse trigger condition.
 *
 * <p>Triggers determine when a curse's effects are active. They can be:</p>
 * <ul>
 *   <li>{@code always} - Always active</li>
 *   <li>{@code day} / {@code night} - Time-based</li>
 *   <li>{@code full_moon} / {@code new_moon} - Moon phase-based</li>
 *   <li>{@code bright_moon} / {@code dark_moon} - Moon brightness-based</li>
 *   <li>{@code sunlight} - When exposed to sunlight</li>
 *   <li>{@code underground} - When under cover (no sky access)</li>
 *   <li>{@code rain} / {@code storm} / {@code clear} - Weather-based</li>
 *   <li>{@code overworld} / {@code nether} / {@code end} - Dimension-based</li>
 *   <li>{@code has_helmet} / {@code no_helmet} - Armor-based</li>
 * </ul>
 *
 * <p>Triggers can also be inverted with the {@code inverted} option.</p>
 */
public class TriggerConfig {

    private final String type;
    private final boolean inverted;

    /**
     * Create a trigger config.
     *
     * @param type     The trigger type
     * @param inverted Whether to invert the condition
     */
    public TriggerConfig(@NotNull String type, boolean inverted) {
        this.type = type;
        this.inverted = inverted;
    }

    /**
     * Load a trigger config from a YAML section.
     *
     * @param section The YAML section containing trigger settings
     * @return The loaded trigger config, or null if section is null
     */
    public static @Nullable TriggerConfig fromSection(@Nullable Section section) {
        if (section == null) {
            return null;
        }

        String type = section.getString("type", "always");
        boolean inverted = section.getBoolean("inverted", false);

        return new TriggerConfig(type, inverted);
    }

    /**
     * Create a trigger config from a simple type string.
     *
     * @param type The trigger type
     * @return The trigger config
     */
    public static @NotNull TriggerConfig of(@NotNull String type) {
        return new TriggerConfig(type, false);
    }

    /**
     * Get the trigger type.
     */
    public @NotNull String getType() {
        return type;
    }

    /**
     * Check if the trigger is inverted.
     */
    public boolean isInverted() {
        return inverted;
    }

    /**
     * Convert this config to a condition.
     *
     * @return The condition
     */
    public @NotNull Condition<Player> toCondition() {
        return TriggerFactory.fromConfig(this);
    }
}
