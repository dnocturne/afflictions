package com.dnocturne.afflictions.hook;

import com.dnocturne.afflictions.Afflictions;
import org.bukkit.Bukkit;

import java.util.logging.Logger;

/**
 * Manages soft dependency hooks.
 */
public class HookManager {

    private final Afflictions plugin;
    private final Logger logger;

    private boolean placeholderApiEnabled = false;

    public HookManager(Afflictions plugin) {
        this.plugin = plugin;
        this.logger = plugin.getLogger();
    }

    /**
     * Initialize all hooks.
     */
    public void init() {
        hookPlaceholderApi();
    }

    private void hookPlaceholderApi() {
        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            placeholderApiEnabled = true;
            // Register expansion here when implemented
            logger.info("Hooked into PlaceholderAPI");
        }
    }

    public boolean isPlaceholderApiEnabled() {
        return placeholderApiEnabled;
    }

    public Afflictions getPlugin() {
        return plugin;
    }
}
