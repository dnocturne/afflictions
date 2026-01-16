package com.dnocturne.afflictions.command.commands;

import com.dnocturne.afflictions.Afflictions;
import com.dnocturne.afflictions.registry.SubCommandRegistry;
import com.dnocturne.afflictions.command.subcommand.admin.ClearCommand;
import com.dnocturne.afflictions.command.subcommand.admin.GiveCommand;
import com.dnocturne.afflictions.command.subcommand.admin.ReloadCommand;
import com.dnocturne.afflictions.command.subcommand.admin.RemoveCommand;
import com.dnocturne.afflictions.command.subcommand.player.InfoCommand;
import com.dnocturne.afflictions.command.subcommand.player.ListCommand;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import org.incendo.cloud.paper.PaperCommandManager;

/**
 * Main /afflictions command that delegates to modular subcommands.
 */
public class AfflictionsCommand {

    private final SubCommandRegistry registry;

    public AfflictionsCommand(Afflictions plugin, PaperCommandManager<CommandSourceStack> manager) {
        this.registry = SubCommandRegistry.create(plugin, manager)
                // Player subcommands
                .register(ListCommand::new)
                .register(InfoCommand::new)
                // Admin subcommands
                .register(GiveCommand::new)
                .register(RemoveCommand::new)
                .register(ClearCommand::new)
                .register(ReloadCommand::new);
    }

    /**
     * Register all subcommands with the command manager.
     */
    public void register() {
        registry.registerAll();
    }
}
