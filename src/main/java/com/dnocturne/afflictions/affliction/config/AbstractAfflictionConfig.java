package com.dnocturne.afflictions.affliction.config;

import com.dnocturne.afflictions.util.MessageUtil;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

/**
 * Abstract base class for affliction configurations.
 * Handles caching of legacy-formatted strings for PlaceholderAPI performance.
 *
 * <p>Subclasses should call {@link #buildLegacyCache()} after loading config values.</p>
 */
public abstract class AbstractAfflictionConfig implements AfflictionDisplayConfig {

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
    public String getNameLegacy() {
        return nameLegacy;
    }

    @Override
    public String getAfflictionNameLegacy() {
        return afflictionNameLegacy;
    }

    @Override
    public String getPrefixLegacy() {
        return prefixLegacy;
    }

    @Override
    @Nullable
    public String getLevelTitleLegacy(int level) {
        return levelTitlesLegacy.get(level);
    }
}
