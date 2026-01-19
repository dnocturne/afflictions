package com.dnocturne.afflictions.component.effect;

import com.dnocturne.afflictions.api.affliction.AfflictionInstance;
import com.dnocturne.basalt.component.Tickable;
import org.bukkit.entity.Player;

/**
 * Deals damage to the player over time.
 * Configurable damage amount and interval.
 */
public class DamageEffect implements Tickable<Player, AfflictionInstance> {

    private final String id;
    private final double damage;
    private final int tickInterval;
    private final boolean bypassArmor;

    public DamageEffect(String id, double damage, int tickInterval, boolean bypassArmor) {
        this.id = id;
        this.damage = damage;
        this.tickInterval = tickInterval;
        this.bypassArmor = bypassArmor;
    }

    public DamageEffect(String id, double damage, int tickInterval) {
        this(id, damage, tickInterval, false);
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
        if (bypassArmor) {
            player.setHealth(Math.max(0, player.getHealth() - damage));
        } else {
            player.damage(damage);
        }
    }

    public double getDamage() {
        return damage;
    }

    public boolean isBypassArmor() {
        return bypassArmor;
    }
}
