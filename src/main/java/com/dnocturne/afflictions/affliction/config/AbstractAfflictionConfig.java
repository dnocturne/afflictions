package com.dnocturne.afflictions.affliction.config;

import com.dnocturne.afflictions.api.affliction.Affliction;
import com.dnocturne.basalt.util.MessageUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

/**
 * Abstract base class for affliction configurations.
 * Handles caching of legacy-formatted strings for PlaceholderAPI performance.
 *
 * <p>All affliction configs have an {@code enabled} setting that can be configured
 * in the YAML file under {@code settings.enabled}.</p>
 *
 * <p>Subclasses must implement:</p>
 * <ul>
 *   <li>{@link #load()} - Load configuration from file</li>
 *   <li>{@link #createAffliction()} - Create the affliction instance</li>
 * </ul>
 *
 * <p>Subclasses should call {@link #buildLegacyCache()} after loading config values.</p>
 */
public abstract class AbstractAfflictionConfig implements AfflictionDisplayConfig {

    // Whether this affliction is enabled
    protected boolean enabled = true;

    // Cached legacy-formatted strings for PlaceholderAPI performance
    private String nameLegacy;
    private String afflictionNameLegacy;
    private String prefixLegacy;
    private final Map<Integer, String> levelTitlesLegacy = new HashMap<>();

    /**
     * Build the legacy cache for all display strings.
     * Call this after loading config values.
     */
    protected void buildLegacyCache() {
        nameLegacy = MessageUtil.toLegacy(getName());
        afflictionNameLegacy = MessageUtil.toLegacy(getAfflictionName());
        prefixLegacy = MessageUtil.toLegacy(getPrefix());

        levelTitlesLegacy.clear();
        for (int level = 1; level <= getMaxLevel(); level++) {
            String title = getLevelTitle(level);
            if (title != null) {
                levelTitlesLegacy.put(level, MessageUtil.toLegacy(title));
            }
        }
    }

    @Override
    public @NotNull String getNameLegacy() {
        return nameLegacy != null ? nameLegacy : "";
    }

    @Override
    public @NotNull String getAfflictionNameLegacy() {
        return afflictionNameLegacy != null ? afflictionNameLegacy : "";
    }

    @Override
    public @NotNull String getPrefixLegacy() {
        return prefixLegacy != null ? prefixLegacy : "";
    }

    @Override
    public @Nullable String getLevelTitleLegacy(int level) {
        return levelTitlesLegacy.get(level);
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    /**
     * Load configuration from file.
     */
    public abstract void load();

    /**
     * Reload configuration from file.
     */
    public abstract void reload();

    /**
     * Create the affliction instance from this configuration.
     *
     * @return The configured affliction
     */
    public abstract @NotNull Affliction createAffliction();
}
