package com.dnocturne.afflictions.listener;

import com.dnocturne.afflictions.Afflictions;
import com.dnocturne.afflictions.locale.LocalizationManager;
import com.dnocturne.afflictions.util.TaskUtil;
import com.dnocturne.afflictions.util.TimeUtil;
import com.dnocturne.afflictions.util.TimeUtil.MoonPhase;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;

/**
 * Listens for day/night transitions and broadcasts messages.
 */
public class TimeListener {

    private final Afflictions plugin;
    private final LocalizationManager lang;
    private final Map<String, Boolean> wasNight = new HashMap<>();

    public TimeListener(Afflictions plugin) {
        this.plugin = plugin;
        this.lang = plugin.getLocalizationManager();
    }

    /**
     * Start the time checking task.
     */
    public void start() {
        // Check every 100 ticks (5 seconds)
        TaskUtil.runTimer(this::checkTimeTransitions, 100L, 100L);
    }

    private void checkTimeTransitions() {
        for (World world : Bukkit.getWorlds()) {
            if (world.getEnvironment() != World.Environment.NORMAL) {
                continue; // Skip nether/end
            }

            String worldName = world.getName();
            boolean isNightNow = TimeUtil.isNight(world);
            Boolean wasNightBefore = wasNight.get(worldName);

            // First check - initialize state
            if (wasNightBefore == null) {
                wasNight.put(worldName, isNightNow);
                continue;
            }

            // Transition to night
            if (isNightNow && !wasNightBefore) {
                onNightfall(world);
            }
            // Transition to day
            else if (!isNightNow && wasNightBefore) {
                onDawn(world);
            }

            wasNight.put(worldName, isNightNow);
        }
    }

    private void onNightfall(World world) {
        MoonPhase phase = TimeUtil.getMoonPhaseEnum(world);

        for (Player player : world.getPlayers()) {
            lang.send(player, "time.nightfall",
                    LocalizationManager.placeholder("moon_phase", getMoonPhaseName(phase))
            );
        }

        plugin.getLogger().info("Night has fallen in " + world.getName() + " - " + phase.getDisplayName());
    }

    private void onDawn(World world) {
        for (Player player : world.getPlayers()) {
            lang.send(player, "time.dawn");
        }

        plugin.getLogger().info("Dawn breaks in " + world.getName());
    }

    private String getMoonPhaseName(MoonPhase phase) {
        return lang.getRaw("time.moon." + phase.name().toLowerCase().replace("_", "-"));
    }
}
