package com.dnocturne.afflictions.component.trigger;

import com.dnocturne.afflictions.api.affliction.AfflictionInstance;
import com.dnocturne.afflictions.api.component.trigger.Trigger;
import com.dnocturne.afflictions.util.TimeUtil;
import org.bukkit.Location;
import org.bukkit.entity.Player;

/**
 * Trigger when player is exposed to sunlight.
 * Checks for daytime, sky access, and optionally weather.
 */
public class SunlightTrigger implements Trigger {

    private final String id;
    private final boolean checkWeather;
    private final boolean checkHelmet;

    public SunlightTrigger(String id) {
        this(id, true, true);
    }

    public SunlightTrigger(String id, boolean checkWeather, boolean checkHelmet) {
        this.id = id;
        this.checkWeather = checkWeather;
        this.checkHelmet = checkHelmet;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public boolean isActive(Player player, AfflictionInstance instance) {
        // Must be daytime
        if (!TimeUtil.isDay(player.getWorld())) {
            return false;
        }

        // Check if sky is visible
        Location loc = player.getLocation();
        if (loc.getBlock().getLightFromSky() < 15) {
            return false;
        }

        // Optionally check weather (rain/storm provides cover)
        if (checkWeather && player.getWorld().hasStorm()) {
            return false;
        }

        // Optionally check if wearing helmet (provides some protection)
        if (checkHelmet && player.getInventory().getHelmet() != null) {
            // Still exposed, but caller can check this for damage reduction
            instance.setData("has_helmet", true);
        } else {
            instance.setData("has_helmet", false);
        }

        return true;
    }

    public boolean isCheckWeather() {
        return checkWeather;
    }

    public boolean isCheckHelmet() {
        return checkHelmet;
    }
}
