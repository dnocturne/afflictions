package com.dnocturne.afflictions.command;

import com.dnocturne.afflictions.Afflictions;
import com.dnocturne.afflictions.command.commands.AfflictionsCommand;
import org.bukkit.command.CommandSender;
import org.incendo.cloud.execution.ExecutionCoordinator;
import org.incendo.cloud.paper.LegacyPaperCommandManager;

/**
 * Manages command registration using Cloud command framework.
 */
public class CommandManager {

    private final Afflictions plugin;
    private final LegacyPaperCommandManager<CommandSender> manager;

    public CommandManager(Afflictions plugin) {
        this.plugin = plugin;
        this.manager = LegacyPaperCommandManager.createNative(
                plugin,
                ExecutionCoordinator.simpleCoordinator()
        );
    }

    /**
     * Register all commands.
     */
    public void registerCommands() {
        new AfflictionsCommand(plugin, manager).register();

        plugin.getLogger().info("Commands registered successfully");
    }

    /**
     * Get the Cloud command manager.
     */
    public LegacyPaperCommandManager<CommandSender> getManager() {
        return manager;
    }
}
