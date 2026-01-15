package com.dnocturne.afflictions.component.effect;

import com.dnocturne.afflictions.api.affliction.AfflictionInstance;
import com.dnocturne.afflictions.api.component.TickableComponent;
import com.dnocturne.afflictions.util.TimeUtil;
import org.bukkit.Location;
import org.bukkit.entity.Player;

/**
 * Deals damage to the player when exposed to sunlight.
 * Checks for daytime, sky visibility, weather, and helmet protection.
 */
public class SunlightDamageComponent implements TickableComponent {

    private final String id;
    private final double baseDamage;
    private final int tickInterval;
    private final boolean checkWeather;
    private final double helmetDamageReduction;

    public SunlightDamageComponent(String id, double baseDamage, int tickInterval) {
        this(id, baseDamage, tickInterval, true, 0.5);
    }

    public SunlightDamageComponent(String id, double baseDamage, int tickInterval,
                                   boolean checkWeather, double helmetDamageReduction) {
        this.id = id;
        this.baseDamage = baseDamage;
        this.tickInterval = tickInterval;
        this.checkWeather = checkWeather;
        this.helmetDamageReduction = helmetDamageReduction;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public int getTickInterval() {
        return tickInterval;
    }

    @Override
    public void onTick(Player player, AfflictionInstance instance) {
        if (!isExposedToSunlight(player, instance)) {
            instance.setData("burning", false);
            return;
        }

        instance.setData("burning", true);

        // Calculate damage based on level and helmet
        double damage = calculateDamage(player, instance);

        // Apply damage
        player.damage(damage);

        // Visual feedback - set player on fire briefly
        if (player.getFireTicks() < 20) {
            player.setFireTicks(40); // 2 seconds of fire effect
        }
    }

    /**
     * Check if the player is exposed to sunlight.
     */
    private boolean isExposedToSunlight(Player player, AfflictionInstance instance) {
        // Must be daytime
        if (!TimeUtil.isDay(player.getWorld())) {
            return false;
        }

        // Check if sky is visible (light from sky must be max)
        Location loc = player.getLocation();
        if (loc.getBlock().getLightFromSky() < 15) {
            return false;
        }

        // Check weather (rain/storm provides cover)
        if (checkWeather && player.getWorld().hasStorm()) {
            return false;
        }

        return true;
    }

    /**
     * Calculate damage based on affliction level and protection.
     */
    private double calculateDamage(Player player, AfflictionInstance instance) {
        double damage = baseDamage;

        // Scale damage with level
        int level = instance.getLevel();
        // Higher level = less damage (more control over vampirism)
        // Level 1: full damage, Level 5: 60% damage
        double levelReduction = 1.0 - ((level - 1) * 0.1);
        damage *= levelReduction;

        // Helmet provides damage reduction
        if (player.getInventory().getHelmet() != null) {
            damage *= (1.0 - helmetDamageReduction);
            instance.setData("has_helmet", true);
        } else {
            instance.setData("has_helmet", false);
        }

        return Math.max(0.5, damage); // Minimum 0.5 damage
    }

    public double getBaseDamage() {
        return baseDamage;
    }

    public boolean isCheckWeather() {
        return checkWeather;
    }

    public double getHelmetDamageReduction() {
        return helmetDamageReduction;
    }
}
