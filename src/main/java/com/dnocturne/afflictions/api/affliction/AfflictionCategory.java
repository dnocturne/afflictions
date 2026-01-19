package com.dnocturne.afflictions.api.affliction;

/**
 * Categories of afflictions.
 *
 * <p>Category behaviors:</p>
 * <ul>
 *   <li>{@link #SUPERNATURAL} - Major afflictions (Vampirism, Lycanthropy). By default, only one
 *       supernatural affliction per player unless hybrid mode is enabled.</li>
 *   <li>{@link #CURSE} - Can have multiple curses simultaneously. Gained through various means.</li>
 *   <li>{@link #PHYSICAL} - Physical ailments and conditions.</li>
 *   <li>{@link #MENTAL} - Mental afflictions and madness.</li>
 *   <li>{@link #ENVIRONMENTAL} - Environmental effects and conditions.</li>
 * </ul>
 */
public enum AfflictionCategory {
    /**
     * Major supernatural afflictions (Vampirism, Lycanthropy, etc.).
     * Only one allowed per player by default.
     */
    SUPERNATURAL,

    /**
     * Curses that can be stacked. Players can have multiple curses.
     */
    CURSE,

    /**
     * Physical ailments and conditions.
     */
    PHYSICAL,

    /**
     * Mental afflictions and madness.
     */
    MENTAL,

    /**
     * Environmental effects and conditions.
     */
    ENVIRONMENTAL
}
