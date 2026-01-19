package com.dnocturne.afflictions.command.subcommand.player;

import com.dnocturne.afflictions.Afflictions;
import com.dnocturne.afflictions.api.affliction.AfflictionInstance;
import com.dnocturne.basalt.command.SubCommand;
import com.dnocturne.basalt.locale.LocalizationManager;
import com.dnocturne.afflictions.locale.MessageKey;
import com.dnocturne.afflictions.manager.AfflictionManager;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import org.bukkit.entity.Player;
import org.incendo.cloud.paper.PaperCommandManager;
import org.incendo.cloud.suggestion.Suggestion;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import static org.incendo.cloud.parser.standard.StringParser.stringParser;

/**
 * /afflictions info <affliction> - Shows details about an affliction.
 */
public class InfoCommand implements SubCommand {

    private final Afflictions plugin;

    public InfoCommand(Afflictions plugin) {
        this.plugin = plugin;
    }

    @Override
    public void register(PaperCommandManager<CommandSourceStack> manager) {
        manager.command(
                manager.commandBuilder("afflictions", "aff", "afflict")
                        .literal("info")
                        .required("affliction", stringParser(), (ctx, input) -> {
                            if (!(ctx.sender().getSender() instanceof Player player)) {
                                return CompletableFuture.completedFuture(java.util.Collections.emptyList());
                            }
                            AfflictionManager afflictionManager = plugin.getAfflictionManager();
                            return CompletableFuture.completedFuture(
                                    afflictionManager.getPlayerManager()
                                            .get(player.getUniqueId())
                                            .map(afflicted -> afflicted.getAfflictions().stream()
                                                    .map(inst -> Suggestion.suggestion(inst.getAffliction().getId()))
                                                    .toList())
                                            .orElse(java.util.Collections.emptyList())
                            );
                        })
                        .permission("afflictions.info")
                        .handler(ctx -> {
                            if (ctx.sender().getSender() instanceof Player player) {
                                String afflictionId = ctx.get("affliction");
                                showInfo(player, afflictionId);
                            }
                        })
        );
    }

    private void showInfo(Player player, String afflictionId) {
        LocalizationManager lang = plugin.getLocalizationManager();
        AfflictionManager afflictionManager = plugin.getAfflictionManager();

        Optional<AfflictionInstance> instanceOpt = afflictionManager.getPlayerManager()
                .get(player.getUniqueId())
                .flatMap(afflicted -> afflicted.getAffliction(afflictionId));

        if (instanceOpt.isEmpty()) {
            lang.send(player, MessageKey.INVALID_AFFLICTION,
                    LocalizationManager.placeholder("affliction", afflictionId));
            return;
        }

        AfflictionInstance instance = instanceOpt.get();

        lang.send(player, MessageKey.AFFLICTION_INFO_HEADER,
                LocalizationManager.placeholder("affliction", instance.getAffliction().getDisplayName()));

        lang.send(player, MessageKey.AFFLICTION_INFO_LEVEL,
                LocalizationManager.placeholder("level", String.valueOf(instance.getLevel())));

        // Duration
        String durationText = instance.getDuration() < 0 ? "Permanent" : formatDuration(instance.getDuration());
        lang.send(player, MessageKey.AFFLICTION_INFO_DURATION,
                LocalizationManager.placeholder("duration", durationText));

        // Time contracted
        String timeAgo = formatTimeAgo(instance.getContractedAt());
        lang.send(player, MessageKey.AFFLICTION_INFO_CONTRACTED,
                LocalizationManager.placeholder("time", timeAgo));
    }

    private String formatDuration(long ticks) {
        long seconds = ticks / 20;
        long minutes = seconds / 60;
        long hours = minutes / 60;

        if (hours > 0) {
            return hours + "h " + (minutes % 60) + "m";
        } else if (minutes > 0) {
            return minutes + "m " + (seconds % 60) + "s";
        } else {
            return seconds + "s";
        }
    }

    private String formatTimeAgo(long contractedAt) {
        Duration duration = Duration.between(Instant.ofEpochMilli(contractedAt), Instant.now());
        long minutes = duration.toMinutes();
        long hours = duration.toHours();
        long days = duration.toDays();

        if (days > 0) {
            return days + " day" + (days > 1 ? "s" : "");
        } else if (hours > 0) {
            return hours + " hour" + (hours > 1 ? "s" : "");
        } else if (minutes > 0) {
            return minutes + " minute" + (minutes > 1 ? "s" : "");
        } else {
            return "just now";
        }
    }
}
