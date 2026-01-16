package com.dnocturne.afflictions.command.subcommand.player;

import com.dnocturne.afflictions.Afflictions;
import com.dnocturne.afflictions.api.affliction.AfflictionInstance;
import com.dnocturne.afflictions.command.subcommand.SubCommand;
import com.dnocturne.afflictions.locale.LocalizationManager;
import com.dnocturne.afflictions.locale.MessageKey;
import com.dnocturne.afflictions.manager.AfflictionManager;
import com.dnocturne.afflictions.player.AfflictedPlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.incendo.cloud.paper.LegacyPaperCommandManager;

import java.util.Collection;

/**
 * /afflictions list - Shows the player's active afflictions.
 */
public class ListCommand implements SubCommand {

    private final Afflictions plugin;

    public ListCommand(Afflictions plugin) {
        this.plugin = plugin;
    }

    @Override
    public void register(LegacyPaperCommandManager<CommandSender> manager) {
        manager.command(
                manager.commandBuilder("afflictions", "aff", "afflict")
                        .literal("list")
                        .permission("afflictions.list")
                        .senderType(Player.class)
                        .handler(ctx -> {
                            Player player = (Player) ctx.sender();
                            showAfflictions(player);
                        })
        );
    }

    private void showAfflictions(Player player) {
        LocalizationManager lang = plugin.getLocalizationManager();
        AfflictionManager afflictionManager = plugin.getAfflictionManager();

        AfflictedPlayer afflicted = afflictionManager.getPlayerManager()
                .get(player.getUniqueId())
                .orElse(null);

        if (afflicted == null) {
            lang.send(player, MessageKey.AFFLICTION_LIST_NONE);
            return;
        }

        Collection<AfflictionInstance> afflictions = afflicted.getAfflictions();

        if (afflictions.isEmpty()) {
            lang.send(player, MessageKey.AFFLICTION_LIST_NONE);
            return;
        }

        lang.send(player, MessageKey.AFFLICTION_LIST_HEADER);

        for (AfflictionInstance instance : afflictions) {
            lang.send(player, MessageKey.AFFLICTION_LIST_ENTRY,
                    LocalizationManager.placeholder("affliction", instance.getAffliction().getDisplayName()),
                    LocalizationManager.placeholder("level", String.valueOf(instance.getLevel()))
            );
        }
    }
}
