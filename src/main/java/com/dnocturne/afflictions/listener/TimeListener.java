package com.dnocturne.afflictions.listener;

import com.dnocturne.afflictions.Afflictions;
import com.dnocturne.basalt.listener.TimeTransitionListener;
import com.dnocturne.basalt.locale.LocalizationManager;
import com.dnocturne.basalt.util.TimeUtil.MoonPhase;
import org.bukkit.World;
import org.bukkit.entity.Player;

/**
 * Listens for day/night transitions and broadcasts messages.
 */
public class TimeListener extends TimeTransitionListener {

    private final Afflictions plugin;
    private final LocalizationManager lang;

    public TimeListener(Afflictions plugin) {
        super();
        this.plugin = plugin;
        this.lang = plugin.getLocalizationManager();
    }

    @Override
    protected void onNightfall(World world, MoonPhase phase) {
        for (Player player : world.getPlayers()) {
            lang.send(player, "time.nightfall",
                    LocalizationManager.placeholder("moon_phase", getMoonPhaseName(phase))
            );
        }

        plugin.getLogger().info("Night has fallen in " + world.getName() + " - " + phase.getDisplayName());
    }

    @Override
    protected void onDawn(World world) {
        for (Player player : world.getPlayers()) {
            lang.send(player, "time.dawn");
        }

        plugin.getLogger().info("Dawn breaks in " + world.getName());
    }

    private String getMoonPhaseName(MoonPhase phase) {
        String key = "time.moon." + phase.getLocaleKey() + ".name";
        String value = lang.getRaw(key);
        // Fallback to enum display name if not configured
        return value.equals(key) ? phase.getDisplayName() : value;
    }
}
