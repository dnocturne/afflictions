package com.dnocturne.afflictions.command.subcommand.admin;

import com.dnocturne.afflictions.Afflictions;
import com.dnocturne.basalt.command.SubCommand;
import com.dnocturne.basalt.locale.LocalizationManager;
import com.dnocturne.afflictions.locale.MessageKey;
import com.dnocturne.afflictions.manager.AfflictionManager;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.incendo.cloud.paper.PaperCommandManager;

import static org.incendo.cloud.bukkit.parser.PlayerParser.playerParser;

/**
 * /afflictions clear <player> - Clear all afflictions from a player.
 */
public class ClearCommand implements SubCommand {

    private final Afflictions plugin;

    public ClearCommand(Afflictions plugin) {
        this.plugin = plugin;
    }

    @Override
    public void register(PaperCommandManager<CommandSourceStack> manager) {
        manager.command(
                manager.commandBuilder("afflictions", "aff", "afflict")
                        .literal("clear")
                        .required("player", playerParser())
                        .permission("afflictions.admin.clear")
                        .handler(ctx -> {
                            CommandSender sender = ctx.sender().getSender();
                            Player target = ctx.get("player");

                            clearAfflictions(sender, target);
                        })
        );
    }

    private void clearAfflictions(CommandSender sender, Player target) {
        LocalizationManager lang = plugin.getLocalizationManager();
        AfflictionManager afflictionManager = plugin.getAfflictionManager();

        afflictionManager.clearAfflictions(target, AfflictionManager.RemovalReason.ADMIN);

        lang.send(sender, MessageKey.ADMIN_CLEAR_SUCCESS,
                LocalizationManager.placeholder("player", target.getName()));
    }
}
