package com.dnocturne.afflictions.registry;

import com.dnocturne.afflictions.Afflictions;
import com.dnocturne.afflictions.command.subcommand.SubCommand;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import org.incendo.cloud.paper.PaperCommandManager;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

/**
 * Registry for dynamic subcommand registration.
 * Provides a fluent API for registering subcommands.
 */
public class SubCommandRegistry {

    private final Afflictions plugin;
    private final PaperCommandManager<CommandSourceStack> manager;
    private final List<SubCommand> subCommands = new ArrayList<>();

    private SubCommandRegistry(Afflictions plugin, PaperCommandManager<CommandSourceStack> manager) {
        this.plugin = plugin;
        this.manager = manager;
    }

    /**
     * Create a new registry builder.
     *
     * @param plugin  The plugin instance
     * @param manager The command manager
     * @return A new registry instance
     */
    public static SubCommandRegistry create(Afflictions plugin, PaperCommandManager<CommandSourceStack> manager) {
        return new SubCommandRegistry(plugin, manager);
    }

    /**
     * Register a subcommand using a factory function.
     *
     * @param factory Factory that creates the subcommand given the plugin
     * @return This registry for chaining
     */
    public SubCommandRegistry register(Function<Afflictions, SubCommand> factory) {
        subCommands.add(factory.apply(plugin));
        return this;
    }

    /**
     * Register a subcommand conditionally.
     *
     * @param condition Whether to register this command
     * @param factory   Factory that creates the subcommand given the plugin
     * @return This registry for chaining
     */
    public SubCommandRegistry registerIf(boolean condition, Function<Afflictions, SubCommand> factory) {
        if (condition) {
            subCommands.add(factory.apply(plugin));
        }
        return this;
    }

    /**
     * Register all subcommands with the command manager.
     *
     * @return The number of commands registered
     */
    public int registerAll() {
        for (SubCommand subCommand : subCommands) {
            subCommand.register(manager);
        }
        return subCommands.size();
    }

    /**
     * Get the number of registered subcommands.
     */
    public int size() {
        return subCommands.size();
    }
}
