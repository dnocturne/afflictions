package com.dnocturne.afflictions.command.commands;

import com.dnocturne.afflictions.Afflictions;
import com.dnocturne.afflictions.command.subcommand.SubCommand;
import com.dnocturne.afflictions.command.subcommand.admin.ClearCommand;
import com.dnocturne.afflictions.command.subcommand.admin.GiveCommand;
import com.dnocturne.afflictions.command.subcommand.admin.ReloadCommand;
import com.dnocturne.afflictions.command.subcommand.admin.RemoveCommand;
import com.dnocturne.afflictions.command.subcommand.player.InfoCommand;
import com.dnocturne.afflictions.command.subcommand.player.ListCommand;
import org.bukkit.command.CommandSender;
import org.incendo.cloud.paper.LegacyPaperCommandManager;

import java.util.ArrayList;
import java.util.List;

/**
 * Main /afflictions command that delegates to modular subcommands.
 */
public class AfflictionsCommand {

    private final Afflictions plugin;
    private final LegacyPaperCommandManager<CommandSender> manager;
    private final List<SubCommand> subCommands = new ArrayList<>();

    public AfflictionsCommand(Afflictions plugin, LegacyPaperCommandManager<CommandSender> manager) {
        this.plugin = plugin;
        this.manager = manager;

        // Register player subcommands
        subCommands.add(new ListCommand(plugin));
        subCommands.add(new InfoCommand(plugin));

        // Register admin subcommands
        subCommands.add(new GiveCommand(plugin));
        subCommands.add(new RemoveCommand(plugin));
        subCommands.add(new ClearCommand(plugin));
        subCommands.add(new ReloadCommand(plugin));
    }

    /**
     * Register all subcommands with the command manager.
     */
    public void register() {
        for (SubCommand subCommand : subCommands) {
            subCommand.register(manager);
        }
    }
}
