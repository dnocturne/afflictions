package com.dnocturne.afflictions.util;

import com.dnocturne.afflictions.Afflictions;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitTask;

/**
 * Utilities for scheduling tasks.
 */
public final class TaskUtil {

    private static Afflictions plugin;

    private TaskUtil() {
    }

    public static void init(Afflictions instance) {
        plugin = instance;
    }

    /**
     * Run a task synchronously on the main thread.
     */
    public static BukkitTask runSync(Runnable task) {
        return Bukkit.getScheduler().runTask(plugin, task);
    }

    /**
     * Run a task asynchronously.
     */
    public static BukkitTask runAsync(Runnable task) {
        return Bukkit.getScheduler().runTaskAsynchronously(plugin, task);
    }

    /**
     * Run a task after a delay (in ticks).
     */
    public static BukkitTask runLater(Runnable task, long delayTicks) {
        return Bukkit.getScheduler().runTaskLater(plugin, task, delayTicks);
    }

    /**
     * Run a task asynchronously after a delay (in ticks).
     */
    public static BukkitTask runLaterAsync(Runnable task, long delayTicks) {
        return Bukkit.getScheduler().runTaskLaterAsynchronously(plugin, task, delayTicks);
    }

    /**
     * Run a repeating task (in ticks).
     */
    public static BukkitTask runTimer(Runnable task, long delayTicks, long periodTicks) {
        return Bukkit.getScheduler().runTaskTimer(plugin, task, delayTicks, periodTicks);
    }

    /**
     * Run a repeating async task (in ticks).
     */
    public static BukkitTask runTimerAsync(Runnable task, long delayTicks, long periodTicks) {
        return Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, task, delayTicks, periodTicks);
    }
}
