package com.dnocturne.afflictions.component.effect;

import com.dnocturne.afflictions.Afflictions;
import com.dnocturne.afflictions.api.affliction.AfflictionInstance;
import com.dnocturne.basalt.component.Tickable;
import com.dnocturne.basalt.ui.ActionBarDisplay;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Manages a blood resource system.
 *
 * <p>Blood is stored in the affliction instance data and automatically persisted.
 * This component handles passive blood drain over time and optional action bar display.</p>
 *
 * <p>Data keys used:</p>
 * <ul>
 *   <li>{@code blood} - Current blood level (double)</li>
 * </ul>
 */
public class BloodComponent implements Tickable<Player, AfflictionInstance> {

    public static final String BLOOD_KEY = "blood";

    private final String id;
    private final double maxBlood;
    private final double startBlood;
    private final double passiveDrain;

    // Action bar display (null if disabled)
    private final @Nullable ActionBarDisplay actionBarDisplay;

    /**
     * Create a blood component with configurable settings (no action bar).
     *
     * @param id           The component ID
     * @param maxBlood     Maximum blood capacity
     * @param startBlood   Starting blood when affliction is first applied
     * @param passiveDrain Blood drained per tick
     */
    public BloodComponent(@NotNull String id, double maxBlood, double startBlood, double passiveDrain) {
        this(id, maxBlood, startBlood, passiveDrain, null);
    }

    /**
     * Create a blood component with action bar display.
     *
     * @param id               The component ID
     * @param maxBlood         Maximum blood capacity
     * @param startBlood       Starting blood when affliction is first applied
     * @param passiveDrain     Blood drained per tick
     * @param actionBarDisplay The action bar display configuration (null to disable)
     */
    public BloodComponent(@NotNull String id, double maxBlood, double startBlood, double passiveDrain,
                          @Nullable ActionBarDisplay actionBarDisplay) {
        this.id = id;
        this.maxBlood = maxBlood;
        this.startBlood = startBlood;
        this.passiveDrain = passiveDrain;
        this.actionBarDisplay = actionBarDisplay;
    }

    /**
     * Create a blood component with action bar settings (legacy constructor).
     *
     * <p>This constructor builds a vampirism-specific action bar display for backwards compatibility.
     * For new code, prefer using the constructor that accepts an ActionBarDisplay directly.</p>
     *
     * @param id                      The component ID
     * @param maxBlood                Maximum blood capacity
     * @param startBlood              Starting blood when affliction is first applied
     * @param passiveDrain            Blood drained per tick
     * @param actionBarEnabled        Whether to show blood in action bar
     * @param actionBarUpdateInterval How often to update the action bar (in ticks)
     * @param actionBarOnlyOnChange   Only show action bar when blood changes
     */
    public BloodComponent(@NotNull String id, double maxBlood, double startBlood, double passiveDrain,
                          boolean actionBarEnabled, int actionBarUpdateInterval, boolean actionBarOnlyOnChange) {
        this.id = id;
        this.maxBlood = maxBlood;
        this.startBlood = startBlood;
        this.passiveDrain = passiveDrain;

        if (actionBarEnabled) {
            this.actionBarDisplay = ActionBarDisplay.builder()
                    .localizationSupplier(() -> Afflictions.getInstance().getLocalizationManager())
                    .formatKey("vampirism.action-bar.format")
                    .barConfigKey("vampirism.action-bar.bar")
                    .updateInterval(actionBarUpdateInterval)
                    .onlyOnChange(actionBarOnlyOnChange)
                    .build();
        } else {
            this.actionBarDisplay = null;
        }
    }

    @Override
    public @NotNull String getId() {
        return id;
    }

    @Override
    public void onApply(@NotNull Player player, @NotNull AfflictionInstance instance) {
        // Initialize blood if not already set (preserves existing blood on reload)
        if (!instance.hasData(BLOOD_KEY)) {
            instance.setData(BLOOD_KEY, startBlood);
        }

        // Reset the action bar display state
        if (actionBarDisplay != null) {
            actionBarDisplay.reset();
        }
    }

    @Override
    public void onTick(@NotNull Player player, @NotNull AfflictionInstance instance) {
        if (passiveDrain > 0) {
            drainBlood(instance, passiveDrain);
        }

        // Handle action bar display using the reusable utility
        if (actionBarDisplay != null) {
            double currentBlood = getBlood(instance);
            actionBarDisplay.update(player, currentBlood, maxBlood);
        }
    }

    /**
     * Get the current blood level for an instance.
     *
     * @param instance The affliction instance
     * @return Current blood level, or 0 if not set
     */
    public static double getBlood(@NotNull AfflictionInstance instance) {
        Object blood = instance.getData(BLOOD_KEY);
        if (blood == null) {
            return 0.0;
        }
        if (blood instanceof Number number) {
            return number.doubleValue();
        }
        if (blood instanceof String str) {
            try {
                return Double.parseDouble(str);
            } catch (NumberFormatException e) {
                return 0.0;
            }
        }
        return 0.0;
    }

    /**
     * Set the blood level for an instance.
     *
     * @param instance The affliction instance
     * @param amount   The blood amount to set
     * @param maxBlood The maximum blood capacity
     */
    public static void setBlood(@NotNull AfflictionInstance instance, double amount, double maxBlood) {
        instance.setData(BLOOD_KEY, Math.max(0, Math.min(amount, maxBlood)));
    }

    /**
     * Add blood to an instance (clamped to max).
     *
     * @param instance The affliction instance
     * @param amount   The amount to add
     * @param maxBlood The maximum blood capacity
     * @return The actual amount added
     */
    public static double addBlood(@NotNull AfflictionInstance instance, double amount, double maxBlood) {
        double current = getBlood(instance);
        double newAmount = Math.min(current + amount, maxBlood);
        instance.setData(BLOOD_KEY, newAmount);
        return newAmount - current;
    }

    /**
     * Drain blood from an instance (clamped to 0).
     *
     * @param instance The affliction instance
     * @param amount   The amount to drain
     * @return The actual amount drained
     */
    public static double drainBlood(@NotNull AfflictionInstance instance, double amount) {
        double current = getBlood(instance);
        double newAmount = Math.max(0, current - amount);
        instance.setData(BLOOD_KEY, newAmount);
        return current - newAmount;
    }

    /**
     * Check if an instance has no blood remaining.
     *
     * @param instance The affliction instance
     * @return true if blood is 0 or less
     */
    public static boolean isEmpty(@NotNull AfflictionInstance instance) {
        return getBlood(instance) <= 0;
    }

    /**
     * Get the blood as a percentage of max.
     *
     * @param instance The affliction instance
     * @param maxBlood The maximum blood capacity
     * @return Blood percentage (0.0 to 100.0)
     */
    public static double getBloodPercent(@NotNull AfflictionInstance instance, double maxBlood) {
        if (maxBlood <= 0) return 0;
        return (getBlood(instance) / maxBlood) * 100.0;
    }

    public double getMaxBlood() {
        return maxBlood;
    }

    public double getStartBlood() {
        return startBlood;
    }

    public double getPassiveDrain() {
        return passiveDrain;
    }

    /**
     * Check if action bar display is enabled.
     */
    public boolean isActionBarEnabled() {
        return actionBarDisplay != null;
    }

    /**
     * Get the action bar display, if configured.
     */
    public @Nullable ActionBarDisplay getActionBarDisplay() {
        return actionBarDisplay;
    }
}
