package com.dnocturne.afflictions.command;

import com.dnocturne.afflictions.Afflictions;
import com.dnocturne.afflictions.command.commands.AfflictionsCommand;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import org.incendo.cloud.execution.ExecutionCoordinator;
import org.incendo.cloud.paper.PaperCommandManager;

/**
 * Manages command registration using Cloud command framework.
 * Uses the modern Paper command API (1.20.6+).
 */
public class CommandManager {

    private final Afflictions plugin;
    private final PaperCommandManager<CommandSourceStack> manager;

    public CommandManager(Afflictions plugin) {
        this.plugin = plugin;
        this.manager = PaperCommandManager.builder()
                .executionCoordinator(ExecutionCoordinator.simpleCoordinator())
                .buildOnEnable(plugin);
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
    public PaperCommandManager<CommandSourceStack> getManager() {
        return manager;
    }
}
