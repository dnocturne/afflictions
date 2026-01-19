package com.dnocturne.afflictions.component.curse;

import com.dnocturne.afflictions.affliction.config.curse.AttributeConfig;
import com.dnocturne.afflictions.api.affliction.AfflictionInstance;
import com.dnocturne.basalt.component.Component;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

/**
 * Curse attribute modifier component with level-based amount scaling.
 *
 * <p>This component applies attribute modifiers that scale with the curse level.
 * Modifiers are applied on apply and removed on remove, using NamespacedKeys
 * for safe identification and removal.</p>
 */
public class CurseAttributeComponent implements Component<Player, AfflictionInstance> {

    private final String id;
    private final AttributeConfig config;
    private final NamespacedKey modifierKey;

    /**
     * Create a curse attribute component from a config.
     *
     * @param id     Component ID
     * @param config The attribute configuration
     * @param plugin The plugin for creating NamespacedKey
     */
    public CurseAttributeComponent(
            @NotNull String id,
            @NotNull AttributeConfig config,
            @NotNull Plugin plugin) {
        this.id = id;
        this.config = config;
        this.modifierKey = new NamespacedKey(plugin, "curse_" + id + "_" + config.getAttribute().getKey().getKey());
    }

    @Override
    public @NotNull String getId() {
        return id;
    }

    @Override
    public void onApply(@NotNull Player player, @NotNull AfflictionInstance instance) {
        applyModifier(player, instance);
    }

    @Override
    public void onRemove(@NotNull Player player, @NotNull AfflictionInstance instance) {
        removeModifier(player);
    }

    /**
     * Apply the attribute modifier with level-scaled amount.
     */
    private void applyModifier(@NotNull Player player, @NotNull AfflictionInstance instance) {
        AttributeInstance attrInstance = player.getAttribute(config.getAttribute());
        if (attrInstance == null) {
            return;
        }

        // Remove existing modifier first (in case level changed)
        removeModifier(player);

        double amount = config.calculateAmount(instance.getLevel());

        AttributeModifier modifier = new AttributeModifier(
                modifierKey,
                amount,
                config.getOperation()
        );

        attrInstance.addModifier(modifier);
    }

    /**
     * Remove the attribute modifier.
     */
    private void removeModifier(@NotNull Player player) {
        AttributeInstance attrInstance = player.getAttribute(config.getAttribute());
        if (attrInstance == null) {
            return;
        }

        // Remove by key
        AttributeModifier existing = attrInstance.getModifier(modifierKey);
        if (existing != null) {
            attrInstance.removeModifier(existing);
        }
    }

    /**
     * Update the modifier when the curse level changes.
     *
     * @param player   The player
     * @param instance The affliction instance with updated level
     */
    public void updateLevel(@NotNull Player player, @NotNull AfflictionInstance instance) {
        applyModifier(player, instance);
    }

    /**
     * Get the attribute being modified.
     */
    public @NotNull Attribute getAttribute() {
        return config.getAttribute();
    }

    /**
     * Get the configuration.
     */
    public @NotNull AttributeConfig getConfig() {
        return config;
    }

    /**
     * Get the modifier key.
     */
    public @NotNull NamespacedKey getModifierKey() {
        return modifierKey;
    }
}
