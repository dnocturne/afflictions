package com.dnocturne.afflictions.command.subcommand.admin;

import com.dnocturne.afflictions.Afflictions;
import com.dnocturne.afflictions.affliction.config.VampirismConfig;
import com.dnocturne.afflictions.affliction.impl.Vampirism;
import com.dnocturne.afflictions.api.affliction.AfflictionInstance;
import com.dnocturne.afflictions.component.effect.BloodComponent;
import com.dnocturne.afflictions.locale.MessageKey;
import com.dnocturne.basalt.command.SubCommand;
import com.dnocturne.basalt.locale.LocalizationManager;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.incendo.cloud.paper.PaperCommandManager;

import java.util.Optional;

import static org.incendo.cloud.bukkit.parser.PlayerParser.playerParser;
import static org.incendo.cloud.parser.standard.DoubleParser.doubleParser;

/**
 * /afflictions blood <set|add|remove> <player> <amount> - Manage vampire blood levels.
 *
 * <p>Only available when the blood system is enabled in vampirism config.</p>
 */
public class BloodCommand implements SubCommand {

    private final Afflictions plugin;

    public BloodCommand(Afflictions plugin) {
        this.plugin = plugin;
    }

    @Override
    public void register(PaperCommandManager<CommandSourceStack> manager) {
        // /afflictions blood set <player> <amount>
        manager.command(
                manager.commandBuilder("afflictions", "aff", "afflict")
                        .literal("blood")
                        .literal("set")
                        .required("player", playerParser())
                        .required("amount", doubleParser(0))
                        .permission("afflictions.admin.blood")
                        .handler(ctx -> {
                            CommandSender sender = ctx.sender().getSender();
                            Player target = ctx.get("player");
                            double amount = ctx.get("amount");
                            setBlood(sender, target, amount);
                        })
        );

        // /afflictions blood add <player> <amount>
        manager.command(
                manager.commandBuilder("afflictions", "aff", "afflict")
                        .literal("blood")
                        .literal("add")
                        .required("player", playerParser())
                        .required("amount", doubleParser(0))
                        .permission("afflictions.admin.blood")
                        .handler(ctx -> {
                            CommandSender sender = ctx.sender().getSender();
                            Player target = ctx.get("player");
                            double amount = ctx.get("amount");
                            addBlood(sender, target, amount);
                        })
        );

        // /afflictions blood remove <player> <amount>
        manager.command(
                manager.commandBuilder("afflictions", "aff", "afflict")
                        .literal("blood")
                        .literal("remove")
                        .required("player", playerParser())
                        .required("amount", doubleParser(0))
                        .permission("afflictions.admin.blood")
                        .handler(ctx -> {
                            CommandSender sender = ctx.sender().getSender();
                            Player target = ctx.get("player");
                            double amount = ctx.get("amount");
                            removeBlood(sender, target, amount);
                        })
        );

        // /afflictions blood get <player> - View blood level
        manager.command(
                manager.commandBuilder("afflictions", "aff", "afflict")
                        .literal("blood")
                        .literal("get")
                        .required("player", playerParser())
                        .permission("afflictions.admin.blood")
                        .handler(ctx -> {
                            CommandSender sender = ctx.sender().getSender();
                            Player target = ctx.get("player");
                            getBlood(sender, target);
                        })
        );
    }

    private void setBlood(CommandSender sender, Player target, double amount) {
        LocalizationManager lang = plugin.getLocalizationManager();

        if (!isBloodSystemEnabled(sender, lang)) {
            return;
        }

        Optional<AfflictionInstance> instanceOpt = getVampireInstance(target);
        if (instanceOpt.isEmpty()) {
            lang.send(sender, MessageKey.BLOOD_NOT_VAMPIRE,
                    LocalizationManager.placeholder("player", target.getName()));
            return;
        }

        VampirismConfig config = plugin.getAfflictionConfig(VampirismConfig.class);
        double maxBlood = config != null ? config.getMaxBlood() : 100;
        double clampedAmount = Math.max(0, Math.min(amount, maxBlood));

        BloodComponent.setBlood(instanceOpt.get(), clampedAmount, maxBlood);

        lang.send(sender, MessageKey.BLOOD_SET_SUCCESS,
                LocalizationManager.placeholder("player", target.getName()),
                LocalizationManager.placeholder("amount", String.format("%.1f", clampedAmount)),
                LocalizationManager.placeholder("max", String.format("%.0f", maxBlood)));
    }

    private void addBlood(CommandSender sender, Player target, double amount) {
        LocalizationManager lang = plugin.getLocalizationManager();

        if (!isBloodSystemEnabled(sender, lang)) {
            return;
        }

        Optional<AfflictionInstance> instanceOpt = getVampireInstance(target);
        if (instanceOpt.isEmpty()) {
            lang.send(sender, MessageKey.BLOOD_NOT_VAMPIRE,
                    LocalizationManager.placeholder("player", target.getName()));
            return;
        }

        VampirismConfig config = plugin.getAfflictionConfig(VampirismConfig.class);
        double maxBlood = config != null ? config.getMaxBlood() : 100;

        double added = BloodComponent.addBlood(instanceOpt.get(), amount, maxBlood);
        double newAmount = BloodComponent.getBlood(instanceOpt.get());

        lang.send(sender, MessageKey.BLOOD_ADD_SUCCESS,
                LocalizationManager.placeholder("player", target.getName()),
                LocalizationManager.placeholder("amount", String.format("%.1f", added)),
                LocalizationManager.placeholder("new_amount", String.format("%.1f", newAmount)),
                LocalizationManager.placeholder("max", String.format("%.0f", maxBlood)));
    }

    private void removeBlood(CommandSender sender, Player target, double amount) {
        LocalizationManager lang = plugin.getLocalizationManager();

        if (!isBloodSystemEnabled(sender, lang)) {
            return;
        }

        Optional<AfflictionInstance> instanceOpt = getVampireInstance(target);
        if (instanceOpt.isEmpty()) {
            lang.send(sender, MessageKey.BLOOD_NOT_VAMPIRE,
                    LocalizationManager.placeholder("player", target.getName()));
            return;
        }

        VampirismConfig config = plugin.getAfflictionConfig(VampirismConfig.class);
        double maxBlood = config != null ? config.getMaxBlood() : 100;

        double removed = BloodComponent.drainBlood(instanceOpt.get(), amount);
        double newAmount = BloodComponent.getBlood(instanceOpt.get());

        lang.send(sender, MessageKey.BLOOD_REMOVE_SUCCESS,
                LocalizationManager.placeholder("player", target.getName()),
                LocalizationManager.placeholder("amount", String.format("%.1f", removed)),
                LocalizationManager.placeholder("new_amount", String.format("%.1f", newAmount)),
                LocalizationManager.placeholder("max", String.format("%.0f", maxBlood)));
    }

    private void getBlood(CommandSender sender, Player target) {
        LocalizationManager lang = plugin.getLocalizationManager();

        if (!isBloodSystemEnabled(sender, lang)) {
            return;
        }

        Optional<AfflictionInstance> instanceOpt = getVampireInstance(target);
        if (instanceOpt.isEmpty()) {
            lang.send(sender, MessageKey.BLOOD_NOT_VAMPIRE,
                    LocalizationManager.placeholder("player", target.getName()));
            return;
        }

        VampirismConfig config = plugin.getAfflictionConfig(VampirismConfig.class);
        double maxBlood = config != null ? config.getMaxBlood() : 100;
        double currentBlood = BloodComponent.getBlood(instanceOpt.get());
        double percent = (maxBlood > 0) ? (currentBlood / maxBlood) * 100 : 0;

        lang.send(sender, MessageKey.BLOOD_GET_SUCCESS,
                LocalizationManager.placeholder("player", target.getName()),
                LocalizationManager.placeholder("amount", String.format("%.1f", currentBlood)),
                LocalizationManager.placeholder("max", String.format("%.0f", maxBlood)),
                LocalizationManager.placeholder("percent", String.format("%.1f", percent)));
    }

    /**
     * Check if the blood system is enabled.
     */
    private boolean isBloodSystemEnabled(CommandSender sender, LocalizationManager lang) {
        VampirismConfig config = plugin.getAfflictionConfig(VampirismConfig.class);
        if (config == null || !config.isEnabled() || !config.isBloodEnabled()) {
            lang.send(sender, MessageKey.BLOOD_SYSTEM_DISABLED);
            return false;
        }
        return true;
    }

    /**
     * Get the vampirism affliction instance for a player.
     */
    private Optional<AfflictionInstance> getVampireInstance(Player player) {
        return plugin.getAfflictionManager().getPlayerManager().get(player.getUniqueId())
                .flatMap(data -> data.getAffliction(Vampirism.ID));
    }
}
