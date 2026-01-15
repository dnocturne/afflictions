package com.dnocturne.afflictions.command.subcommand.admin;

import com.dnocturne.afflictions.Afflictions;
import com.dnocturne.afflictions.command.subcommand.SubCommand;
import com.dnocturne.afflictions.locale.LocalizationManager;
import com.dnocturne.afflictions.locale.MessageKey;
import com.dnocturne.afflictions.manager.AfflictionManager;
import com.dnocturne.afflictions.player.AfflictedPlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.incendo.cloud.paper.LegacyPaperCommandManager;
import org.incendo.cloud.suggestion.Suggestion;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import static org.incendo.cloud.bukkit.parser.PlayerParser.playerParser;
import static org.incendo.cloud.parser.standard.StringParser.stringParser;

/**
 * /afflictions remove <player> <affliction> - Remove an affliction from a player.
 */
public class RemoveCommand implements SubCommand {

    private final Afflictions plugin;

    public RemoveCommand(Afflictions plugin) {
        this.plugin = plugin;
    }

    @Override
    public void register(LegacyPaperCommandManager<CommandSender> manager) {
        manager.command(
                manager.commandBuilder("afflictions", "aff", "afflict")
                        .literal("remove")
                        .required("player", playerParser())
                        .required("affliction", stringParser(), (ctx, input) -> {
                            Player target = ctx.get("player");
                            AfflictionManager afflictionManager = plugin.getAfflictionManager();
                            Optional<AfflictedPlayer> afflictedOpt = afflictionManager.getPlayerManager()
                                    .get(target.getUniqueId());

                            if (afflictedOpt.isEmpty()) {
                                return CompletableFuture.completedFuture(java.util.Collections.emptyList());
                            }

                            return CompletableFuture.completedFuture(
                                    afflictedOpt.get().getAfflictions().stream()
                                            .map(inst -> Suggestion.suggestion(inst.getAffliction().getId()))
                                            .toList()
                            );
                        })
                        .permission("afflictions.admin.remove")
                        .handler(ctx -> {
                            CommandSender sender = ctx.sender();
                            Player target = ctx.get("player");
                            String afflictionId = ctx.get("affliction");

                            removeAffliction(sender, target, afflictionId);
                        })
        );
    }

    private void removeAffliction(CommandSender sender, Player target, String afflictionId) {
        LocalizationManager lang = plugin.getLocalizationManager();
        AfflictionManager afflictionManager = plugin.getAfflictionManager();

        boolean success = afflictionManager.removeAffliction(target, afflictionId, AfflictionManager.RemovalReason.ADMIN);

        if (success) {
            lang.send(sender, MessageKey.ADMIN_REMOVE_SUCCESS,
                    LocalizationManager.placeholder("affliction", afflictionId),
                    LocalizationManager.placeholder("player", target.getName()));

            // Notify target
            lang.send(target, MessageKey.AFFLICTION_CURED,
                    LocalizationManager.placeholder("affliction",
                            afflictionManager.getRegistry().get(afflictionId)
                                    .map(a -> a.getDisplayName())
                                    .orElse(afflictionId)));
        } else {
            lang.send(sender, MessageKey.ADMIN_REMOVE_FAILED,
                    LocalizationManager.placeholder("affliction", afflictionId),
                    LocalizationManager.placeholder("player", target.getName()));
        }
    }
}
