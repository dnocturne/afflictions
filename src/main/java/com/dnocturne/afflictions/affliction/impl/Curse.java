package com.dnocturne.afflictions.affliction.impl;

import com.dnocturne.afflictions.Afflictions;
import com.dnocturne.afflictions.affliction.config.CurseConfig;
import com.dnocturne.afflictions.affliction.config.curse.*;
import com.dnocturne.afflictions.api.affliction.AbstractAffliction;
import com.dnocturne.afflictions.api.affliction.AfflictionCategory;
import com.dnocturne.afflictions.api.affliction.AfflictionInstance;
import com.dnocturne.afflictions.component.curse.*;
import com.dnocturne.basalt.component.Component;
import com.dnocturne.basalt.component.Tickable;
import com.dnocturne.basalt.component.wrapper.ConditionalComponent;
import com.dnocturne.basalt.condition.Condition;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * Data-driven curse affliction.
 *
 * <p>Curses are configurable afflictions loaded from YAML files. They support:</p>
 * <ul>
 *   <li>Potion effects with level scaling</li>
 *   <li>Damage over time with level scaling</li>
 *   <li>Attribute modifiers with level scaling</li>
 *   <li>Particle effects</li>
 *   <li>Sound effects</li>
 *   <li>Conditional triggers (time, weather, location, etc.)</li>
 *   <li>Custom messages on apply/remove</li>
 * </ul>
 *
 * <p>Multiple different curses can stack on the same player, each with their
 * own effects and triggers.</p>
 */
public class Curse extends AbstractAffliction {

    private final CurseConfig config;

    private Curse(Builder builder, CurseConfig config) {
        super(builder);
        this.config = config;
    }

    /**
     * Create a curse from configuration.
     *
     * @param config The curse configuration
     * @return The configured curse
     */
    public static @NotNull Curse create(@NotNull CurseConfig config) {
        return new Builder(config).build();
    }

    /**
     * Get the configuration for this curse.
     */
    public @NotNull CurseConfig getConfig() {
        return config;
    }

    /**
     * Builder for creating curse afflictions from configuration.
     */
    public static class Builder extends AbstractAffliction.Builder {

        private final CurseConfig config;

        public Builder(@NotNull CurseConfig config) {
            super(config.getId());
            this.config = config;

            // Set base properties from config
            this.displayName = stripMiniMessage(config.getAfflictionName());
            this.description = config.getDescription();
            this.category = AfflictionCategory.CURSE;
            this.maxLevel = config.getMaxLevel();
            this.curable = true; // Curses are generally curable
        }

        @Override
        public @NotNull Curse build() {
            validate();

            // Get the trigger condition
            Condition<Player> triggerCondition = config.getTriggerConfig().toCondition();
            boolean alwaysActive = "always".equalsIgnoreCase(config.getTriggerConfig().getType())
                    && !config.getTriggerConfig().isInverted();

            // Build components
            List<Component<Player, AfflictionInstance>> effectComponents = new ArrayList<>();

            // Add potion effect components
            int potionIndex = 0;
            for (PotionEffectConfig potionConfig : config.getPotionEffects()) {
                String componentId = config.getId() + "_potion_" + potionIndex++;
                effectComponents.add(new CursePotionComponent(componentId, potionConfig));
            }

            // Add damage component if enabled
            DamageConfig damageConfig = config.getDamageConfig();
            if (damageConfig.isEnabled()) {
                effectComponents.add(new CurseDamageComponent(config.getId() + "_damage", damageConfig));
            }

            // Add attribute components
            Afflictions plugin = Afflictions.getInstance();
            int attrIndex = 0;
            for (AttributeConfig attrConfig : config.getAttributeConfigs()) {
                String componentId = config.getId() + "_attr_" + attrIndex++;
                if (plugin != null) {
                    effectComponents.add(new CurseAttributeComponent(componentId, attrConfig, plugin));
                }
            }

            // Add particle component if enabled
            ParticleConfig particleConfig = config.getParticleConfig();
            if (particleConfig.isEnabled()) {
                effectComponents.add(new CurseParticleComponent(config.getId() + "_particles", particleConfig));
            }

            // Add sound component if enabled
            SoundConfig soundConfig = config.getSoundConfig();
            if (soundConfig.isEnabled()) {
                effectComponents.add(new CurseSoundComponent(config.getId() + "_sound", soundConfig));
            }

            // Wrap components in conditional wrapper if not always active
            if (alwaysActive) {
                // Components are always active, add them directly
                this.components.addAll(effectComponents);
            } else {
                // Wrap each component in a conditional wrapper
                for (Component<Player, AfflictionInstance> effect : effectComponents) {
                    String wrapperId = effect.getId() + "_conditional";
                    String stateKey = effect.getId() + "_active";

                    int tickInterval = 1;
                    if (effect instanceof Tickable<?, ?> tickable) {
                        tickInterval = tickable.getTickInterval();
                    }

                    ConditionalComponent<Player, AfflictionInstance> conditional = ConditionalComponent
                            .<Player, AfflictionInstance>builder()
                            .id(wrapperId)
                            .wrap(effect)
                            .condition(triggerCondition)
                            .stateKey(stateKey)
                            .dataAccessor(AfflictionInstance::getDataMap)
                            .tickInterval(tickInterval)
                            .build();

                    this.components.add(conditional);
                }
            }

            return new Curse(this, config);
        }

        /**
         * Strip MiniMessage formatting for internal display name.
         */
        private static String stripMiniMessage(String text) {
            if (text == null) return "";
            // Simple strip of common MiniMessage tags for internal use
            return text.replaceAll("<[^>]+>", "").trim();
        }
    }
}
