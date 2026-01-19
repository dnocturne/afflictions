package com.dnocturne.afflictions.affliction.config.curse;

import dev.dejvokep.boostedyaml.block.implementation.Section;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Configuration for a curse potion effect.
 *
 * <p>Example YAML:</p>
 * <pre>
 * effects:
 *   potions:
 *     - type: WEAKNESS
 *       base-amplifier: 0
 *       level-scaling: 0.5
 *       ambient: true
 *       particles: false
 *       icon: true
 * </pre>
 */
public class PotionEffectConfig {

    private final PotionEffectType type;
    private final int baseAmplifier;
    private final double levelScaling;
    private final boolean ambient;
    private final boolean particles;
    private final boolean icon;

    /**
     * Create a potion effect config.
     */
    public PotionEffectConfig(
            @NotNull PotionEffectType type,
            int baseAmplifier,
            double levelScaling,
            boolean ambient,
            boolean particles,
            boolean icon) {
        this.type = type;
        this.baseAmplifier = baseAmplifier;
        this.levelScaling = levelScaling;
        this.ambient = ambient;
        this.particles = particles;
        this.icon = icon;
    }

    /**
     * Load potion effect configs from a YAML list section.
     *
     * @param section The YAML section containing the "potions" list
     * @return List of loaded potion effect configs
     */
    public static @NotNull List<PotionEffectConfig> fromSection(@Nullable Section section) {
        List<PotionEffectConfig> configs = new ArrayList<>();
        if (section == null) {
            return configs;
        }

        List<?> potionList = section.getList("potions");
        if (potionList == null) {
            return configs;
        }

        for (Object item : potionList) {
            PotionEffectConfig config = null;
            if (item instanceof Section potionSection) {
                config = fromPotionSection(potionSection);
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
     * Load a single potion effect config from a YAML section.
     */
    private static @Nullable PotionEffectConfig fromPotionSection(@NotNull Section section) {
        String typeName = section.getString("type");
        if (typeName == null) {
            return null;
        }

        PotionEffectType type = parsePotionEffectType(typeName);
        if (type == null) {
            return null;
        }

        int baseAmplifier = section.getInt("base-amplifier", 0);
        double levelScaling = section.getDouble("level-scaling", 0.0);
        boolean ambient = section.getBoolean("ambient", true);
        boolean particles = section.getBoolean("particles", false);
        boolean icon = section.getBoolean("icon", true);

        return new PotionEffectConfig(type, baseAmplifier, levelScaling, ambient, particles, icon);
    }

    /**
     * Load a potion effect config from a Map (for list items).
     */
    private static @Nullable PotionEffectConfig fromMap(@NotNull java.util.Map<?, ?> map) {
        Object typeObj = map.get("type");
        if (typeObj == null) {
            return null;
        }

        PotionEffectType type = parsePotionEffectType(typeObj.toString());
        if (type == null) {
            return null;
        }

        int baseAmplifier = getInt(map, "base-amplifier", 0);
        double levelScaling = getDouble(map, "level-scaling", 0.0);
        boolean ambient = getBoolean(map, "ambient", true);
        boolean particles = getBoolean(map, "particles", false);
        boolean icon = getBoolean(map, "icon", true);

        return new PotionEffectConfig(type, baseAmplifier, levelScaling, ambient, particles, icon);
    }

    private static int getInt(java.util.Map<?, ?> map, String key, int defaultValue) {
        Object value = map.get(key);
        if (value instanceof Number num) {
            return num.intValue();
        }
        return defaultValue;
    }

    private static double getDouble(java.util.Map<?, ?> map, String key, double defaultValue) {
        Object value = map.get(key);
        if (value instanceof Number num) {
            return num.doubleValue();
        }
        return defaultValue;
    }

    private static boolean getBoolean(java.util.Map<?, ?> map, String key, boolean defaultValue) {
        Object value = map.get(key);
        if (value instanceof Boolean bool) {
            return bool;
        }
        return defaultValue;
    }

    /**
     * Parse a potion effect type from a string name.
     * Supports formats like "WEAKNESS" or "weakness".
     */
    private static @Nullable PotionEffectType parsePotionEffectType(@NotNull String name) {
        String keyName = name.toLowerCase(Locale.ROOT);
        NamespacedKey key = NamespacedKey.minecraft(keyName);
        return Registry.EFFECT.get(key);
    }

    /**
     * Get the potion effect type.
     */
    public @NotNull PotionEffectType getType() {
        return type;
    }

    /**
     * Get the base amplifier (at level 1).
     */
    public int getBaseAmplifier() {
        return baseAmplifier;
    }

    /**
     * Get the level scaling factor.
     */
    public double getLevelScaling() {
        return levelScaling;
    }

    /**
     * Check if the effect should be ambient.
     */
    public boolean isAmbient() {
        return ambient;
    }

    /**
     * Check if particles should be shown.
     */
    public boolean hasParticles() {
        return particles;
    }

    /**
     * Check if the icon should be shown.
     */
    public boolean hasIcon() {
        return icon;
    }

    /**
     * Calculate the amplifier for a specific level.
     *
     * @param level The curse level
     * @return The calculated amplifier (clamped to 0-255)
     */
    public int calculateAmplifier(int level) {
        double amplifier = baseAmplifier + ((level - 1) * levelScaling);
        return Math.max(0, Math.min(255, (int) amplifier));
    }
}
