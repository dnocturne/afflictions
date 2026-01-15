# Afflictions

A Minecraft Paper plugin for supernatural afflictions - Vampirism, Lycanthropy, Curses, and more.

## Features

- **Vampirism** - Players take damage in sunlight, reduced by wearing helmets and higher affliction levels
- **Component-based architecture** - Easily create new afflictions with reusable effect components
- **Persistent storage** - SQLite storage with offline player support
- **Localization** - Full MiniMessage support for customizable messages
- **API** - Developer-friendly API for creating custom afflictions

## Requirements

- Paper 1.21+
- Java 21+

## Optional Dependencies

- [PlaceholderAPI](https://www.spigotmc.org/resources/placeholderapi.6245/) - Placeholder support

## Commands

| Command | Description | Permission |
|---------|-------------|------------|
| `/afflictions list [player]` | List afflictions | `afflictions.command.list` |
| `/afflictions give <player> <affliction> [level]` | Give an affliction | `afflictions.command.give` |
| `/afflictions remove <player> <affliction>` | Remove an affliction | `afflictions.command.remove` |
| `/afflictions clear <player>` | Clear all afflictions | `afflictions.command.clear` |
| `/afflictions info <affliction>` | View affliction details | `afflictions.command.info` |
| `/afflictions reload` | Reload configuration | `afflictions.command.reload` |

## Configuration

Configuration files are located in `plugins/Afflictions/`:

- `config.yml` - Main configuration
- `messages.yml` - Localized messages

## Building

```bash
./gradlew build
```

The plugin JAR will be in `build/libs/Afflictions-<version>.jar`.

## License

Apache License 2.0
