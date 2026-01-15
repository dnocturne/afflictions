package com.dnocturne.afflictions;

import org.bukkit.plugin.java.JavaPlugin;

public class Afflictions extends JavaPlugin {

    @Override
    public void onEnable() {
        getLogger().info("Afflictions has been enabled!");
    }

    @Override
    public void onDisable() {
        getLogger().info("Afflictions has been disabled!");
    }
}
