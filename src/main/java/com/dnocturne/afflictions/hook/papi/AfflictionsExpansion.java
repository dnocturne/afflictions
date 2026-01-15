package com.dnocturne.afflictions.hook.papi;

import com.dnocturne.afflictions.Afflictions;
import com.dnocturne.afflictions.api.affliction.AfflictionInstance;
import com.dnocturne.afflictions.player.AfflictedPlayer;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;
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
 *   <li>{@code %afflictions_<id>_prefix%} - Configured prefix (empty if not afflicted)</li>
 *   <li>{@code %afflictions_<id>_name%} - Configured formatted name (empty if not afflicted)</li>
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

            // Handle prefix and name - these return empty string if not afflicted
            // so admins can use them flexibly in formatting
            if (property.equalsIgnoreCase("prefix")) {
                boolean hasAffliction = afflictedOpt
                        .map(ap -> ap.hasAffliction(afflictionId))
                        .orElse(false);
                if (hasAffliction) {
                    return getAfflictionPrefix(afflictionId);
                }
                return "";
            }

            if (property.equalsIgnoreCase("name")) {
                boolean hasAffliction = afflictedOpt
                        .map(ap -> ap.hasAffliction(afflictionId))
                        .orElse(false);
                if (hasAffliction) {
                    return getAfflictionFormattedName(afflictionId);
                }
                return "";
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
     * Get the prefix for an affliction by ID.
     */
    private String getAfflictionPrefix(String afflictionId) {
        return switch (afflictionId.toLowerCase()) {
            case "vampirism" -> plugin.getVampirismConfig() != null
                    ? plugin.getVampirismConfig().getPrefix()
                    : "";
            // Future afflictions:
            // case "lycanthropy" -> plugin.getLycanthropyConfig() != null
            //         ? plugin.getLycanthropyConfig().getPrefix()
            //         : "";
            default -> "";
        };
    }

    /**
     * Get the formatted name for an affliction by ID.
     */
    private String getAfflictionFormattedName(String afflictionId) {
        return switch (afflictionId.toLowerCase()) {
            case "vampirism" -> plugin.getVampirismConfig() != null
                    ? plugin.getVampirismConfig().getFormattedName()
                    : "";
            // Future afflictions:
            // case "lycanthropy" -> plugin.getLycanthropyConfig() != null
            //         ? plugin.getLycanthropyConfig().getFormattedName()
            //         : "";
            default -> "";
        };
    }
}
