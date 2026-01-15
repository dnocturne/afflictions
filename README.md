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
- `afflictions/vampirism.yml` - Vampirism configuration

## PlaceholderAPI

Requires [PlaceholderAPI](https://www.spigotmc.org/resources/placeholderapi.6245/) to be installed.

### General Placeholders

| Placeholder | Description | Example Output |
|-------------|-------------|----------------|
| `%afflictions_count%` | Total number of active afflictions | `1` |
| `%afflictions_list%` | Comma-separated list of affliction IDs | `vampirism, curse_weakness` |
| `%afflictions_has_any%` | Whether player has any affliction | `true` / `false` |

### Affliction-Specific Placeholders

Replace `<id>` with the affliction ID (e.g., `vampirism`).

| Placeholder | Description | Example Output |
|-------------|-------------|----------------|
| `%afflictions_has_<id>%` | Whether player has this affliction | `true` / `false` |
| `%afflictions_level_<id>%` | Level of affliction (0 if not afflicted) | `3` |
| `%afflictions_<id>_prefix%` | Configured prefix (empty if not afflicted) | `[Vampire]` |
| `%afflictions_<id>_name%` | Configured formatted name (empty if not afflicted) | `Vampire` |
| `%afflictions_<id>_permanent%` | Whether affliction is permanent | `true` / `false` |
| `%afflictions_<id>_duration%` | Duration in ms (-1 if permanent) | `-1` |
| `%afflictions_<id>_contracted%` | Timestamp when contracted | `1705312800000` |

### Curse Placeholders

| Placeholder | Description | Example Output |
|-------------|-------------|----------------|
| `%afflictions_curse_count%` | Number of active curses | `2` |
| `%afflictions_curse_list%` | Comma-separated list of curse names | `Weakness, Blindness` |

### Custom Data Placeholders

| Placeholder | Description |
|-------------|-------------|
| `%afflictions_data_<id>_<key>%` | Custom data value stored on affliction instance |

### Example Usage

Chat format with vampirism prefix:
```
%afflictions_vampirism_prefix%%player_name%
```

Scoreboard showing affliction status:
```
Affliction: %afflictions_vampirism_name%
Level: %afflictions_level_vampirism%
```

## Building

```bash
./gradlew build
```

The plugin JAR will be in `build/libs/Afflictions-<version>.jar`.

## License

Apache License 2.0
