package com.dnocturne.afflictions.hook.papi;

import com.dnocturne.afflictions.Afflictions;
import com.dnocturne.afflictions.affliction.config.AfflictionDisplayConfig;
import com.dnocturne.afflictions.api.affliction.AfflictionInstance;
import com.dnocturne.afflictions.player.AfflictedPlayer;
import com.dnocturne.afflictions.util.MessageUtil;
import com.dnocturne.afflictions.util.TimeUtil;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.stream.Collectors;

/**
 * PlaceholderAPI expansion for Afflictions.
 *
 * <p>General placeholders:</p>
 * <ul>
 *   <li>{@code %afflictions_count%} - Total number of active afflictions</li>
 *   <li>{@code %afflictions_list%} - Comma-separated list of affliction IDs</li>
 *   <li>{@code %afflictions_has_any%} - true/false if player has any affliction</li>
 * </ul>
 *
 * <p>Affliction-specific placeholders (replace {@code <id>} with affliction ID, e.g., "vampirism"):</p>
 * <ul>
 *   <li>{@code %afflictions_has_<id>%} - true/false if player has specific affliction</li>
 *   <li>{@code %afflictions_level_<id>%} - Level of affliction (0 if not afflicted)</li>
 *   <li>{@code %afflictions_<id>_name%} - What the player "is" (e.g., "Vampire")</li>
 *   <li>{@code %afflictions_<id>_affliction%} - The affliction name (e.g., "Vampirism")</li>
 *   <li>{@code %afflictions_<id>_prefix%} - Short prefix/tag (e.g., "[V]")</li>
 *   <li>{@code %afflictions_<id>_title%} - Level title (e.g., "Fledgling", "Elder")</li>
 *   <li>{@code %afflictions_<id>_level%} - Level of affliction</li>
 *   <li>{@code %afflictions_<id>_permanent%} - true/false if permanent</li>
 *   <li>{@code %afflictions_<id>_duration%} - Duration in milliseconds (-1 if permanent)</li>
 *   <li>{@code %afflictions_<id>_contracted%} - Timestamp when contracted</li>
 * </ul>
 *
 * <p>Curse placeholders:</p>
 * <ul>
 *   <li>{@code %afflictions_curse_count%} - Number of active curses</li>
 *   <li>{@code %afflictions_curse_list%} - Comma-separated list of curse display names</li>
 * </ul>
 *
 * <p>Custom data placeholders:</p>
 * <ul>
 *   <li>{@code %afflictions_data_<id>_<key>%} - Custom data value stored on affliction instance</li>
 * </ul>
 *
 * <p>Time placeholders (requires online player):</p>
 * <ul>
 *   <li>{@code %afflictions_time%} - Current time of day with symbol (e.g., "☀ Day" or "🌕 Full Moon")</li>
 *   <li>{@code %afflictions_time_raw%} - Just "day" or "night"</li>
 *   <li>{@code %afflictions_moon_phase%} - Current moon phase name (e.g., "Full Moon")</li>
 *   <li>{@code %afflictions_moon_symbol%} - Moon phase symbol (e.g., "🌕")</li>
 * </ul>
 */
public class AfflictionsExpansion extends PlaceholderExpansion {

    private final Afflictions plugin;

    public AfflictionsExpansion(Afflictions plugin) {
        this.plugin = plugin;
    }

    @Override
    public @NotNull String getIdentifier() {
        return "afflictions";
    }

    @Override
    public @NotNull String getAuthor() {
        return plugin.getPluginMeta().getAuthors().stream()
                .findFirst()
                .orElse("dnocturne");
    }

    @Override
    public @NotNull String getVersion() {
        return plugin.getPluginMeta().getVersion();
    }

    @Override
    public boolean persist() {
        // This is an internal expansion, persist through reloads
        return true;
    }

    @Override
    public @Nullable String onRequest(OfflinePlayer player, @NotNull String params) {
        if (player == null) {
            return "";
        }

        Optional<AfflictedPlayer> afflictedOpt = plugin.getAfflictionManager()
                .getPlayerManager()
                .get(player.getUniqueId());

        // %afflictions_count%
        if (params.equalsIgnoreCase("count")) {
            return String.valueOf(afflictedOpt.map(AfflictedPlayer::getAfflictionCount).orElse(0));
        }

        // %afflictions_list%
        if (params.equalsIgnoreCase("list")) {
            return afflictedOpt
                    .map(ap -> ap.getAfflictions().stream()
                            .map(AfflictionInstance::getAfflictionId)
                            .collect(Collectors.joining(", ")))
                    .orElse("");
        }

        // %afflictions_has_any%
        if (params.equalsIgnoreCase("has_any")) {
            return String.valueOf(afflictedOpt.map(AfflictedPlayer::hasAnyAffliction).orElse(false));
        }

        // %afflictions_curse_count% - Number of curses
        if (params.equalsIgnoreCase("curse_count")) {
            return String.valueOf(afflictedOpt.map(AfflictedPlayer::getCurseCount).orElse(0));
        }

        // %afflictions_curse_list% - Comma-separated list of curse display names
        if (params.equalsIgnoreCase("curse_list")) {
            return afflictedOpt
                    .map(ap -> ap.getCurses().stream()
                            .map(instance -> instance.getAffliction().getDisplayName())
                            .collect(Collectors.joining(", ")))
                    .orElse("");
        }

        // Time placeholders - require online player for world access
        if (params.startsWith("time") || params.startsWith("moon")) {
            if (!player.isOnline()) {
                return "";
            }
            Player onlinePlayer = player.getPlayer();
            if (onlinePlayer == null) {
                return "";
            }
            World world = onlinePlayer.getWorld();

            // %afflictions_time% - localized day/night with moon phase
            if (params.equalsIgnoreCase("time")) {
                if (TimeUtil.isDay(world)) {
                    return MessageUtil.toLegacy(plugin.getLocalizationManager().getRaw("time.placeholder.day"));
                } else {
                    TimeUtil.MoonPhase phase = TimeUtil.getMoonPhaseEnum(world);
                    String symbol = getMoonSymbol(phase);
                    String name = getMoonPhaseName(phase);
                    return MessageUtil.toLegacy(symbol + " " + name);
                }
            }

            // %afflictions_time_raw% - just "day" or "night"
            if (params.equalsIgnoreCase("time_raw")) {
                return TimeUtil.isDay(world) ? "day" : "night";
            }

            // %afflictions_moon_phase% - localized moon phase name
            if (params.equalsIgnoreCase("moon_phase")) {
                return MessageUtil.toLegacy(getMoonPhaseName(TimeUtil.getMoonPhaseEnum(world)));
            }

            // %afflictions_moon_symbol% - moon phase symbol from locale
            if (params.equalsIgnoreCase("moon_symbol")) {
                return MessageUtil.toLegacy(getMoonSymbol(TimeUtil.getMoonPhaseEnum(world)));
            }
        }

        // %afflictions_has_<id>%
        if (params.startsWith("has_")) {
            String afflictionId = params.substring(4);
            return String.valueOf(afflictedOpt
                    .map(ap -> ap.hasAffliction(afflictionId))
                    .orElse(false));
        }

        // %afflictions_level_<id>%
        if (params.startsWith("level_")) {
            String afflictionId = params.substring(6);
            return String.valueOf(afflictedOpt
                    .flatMap(ap -> ap.getAffliction(afflictionId))
                    .map(AfflictionInstance::getLevel)
                    .orElse(0));
        }

        // %afflictions_data_<id>_<key>%
        if (params.startsWith("data_")) {
            String rest = params.substring(5);
            int separatorIndex = rest.indexOf('_');
            if (separatorIndex > 0) {
                String afflictionId = rest.substring(0, separatorIndex);
                String key = rest.substring(separatorIndex + 1);
                return afflictedOpt
                        .flatMap(ap -> ap.getAffliction(afflictionId))
                        .map(instance -> {
                            Object value = instance.getData(key);
                            return value != null ? String.valueOf(value) : "";
                        })
                        .orElse("");
            }
        }

        // Affliction-specific placeholders: %afflictions_<id>_<property>%
        // e.g., %afflictions_vampirism_prefix%, %afflictions_vampirism_name%
        int underscoreIndex = params.indexOf('_');
        if (underscoreIndex > 0) {
            String afflictionId = params.substring(0, underscoreIndex);
            String property = params.substring(underscoreIndex + 1);

            // Handle display properties - these return empty string if not afflicted
            // so admins can use them flexibly in formatting
            boolean hasAffliction = afflictedOpt
                    .map(ap -> ap.hasAffliction(afflictionId))
                    .orElse(false);

            // %afflictions_<id>_name% - What the player "is" (e.g., "Vampire")
            if (property.equalsIgnoreCase("name")) {
                return hasAffliction ? getDisplayNameLegacy(afflictionId) : "";
            }

            // %afflictions_<id>_affliction% - The affliction name (e.g., "Vampirism")
            if (property.equalsIgnoreCase("affliction")) {
                return hasAffliction ? getDisplayAfflictionNameLegacy(afflictionId) : "";
            }

            // %afflictions_<id>_prefix% - Short prefix/tag (e.g., "[V]")
            if (property.equalsIgnoreCase("prefix")) {
                return hasAffliction ? getDisplayPrefixLegacy(afflictionId) : "";
            }

            // %afflictions_<id>_title% - Level title (e.g., "Fledgling", "Elder")
            if (property.equalsIgnoreCase("title")) {
                if (!hasAffliction) return "";
                int level = afflictedOpt
                        .flatMap(ap -> ap.getAffliction(afflictionId))
                        .map(AfflictionInstance::getLevel)
                        .orElse(1);
                String title = getLevelTitleLegacy(afflictionId, level);
                return title != null ? title : "";
            }

            // Other properties require the player to have the affliction
            Optional<AfflictionInstance> instanceOpt = afflictedOpt
                    .flatMap(ap -> ap.getAffliction(afflictionId));

            if (instanceOpt.isPresent()) {
                AfflictionInstance instance = instanceOpt.get();

                // Common properties
                switch (property.toLowerCase()) {
                    case "level":
                        return String.valueOf(instance.getLevel());
                    case "permanent":
                        return String.valueOf(instance.isPermanent());
                    case "duration":
                        return String.valueOf(instance.getDuration());
                    case "contracted":
                        return String.valueOf(instance.getContractedAt());
                    default:
                        // Try as data key
                        Object value = instance.getData(property);
                        if (value != null) {
                            return String.valueOf(value);
                        }
                }
            }
        }

        return null;
    }

    /**
     * Get the display config for an affliction, or null if not found.
     */
    private AfflictionDisplayConfig getConfig(String afflictionId) {
        return plugin.getDisplayConfig(afflictionId);
    }

    /**
     * Get the cached legacy-formatted display name for an affliction.
     * Example: "Vampire", "Werewolf"
     */
    private String getDisplayNameLegacy(String afflictionId) {
        AfflictionDisplayConfig config = getConfig(afflictionId);
        return config != null ? config.getNameLegacy() : "";
    }

    /**
     * Get the cached legacy-formatted affliction name.
     * Example: "Vampirism", "Lycanthropy"
     */
    private String getDisplayAfflictionNameLegacy(String afflictionId) {
        AfflictionDisplayConfig config = getConfig(afflictionId);
        return config != null ? config.getAfflictionNameLegacy() : "";
    }

    /**
     * Get the cached legacy-formatted prefix/tag.
     * Example: "[V]", "[WW]"
     */
    private String getDisplayPrefixLegacy(String afflictionId) {
        AfflictionDisplayConfig config = getConfig(afflictionId);
        return config != null ? config.getPrefixLegacy() : "";
    }

    /**
     * Get the cached legacy-formatted level title.
     * Example: "Fledgling", "Elder", "Ancient"
     */
    private String getLevelTitleLegacy(String afflictionId, int level) {
        AfflictionDisplayConfig config = getConfig(afflictionId);
        return config != null ? config.getLevelTitleLegacy(level) : null;
    }

    /**
     * Get the localized moon phase name from the locale file.
     */
    private String getMoonPhaseName(TimeUtil.MoonPhase phase) {
        String key = "time.moon." + getMoonPhaseKey(phase) + ".name";
        String value = plugin.getLocalizationManager().getRaw(key);
        // Fallback to enum display name if not configured
        return value.equals(key) ? phase.getDisplayName() : value;
    }

    /**
     * Get the moon phase symbol from the locale file.
     */
    private String getMoonSymbol(TimeUtil.MoonPhase phase) {
        String key = "time.moon." + getMoonPhaseKey(phase) + ".symbol";
        String value = plugin.getLocalizationManager().getRaw(key);
        // Fallback to enum symbol if not configured
        return value.equals(key) ? phase.getSymbol() : value;
    }

    /**
     * Convert MoonPhase enum to locale key format (e.g., FULL_MOON -> full-moon).
     */
    private String getMoonPhaseKey(TimeUtil.MoonPhase phase) {
        return phase.name().toLowerCase().replace('_', '-');
    }
}
