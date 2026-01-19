package com.dnocturne.afflictions.affliction.config.curse;

import dev.dejvokep.boostedyaml.block.implementation.Section;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Configuration for a curse attribute modifier.
 *
 * <p>Example YAML:</p>
 * <pre>
 * effects:
 *   attributes:
 *     - attribute: GENERIC_MAX_HEALTH
 *       base-amount: -2.0
 *       level-scaling: -1.0
 *       operation: ADD_NUMBER
 * </pre>
 */
public class AttributeConfig {

    private final Attribute attribute;
    private final double baseAmount;
    private final double levelScaling;
    private final AttributeModifier.Operation operation;

    /**
     * Create an attribute config.
     */
    public AttributeConfig(
            @NotNull Attribute attribute,
            double baseAmount,
            double levelScaling,
            @NotNull AttributeModifier.Operation operation) {
        this.attribute = attribute;
        this.baseAmount = baseAmount;
        this.levelScaling = levelScaling;
        this.operation = operation;
    }

    /**
     * Load attribute configs from a YAML list section.
     *
     * @param section The YAML section containing the "attributes" list
     * @return List of loaded attribute configs
     */
    public static @NotNull List<AttributeConfig> fromSection(@Nullable Section section) {
        List<AttributeConfig> configs = new ArrayList<>();
        if (section == null) {
            return configs;
        }

        List<?> attributeList = section.getList("attributes");
        if (attributeList == null) {
            return configs;
        }

        for (Object item : attributeList) {
            AttributeConfig config = null;
            if (item instanceof Section attrSection) {
                config = fromAttributeSection(attrSection);
            } else if (item instanceof java.util.Map<?, ?> map) {
                config = fromMap(map);
            }
            if (config != null) {
                configs.add(config);
            }
        }

        return configs;
    }

    /**
     * Load a single attribute config from a YAML section.
     */
    private static @Nullable AttributeConfig fromAttributeSection(@NotNull Section section) {
        String attrName = section.getString("attribute");
        if (attrName == null) {
            return null;
        }

        Attribute attribute = parseAttribute(attrName);
        if (attribute == null) {
            return null;
        }

        double baseAmount = section.getDouble("base-amount", 0.0);
        double levelScaling = section.getDouble("level-scaling", 0.0);

        String opName = section.getString("operation", "ADD_NUMBER");
        AttributeModifier.Operation operation = parseOperation(opName);

        return new AttributeConfig(attribute, baseAmount, levelScaling, operation);
    }

    /**
     * Load an attribute config from a Map (for list items).
     */
    private static @Nullable AttributeConfig fromMap(@NotNull java.util.Map<?, ?> map) {
        Object attrObj = map.get("attribute");
        if (attrObj == null) {
            return null;
        }

        Attribute attribute = parseAttribute(attrObj.toString());
        if (attribute == null) {
            return null;
        }

        double baseAmount = getDouble(map, "base-amount", 0.0);
        double levelScaling = getDouble(map, "level-scaling", 0.0);

        Object opObj = map.get("operation");
        String opName = opObj != null ? opObj.toString() : "ADD_NUMBER";
        AttributeModifier.Operation operation = parseOperation(opName);

        return new AttributeConfig(attribute, baseAmount, levelScaling, operation);
    }

    private static double getDouble(java.util.Map<?, ?> map, String key, double defaultValue) {
        Object value = map.get(key);
        if (value instanceof Number num) {
            return num.doubleValue();
        }
        return defaultValue;
    }

    /**
     * Parse an attribute from a string name.
     * Supports both formats: "GENERIC_MAX_HEALTH" and "generic_max_health"
     */
    private static @Nullable Attribute parseAttribute(@NotNull String name) {
        // Convert to lowercase and replace underscores for NamespacedKey format
        String keyName = name.toLowerCase(Locale.ROOT);
        NamespacedKey key = NamespacedKey.minecraft(keyName);
        return Registry.ATTRIBUTE.get(key);
    }

    /**
     * Parse an operation from a string name.
     */
    private static @NotNull AttributeModifier.Operation parseOperation(@NotNull String name) {
        try {
            return AttributeModifier.Operation.valueOf(name.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            return AttributeModifier.Operation.ADD_NUMBER;
        }
    }

    /**
     * Get the attribute.
     */
    public @NotNull Attribute getAttribute() {
        return attribute;
    }

    /**
     * Get the base amount (at level 1).
     */
    public double getBaseAmount() {
        return baseAmount;
    }

    /**
     * Get the level scaling factor.
     */
    public double getLevelScaling() {
        return levelScaling;
    }

    /**
     * Get the operation.
     */
    public @NotNull AttributeModifier.Operation getOperation() {
        return operation;
    }

    /**
     * Calculate the amount for a specific level.
     *
     * @param level The curse level
     * @return The calculated amount
     */
    public double calculateAmount(int level) {
        return baseAmount + ((level - 1) * levelScaling);
    }
}
