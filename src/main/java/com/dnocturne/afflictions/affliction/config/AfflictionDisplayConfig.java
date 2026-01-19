package com.dnocturne.afflictions.affliction.config;

import org.jetbrains.annotations.NotNull;
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
    @NotNull String getId();

    /**
     * Check if this affliction is enabled.
     *
     * @return true if the affliction is enabled
     */
    boolean isEnabled();

    /**
     * Get the name of what the player "is" (e.g., "Vampire", "Werewolf").
     * Used for "You are: {name}"
     *
     * @return The display name
     */
    @NotNull String getName();

    /**
     * Get the affliction name (e.g., "Vampirism", "Lycanthropy").
     * Used for "Affliction: {affliction}"
     *
     * @return The affliction display name
     */
    @NotNull String getAfflictionName();

    /**
     * Get the short prefix/tag (e.g., "[V]", "[WW]").
     * Used for chat prefixes, tab, etc.
     *
     * @return The prefix string
     */
    @NotNull String getPrefix();

    /**
     * Get the description of the affliction.
     *
     * @return The description
     */
    @NotNull String getDescription();

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
    default @NotNull String getLevelTitle(int level, @NotNull String fallback) {
        String title = getLevelTitle(level);
        return title != null ? title : fallback;
    }

    // ============================================================
    // Cached legacy versions for PlaceholderAPI
    // These avoid repeated MiniMessage parsing on every request
    // ============================================================

    /**
     * Get the cached legacy-formatted name.
     *
     * @return The name converted to legacy color codes
     */
    @NotNull String getNameLegacy();

    /**
     * Get the cached legacy-formatted affliction name.
     *
     * @return The affliction name converted to legacy color codes
     */
    @NotNull String getAfflictionNameLegacy();

    /**
     * Get the cached legacy-formatted prefix.
     *
     * @return The prefix converted to legacy color codes
     */
    @NotNull String getPrefixLegacy();

    /**
     * Get the cached legacy-formatted level title.
     *
     * @param level The affliction level
     * @return The title converted to legacy color codes, or null if not configured
     */
    @Nullable String getLevelTitleLegacy(int level);
}
