package com.dnocturne.afflictions.command.subcommand.admin;

import com.dnocturne.afflictions.Afflictions;
import com.dnocturne.basalt.command.SubCommand;
import com.dnocturne.basalt.locale.LocalizationManager;
import com.dnocturne.afflictions.locale.MessageKey;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import org.bukkit.command.CommandSender;
import org.incendo.cloud.paper.PaperCommandManager;

/**
 * /afflictions reload - Reload plugin configuration.
 */
public class ReloadCommand implements SubCommand {

    private final Afflictions plugin;

    public ReloadCommand(Afflictions plugin) {
        this.plugin = plugin;
    }

    @Override
    public void register(PaperCommandManager<CommandSourceStack> manager) {
        manager.command(
                manager.commandBuilder("afflictions", "aff", "afflict")
                        .literal("reload")
                        .permission("afflictions.admin.reload")
                        .handler(ctx -> {
                            CommandSender sender = ctx.sender().getSender();
                            reloadConfig(sender);
                        })
        );
    }

    private void reloadConfig(CommandSender sender) {
        LocalizationManager lang = plugin.getLocalizationManager();

        // Reload configuration (methods handle their own exceptions internally)
        plugin.reload();

        lang.send(sender, MessageKey.RELOAD_SUCCESS);
        plugin.getLogger().info("Configuration reloaded by " + sender.getName());
    }
}
