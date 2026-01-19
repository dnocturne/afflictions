package com.dnocturne.afflictions.hook;

import com.dnocturne.afflictions.Afflictions;
import com.dnocturne.afflictions.hook.papi.AfflictionsExpansion;
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
            try {
                new AfflictionsExpansion(plugin).register();
                placeholderApiEnabled = true;
                logger.info("Hooked into PlaceholderAPI - expansion registered");
            } catch (Exception e) {
                logger.warning("Failed to register PlaceholderAPI expansion: " + e.getMessage());
                placeholderApiEnabled = false;
            }
        }
    }

    public boolean isPlaceholderApiEnabled() {
        return placeholderApiEnabled;
    }

    public Afflictions getPlugin() {
        return plugin;
    }
}
