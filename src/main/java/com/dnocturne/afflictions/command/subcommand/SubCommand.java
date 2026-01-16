package com.dnocturne.afflictions.command.subcommand;

import io.papermc.paper.command.brigadier.CommandSourceStack;
import org.incendo.cloud.paper.PaperCommandManager;

/**
 * Interface for modular subcommands.
 */
public interface SubCommand {

    /**
     * Register this subcommand with the command manager.
     *
     * @param manager The Cloud command manager
     */
    void register(PaperCommandManager<CommandSourceStack> manager);
}
