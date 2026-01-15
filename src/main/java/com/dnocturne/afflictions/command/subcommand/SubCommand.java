package com.dnocturne.afflictions.command.subcommand;

import org.bukkit.command.CommandSender;
import org.incendo.cloud.paper.LegacyPaperCommandManager;

/**
 * Interface for modular subcommands.
 */
public interface SubCommand {

    /**
     * Register this subcommand with the command manager.
     *
     * @param manager The Cloud command manager
     */
    void register(LegacyPaperCommandManager<CommandSender> manager);
}
