package com.dnocturne.afflictions.command.subcommand.admin;

import com.dnocturne.afflictions.Afflictions;
import com.dnocturne.basalt.command.SubCommand;
import com.dnocturne.afflictions.locale.MessageKey;
import com.dnocturne.basalt.locale.LocalizationManager;
import com.dnocturne.afflictions.manager.AfflictionManager;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.incendo.cloud.paper.PaperCommandManager;
import org.incendo.cloud.suggestion.Suggestion;

import java.util.concurrent.CompletableFuture;

import static org.incendo.cloud.bukkit.parser.PlayerParser.playerParser;
import static org.incendo.cloud.parser.standard.IntegerParser.integerParser;
import static org.incendo.cloud.parser.standard.StringParser.stringParser;

/**
 * /afflictions give <player> <affliction> [level] - Give an affliction to a player.
 */
public class GiveCommand implements SubCommand {

    private final Afflictions plugin;

    public GiveCommand(Afflictions plugin) {
        this.plugin = plugin;
    }

    @Override
    public void register(PaperCommandManager<CommandSourceStack> manager) {
        manager.command(
                manager.commandBuilder("afflictions", "aff", "afflict")
                        .literal("give")
                        .required("player", playerParser())
                        .required("affliction", stringParser(), (ctx, input) ->
                                CompletableFuture.completedFuture(
                                        plugin.getAfflictionManager().getRegistry().getAllIds().stream()
                                                .map(Suggestion::suggestion)
                                                .toList()
                                )
                        )
                        .optional("level", integerParser(1, 100))
                        .permission("afflictions.admin.give")
                        .handler(ctx -> {
                            CommandSender sender = ctx.sender().getSender();
                            Player target = ctx.get("player");
                            String afflictionId = ctx.get("affliction");
                            int level = ctx.getOrDefault("level", 1);

                            giveAffliction(sender, target, afflictionId, level);
                        })
        );
    }

    private void giveAffliction(CommandSender sender, Player target, String afflictionId, int level) {
        LocalizationManager lang = plugin.getLocalizationManager();
        AfflictionManager afflictionManager = plugin.getAfflictionManager();

        if (!afflictionManager.getRegistry().isRegistered(afflictionId)) {
            lang.send(sender, MessageKey.INVALID_AFFLICTION,
                    LocalizationManager.placeholder("affliction", afflictionId));
            return;
        }

        boolean success = afflictionManager.applyAffliction(target, afflictionId, level);

        if (success) {
            lang.send(sender, MessageKey.ADMIN_GIVE_SUCCESS,
                    LocalizationManager.placeholder("affliction", afflictionId),
                    LocalizationManager.placeholder("player", target.getName()));

            // Notify target
            lang.send(target, MessageKey.AFFLICTION_CONTRACTED,
                    LocalizationManager.placeholder("affliction",
                            afflictionManager.getRegistry().get(afflictionId)
                                    .map(a -> a.getDisplayName())
                                    .orElse(afflictionId)));
        } else {
            lang.send(sender, MessageKey.ADMIN_GIVE_FAILED,
                    LocalizationManager.placeholder("affliction", afflictionId),
                    LocalizationManager.placeholder("player", target.getName()));
        }
    }
}
