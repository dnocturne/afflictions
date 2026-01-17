package com.dnocturne.afflictions.listener;

import com.dnocturne.afflictions.Afflictions;
import com.dnocturne.afflictions.locale.LocalizationManager;
import com.dnocturne.afflictions.util.TaskUtil;
import com.dnocturne.afflictions.util.TimeUtil;
import com.dnocturne.afflictions.util.TimeUtil.MoonPhase;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Listens for day/night transitions and broadcasts messages.
 */
public class TimeListener {

    private static final long CHECK_INTERVAL_TICKS = 100L; // 5 seconds

    private final Afflictions plugin;
    private final LocalizationManager lang;
    private final Map<String, Boolean> wasNight = new ConcurrentHashMap<>();
    private BukkitTask task;

    public TimeListener(Afflictions plugin) {
        this.plugin = plugin;
        this.lang = plugin.getLocalizationManager();
    }

    /**
     * Start the time checking task.
     */
    public void start() {
        task = TaskUtil.runTimer(this::checkTimeTransitions, CHECK_INTERVAL_TICKS, CHECK_INTERVAL_TICKS);
    }

    /**
     * Stop the time checking task and clean up resources.
     */
    public void stop() {
        if (task != null) {
            task.cancel();
            task = null;
        }
        wasNight.clear();
    }

    private void checkTimeTransitions() {
        // Collect currently loaded world names to detect unloaded worlds
        Set<String> loadedWorldNames = new HashSet<>();

        for (World world : Bukkit.getWorlds()) {
            if (world.getEnvironment() != World.Environment.NORMAL) {
                continue; // Skip nether/end
            }

            String worldName = world.getName();
            loadedWorldNames.add(worldName);

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

        // Clean up entries for unloaded worlds to prevent memory leaks
        wasNight.keySet().removeIf(worldName -> !loadedWorldNames.contains(worldName));
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
        String key = "time.moon." + phase.getLocaleKey() + ".name";
        String value = lang.getRaw(key);
        // Fallback to enum display name if not configured
        return value.equals(key) ? phase.getDisplayName() : value;
    }
}
