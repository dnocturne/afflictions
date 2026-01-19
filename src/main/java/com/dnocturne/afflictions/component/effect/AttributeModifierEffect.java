package com.dnocturne.afflictions.component.effect;

import com.dnocturne.afflictions.api.affliction.AfflictionInstance;
import com.dnocturne.basalt.component.Component;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.entity.Player;

/**
 * Modifies player attributes (speed, damage, health, etc.).
 * Applied when affliction starts, removed when it ends.
 */
public class AttributeModifierEffect implements Component<Player, AfflictionInstance> {

    private final String id;
    private final Attribute attribute;
    private final double amount;
    private final AttributeModifier.Operation operation;
    private final NamespacedKey modifierKey;

    public AttributeModifierEffect(String id, Attribute attribute, double amount,
                                    AttributeModifier.Operation operation, NamespacedKey modifierKey) {
        this.id = id;
        this.attribute = attribute;
        this.amount = amount;
        this.operation = operation;
        this.modifierKey = modifierKey;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public void onApply(Player player, AfflictionInstance instance) {
        AttributeInstance attrInstance = player.getAttribute(attribute);
        if (attrInstance == null) return;

        // Remove existing modifier if present
        attrInstance.removeModifier(modifierKey);

        // Add the new modifier
        AttributeModifier modifier = new AttributeModifier(modifierKey, amount, operation);
        attrInstance.addModifier(modifier);
    }

    @Override
    public void onRemove(Player player, AfflictionInstance instance) {
        AttributeInstance attrInstance = player.getAttribute(attribute);
        if (attrInstance == null) return;

        attrInstance.removeModifier(modifierKey);
    }

    public Attribute getAttribute() {
        return attribute;
    }

    public double getAmount() {
        return amount;
    }

    public AttributeModifier.Operation getOperation() {
        return operation;
    }
}
