package com.dnocturne.afflictions.util;

import com.dnocturne.afflictions.Afflictions;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;

/**
 * Utilities for scheduling tasks.
 */
public final class TaskUtil {

    private static Afflictions plugin;

    private TaskUtil() {
    }

    public static void init(@NotNull Afflictions instance) {
        plugin = instance;
    }

    /**
     * Run a task synchronously on the main thread.
     */
    public static @NotNull BukkitTask runSync(@NotNull Runnable task) {
        return Bukkit.getScheduler().runTask(plugin, task);
    }

    /**
     * Run a task asynchronously.
     */
    public static @NotNull BukkitTask runAsync(@NotNull Runnable task) {
        return Bukkit.getScheduler().runTaskAsynchronously(plugin, task);
    }

    /**
     * Run a task after a delay (in ticks).
     */
    public static @NotNull BukkitTask runLater(@NotNull Runnable task, long delayTicks) {
        return Bukkit.getScheduler().runTaskLater(plugin, task, delayTicks);
    }

    /**
     * Run a task asynchronously after a delay (in ticks).
     */
    public static @NotNull BukkitTask runLaterAsync(@NotNull Runnable task, long delayTicks) {
        return Bukkit.getScheduler().runTaskLaterAsynchronously(plugin, task, delayTicks);
    }

    /**
     * Run a repeating task (in ticks).
     */
    public static @NotNull BukkitTask runTimer(@NotNull Runnable task, long delayTicks, long periodTicks) {
        return Bukkit.getScheduler().runTaskTimer(plugin, task, delayTicks, periodTicks);
    }

    /**
     * Run a repeating async task (in ticks).
     */
    public static @NotNull BukkitTask runTimerAsync(@NotNull Runnable task, long delayTicks, long periodTicks) {
        return Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, task, delayTicks, periodTicks);
    }
}
