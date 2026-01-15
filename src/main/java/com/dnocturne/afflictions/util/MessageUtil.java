package com.dnocturne.afflictions.util;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Map;

/**
 * Utilities for sending messages using Adventure/MiniMessage.
 */
public final class MessageUtil {

    private static final MiniMessage MINI_MESSAGE = MiniMessage.miniMessage();

    private MessageUtil() {
    }

    /**
     * Parse a MiniMessage string into a Component.
     */
    public static Component parse(String message) {
        return MINI_MESSAGE.deserialize(message);
    }

    /**
     * Parse a MiniMessage string with placeholders.
     */
    public static Component parse(String message, TagResolver... resolvers) {
        return MINI_MESSAGE.deserialize(message, resolvers);
    }

    /**
     * Parse a MiniMessage string with simple key-value placeholders.
     */
    public static Component parse(String message, Map<String, String> placeholders) {
        TagResolver.Builder builder = TagResolver.builder();
        placeholders.forEach((key, value) ->
                builder.resolver(Placeholder.parsed(key, value))
        );
        return MINI_MESSAGE.deserialize(message, builder.build());
    }

    /**
     * Send a MiniMessage to a player.
     */
    public static void send(CommandSender sender, String message) {
        sender.sendMessage(parse(message));
    }

    /**
     * Send a MiniMessage with placeholders to a player.
     */
    public static void send(CommandSender sender, String message, TagResolver... resolvers) {
        sender.sendMessage(parse(message, resolvers));
    }

    /**
     * Send a MiniMessage with key-value placeholders.
     */
    public static void send(CommandSender sender, String message, Map<String, String> placeholders) {
        sender.sendMessage(parse(message, placeholders));
    }

    /**
     * Send an action bar message to a player.
     */
    public static void sendActionBar(Player player, String message) {
        player.sendActionBar(parse(message));
    }

    /**
     * Send an action bar message with placeholders.
     */
    public static void sendActionBar(Player player, String message, TagResolver... resolvers) {
        player.sendActionBar(parse(message, resolvers));
    }

    /**
     * Create a simple placeholder resolver.
     */
    public static TagResolver placeholder(String key, String value) {
        return Placeholder.parsed(key, value);
    }

    /**
     * Create a component placeholder resolver.
     */
    public static TagResolver placeholder(String key, Component value) {
        return Placeholder.component(key, value);
    }
}
