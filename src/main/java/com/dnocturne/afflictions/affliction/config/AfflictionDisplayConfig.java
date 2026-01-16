package com.dnocturne.afflictions.affliction.config;

import org.jetbrains.annotations.Nullable;

/**
 * Interface for affliction display configuration.
 * Defines common display properties used by PlaceholderAPI and other integrations.
 *
 * <p>All string values support MiniMessage format and are automatically
 * converted to legacy format for PlaceholderAPI compatibility.</p>
 */
public interface AfflictionDisplayConfig {

    /**
     * Get the affliction ID (e.g., "vampirism", "lycanthropy").
     *
     * @return The unique affliction identifier
     */
    String getId();

    /**
     * Get the name of what the player "is" (e.g., "Vampire", "Werewolf").
     * Used for "You are: {name}"
     *
     * @return The display name
     */
    String getName();

    /**
     * Get the affliction name (e.g., "Vampirism", "Lycanthropy").
     * Used for "Affliction: {affliction}"
     *
     * @return The affliction display name
     */
    String getAfflictionName();

    /**
     * Get the short prefix/tag (e.g., "[V]", "[WW]").
     * Used for chat prefixes, tab, etc.
     *
     * @return The prefix string
     */
    String getPrefix();

    /**
     * Get the description of the affliction.
     *
     * @return The description
     */
    String getDescription();

    /**
     * Get the maximum level for this affliction.
     *
     * @return The maximum level
     */
    int getMaxLevel();

    /**
     * Get the title for a specific level.
     *
     * @param level The affliction level
     * @return The title for the level, or null if not configured
     */
    @Nullable
    String getLevelTitle(int level);

    /**
     * Get the title for a specific level, with fallback.
     *
     * @param level    The affliction level
     * @param fallback The fallback value if no title is configured
     * @return The title for the level, or the fallback
     */
    default String getLevelTitle(int level, String fallback) {
        String title = getLevelTitle(level);
        return title != null ? title : fallback;
    }
}
