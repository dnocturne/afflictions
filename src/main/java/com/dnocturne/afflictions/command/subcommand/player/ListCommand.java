package com.dnocturne.afflictions.command.subcommand.player;

import com.dnocturne.afflictions.Afflictions;
import com.dnocturne.afflictions.api.affliction.AfflictionInstance;
import com.dnocturne.basalt.command.SubCommand;
import com.dnocturne.basalt.locale.LocalizationManager;
import com.dnocturne.afflictions.locale.MessageKey;
import com.dnocturne.afflictions.manager.AfflictionManager;
import com.dnocturne.afflictions.player.AfflictedPlayer;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import org.bukkit.entity.Player;
import org.incendo.cloud.paper.PaperCommandManager;

import java.util.Collection;
import java.util.Optional;

/**
 * /afflictions list - Shows the player's active afflictions.
 */
public class ListCommand implements SubCommand {

    private final Afflictions plugin;

    public ListCommand(Afflictions plugin) {
        this.plugin = plugin;
    }

    @Override
    public void register(PaperCommandManager<CommandSourceStack> manager) {
        manager.command(
                manager.commandBuilder("afflictions", "aff", "afflict")
                        .literal("list")
                        .permission("afflictions.list")
                        .handler(ctx -> {
                            if (ctx.sender().getSender() instanceof Player player) {
                                showAfflictions(player);
                            }
                        })
        );
    }

    private void showAfflictions(Player player) {
        LocalizationManager lang = plugin.getLocalizationManager();
        AfflictionManager afflictionManager = plugin.getAfflictionManager();

        Optional<AfflictedPlayer> afflictedOpt = afflictionManager.getPlayerManager()
                .get(player.getUniqueId())
                .filter(AfflictedPlayer::hasAnyAffliction);

        if (afflictedOpt.isEmpty()) {
            lang.send(player, MessageKey.AFFLICTION_LIST_NONE);
            return;
        }

        Collection<AfflictionInstance> afflictions = afflictedOpt.get().getAfflictions();

        lang.send(player, MessageKey.AFFLICTION_LIST_HEADER);

        for (AfflictionInstance instance : afflictions) {
            lang.send(player, MessageKey.AFFLICTION_LIST_ENTRY,
                    LocalizationManager.placeholder("affliction", instance.getAffliction().getDisplayName()),
                    LocalizationManager.placeholder("level", String.valueOf(instance.getLevel()))
            );
        }
    }
}
