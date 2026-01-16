package com.dnocturne.afflictions.locale;

import com.dnocturne.afflictions.Afflictions;
import dev.dejvokep.boostedyaml.YamlDocument;
import dev.dejvokep.boostedyaml.settings.dumper.DumperSettings;
import dev.dejvokep.boostedyaml.settings.general.GeneralSettings;
import dev.dejvokep.boostedyaml.settings.loader.LoaderSettings;
import dev.dejvokep.boostedyaml.settings.updater.UpdaterSettings;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Manages localization/translations for multiple languages.
 */
public class LocalizationManager {

    private static final MiniMessage MINI_MESSAGE = MiniMessage.miniMessage();

    private final Afflictions plugin;
    private final Logger logger;
    private final File langFolder;
    private final Map<String, YamlDocument> loadedLanguages = new HashMap<>();

    private String defaultLanguage = "en";
    private @Nullable YamlDocument currentLanguage;
    private @Nullable String currentLanguageCode;

    public LocalizationManager(Afflictions plugin) {
        this.plugin = plugin;
        this.logger = plugin.getLogger();
        this.langFolder = new File(plugin.getDataFolder(), "lang");
    }

    /**
     * Initialize the localization system.
     */
    public void load() {
        // Create lang folder if it doesn't exist
        if (!langFolder.exists()) {
            langFolder.mkdirs();
        }

        // Extract default language files from resources
        extractDefaultLanguages();

        // Load the configured language
        String configuredLang = plugin.getConfigManager().getMainConfig()
                .getString("general.language", defaultLanguage);

        setLanguage(configuredLang);
    }

    /**
     * Extract default language files from plugin resources.
     */
    private void extractDefaultLanguages() {
        String[] defaultLangs = {"en", "es", "de", "fr", "pt", "zh", "ja", "ko", "ru"};

        for (String lang : defaultLangs) {
            String resourcePath = "lang/" + lang + ".yml";
            InputStream resource = plugin.getResource(resourcePath);

            if (resource != null) {
                File langFile = new File(langFolder, lang + ".yml");
                if (!langFile.exists()) {
                    try {
                        YamlDocument.create(langFile, resource,
                                GeneralSettings.DEFAULT,
                                LoaderSettings.DEFAULT,
                                DumperSettings.DEFAULT,
                                UpdaterSettings.DEFAULT
                        );
                        logger.info("Extracted language file: " + lang + ".yml");
                    } catch (IOException e) {
                        logger.log(Level.WARNING, "Failed to extract language file: " + lang, e);
                    }
                }
            }
        }
    }

    /**
     * Set the active language.
     *
     * @param languageCode The language code (e.g., "en", "es", "de")
     * @return true if the language was loaded successfully
     */
    public boolean setLanguage(String languageCode) {
        try {
            YamlDocument lang = loadLanguage(languageCode);
            if (lang != null) {
                currentLanguage = lang;
                currentLanguageCode = languageCode;
                logger.info("Language set to: " + languageCode);
                return true;
            }
        } catch (IOException e) {
            logger.log(Level.WARNING, "Failed to load language: " + languageCode, e);
        }

        // Fallback to default language
        if (!languageCode.equals(defaultLanguage)) {
            logger.warning("Falling back to default language: " + defaultLanguage);
            return setLanguage(defaultLanguage);
        }

        return false;
    }

    /**
     * Load a language file.
     */
    private @Nullable YamlDocument loadLanguage(@NotNull String languageCode) throws IOException {
        if (loadedLanguages.containsKey(languageCode)) {
            return loadedLanguages.get(languageCode);
        }

        File langFile = new File(langFolder, languageCode + ".yml");
        InputStream defaultResource = plugin.getResource("lang/" + languageCode + ".yml");

        if (!langFile.exists() && defaultResource == null) {
            logger.warning("Language file not found: " + languageCode + ".yml");
            return null;
        }

        YamlDocument doc;
        if (defaultResource != null) {
            doc = YamlDocument.create(langFile, defaultResource,
                    GeneralSettings.DEFAULT,
                    LoaderSettings.builder().setAutoUpdate(true).build(),
                    DumperSettings.DEFAULT,
                    UpdaterSettings.DEFAULT
            );
        } else {
            doc = YamlDocument.create(langFile,
                    GeneralSettings.DEFAULT,
                    LoaderSettings.DEFAULT,
                    DumperSettings.DEFAULT,
                    UpdaterSettings.DEFAULT
            );
        }

        loadedLanguages.put(languageCode, doc);
        return doc;
    }

    /**
     * Reload all loaded language files.
     */
    public void reload() {
        loadedLanguages.clear();
        load();
    }

    /**
     * Get a raw message string by key.
     *
     * @param key The message key
     * @return The raw message string, or the key if not found
     */
    public @NotNull String getRaw(@NotNull String key) {
        if (currentLanguage == null) {
            return key;
        }
        return currentLanguage.getString(key, key);
    }

    /**
     * Get a message as a Component.
     *
     * @param key The message key
     * @return The parsed Component
     */
    public @NotNull Component get(@NotNull String key) {
        return MINI_MESSAGE.deserialize(getRaw(key));
    }

    /**
     * Get a message with placeholders.
     *
     * @param key       The message key
     * @param resolvers Placeholder resolvers
     * @return The parsed Component with placeholders replaced
     */
    public @NotNull Component get(@NotNull String key, @NotNull TagResolver... resolvers) {
        return MINI_MESSAGE.deserialize(getRaw(key), resolvers);
    }

    /**
     * Get a message with simple key-value placeholders.
     *
     * @param key          The message key
     * @param placeholders Map of placeholder name to value
     * @return The parsed Component with placeholders replaced
     */
    public @NotNull Component get(@NotNull String key, @NotNull Map<String, String> placeholders) {
        TagResolver.Builder builder = TagResolver.builder();
        placeholders.forEach((k, v) -> builder.resolver(Placeholder.parsed(k, v)));
        return MINI_MESSAGE.deserialize(getRaw(key), builder.build());
    }

    /**
     * Get a prefixed message.
     *
     * @param key The message key
     * @return The message with the plugin prefix prepended
     */
    public @NotNull Component getPrefixed(@NotNull String key) {
        Component prefix = get("prefix");
        Component message = get(key);
        return prefix.append(message);
    }

    /**
     * Get a prefixed message with placeholders.
     *
     * @param key       The message key
     * @param resolvers Placeholder resolvers
     * @return The message with prefix and placeholders
     */
    public @NotNull Component getPrefixed(@NotNull String key, @NotNull TagResolver... resolvers) {
        Component prefix = get("prefix");
        Component message = get(key, resolvers);
        return prefix.append(message);
    }

    /**
     * Send a message to a command sender.
     *
     * @param sender The recipient
     * @param key    The message key
     */
    public void send(@NotNull CommandSender sender, @NotNull String key) {
        sender.sendMessage(getPrefixed(key));
    }

    /**
     * Send a message with placeholders.
     *
     * @param sender    The recipient
     * @param key       The message key
     * @param resolvers Placeholder resolvers
     */
    public void send(@NotNull CommandSender sender, @NotNull String key, @NotNull TagResolver... resolvers) {
        sender.sendMessage(getPrefixed(key, resolvers));
    }

    /**
     * Send a message without prefix.
     *
     * @param sender The recipient
     * @param key    The message key
     */
    public void sendRaw(@NotNull CommandSender sender, @NotNull String key) {
        sender.sendMessage(get(key));
    }

    /**
     * Send an action bar message.
     *
     * @param player The player
     * @param key    The message key
     */
    public void sendActionBar(@NotNull Player player, @NotNull String key) {
        player.sendActionBar(get(key));
    }

    /**
     * Send an action bar message with placeholders.
     *
     * @param player    The player
     * @param key       The message key
     * @param resolvers Placeholder resolvers
     */
    public void sendActionBar(@NotNull Player player, @NotNull String key, @NotNull TagResolver... resolvers) {
        player.sendActionBar(get(key, resolvers));
    }

    /**
     * Create a placeholder resolver.
     */
    public static @NotNull TagResolver placeholder(@NotNull String key, @NotNull String value) {
        return Placeholder.parsed(key, value);
    }

    /**
     * Create a component placeholder resolver.
     */
    public static @NotNull TagResolver placeholder(@NotNull String key, @NotNull Component value) {
        return Placeholder.component(key, value);
    }

    /**
     * Get the current language code.
     */
    public @Nullable String getCurrentLanguageCode() {
        return currentLanguageCode;
    }

    /**
     * Get available language codes.
     */
    public @NotNull String[] getAvailableLanguages() {
        File[] files = langFolder.listFiles((dir, name) -> name.endsWith(".yml"));
        if (files == null) return new String[0];

        String[] langs = new String[files.length];
        for (int i = 0; i < files.length; i++) {
            langs[i] = files[i].getName().replace(".yml", "");
        }
        return langs;
    }
}
