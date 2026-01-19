package com.dnocturne.afflictions.listener;

import com.dnocturne.afflictions.Afflictions;
import com.dnocturne.afflictions.affliction.config.VampirismConfig;
import com.dnocturne.afflictions.affliction.impl.Vampirism;
import com.dnocturne.afflictions.api.affliction.AfflictionInstance;
import com.dnocturne.afflictions.component.effect.BloodComponent;
import com.dnocturne.afflictions.player.AfflictedPlayer;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

import java.util.Optional;

/**
 * Handles blood gain when vampires deal melee damage to living entities.
 */
public class BloodGainListener implements Listener {

    private final Afflictions plugin;

    public BloodGainListener(Afflictions plugin) {
        this.plugin = plugin;
    }

    /**
     * Register this listener with Bukkit.
     */
    public void register() {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        // Check if damager is a player
        if (!(event.getDamager() instanceof Player player)) {
            return;
        }

        // Check if victim is a valid living entity
        if (!(event.getEntity() instanceof LivingEntity victim)) {
            return;
        }

        // Get vampirism config
        VampirismConfig config = plugin.getAfflictionConfig(VampirismConfig.class);
        if (config == null || !config.isBloodEnabled()) {
            return;
        }

        // Check if this entity type can provide blood
        if (!config.canProvideBlood(victim.getType())) {
            return;
        }

        // Check if player is a vampire
        var afflictionManager = plugin.getAfflictionManager();
        if (afflictionManager == null) {
            return;
        }

        Optional<AfflictedPlayer> afflictedOpt = afflictionManager.getPlayerManager()
                .get(player.getUniqueId());

        if (afflictedOpt.isEmpty()) {
            return;
        }

        AfflictedPlayer afflicted = afflictedOpt.get();
        Optional<AfflictionInstance> vampirismOpt = afflicted.getAffliction(Vampirism.ID);

        if (vampirismOpt.isEmpty()) {
            return;
        }

        AfflictionInstance instance = vampirismOpt.get();

        // Calculate blood gain based on damage dealt
        double damage = event.getFinalDamage();
        double bloodGain = damage * config.getBloodGainPerDamage();

        if (bloodGain > 0) {
            BloodComponent.addBlood(instance, bloodGain, config.getMaxBlood());
        }
    }
}
