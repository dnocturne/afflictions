# Afflictions

A Minecraft Paper plugin for supernatural afflictions.

## Features

### Implemented
- **Vampirism** - Players take damage in sunlight, reduced by wearing helmets and higher affliction levels
- **Time & Moon System** - Day/night cycle tracking with 8 moon phases
- **Persistent storage** - SQLite storage with offline player support
- **Localization** - Full MiniMessage support for customizable messages
- **PlaceholderAPI Integration** - Extensive placeholder support for scoreboards, TAB, chat plugins

### Planned
- **Lycanthropy** - Werewolf transformation tied to moon phases
- **Curses** - Stackable debuff afflictions
- **Blood/Thirst System** - Resource management for vampires
- **Vampire Abilities** - Level-locked powers
- **Siring** - Turn other players into vampires

## Requirements

- **Paper 1.21.11** (The use of Paper only configurations and API resulted in the plugin only supporting latest versions. I have no real active interest in trying to support multiple minecraft versions all the time.)
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
- `lang/en.yml` - Localized messages (supports multiple languages)
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
| `%afflictions_<id>_name%` | What the player "is" (empty if not afflicted) | `Vampire` |
| `%afflictions_<id>_affliction%` | The affliction name (empty if not afflicted) | `Vampirism` |
| `%afflictions_<id>_prefix%` | Short prefix/tag (empty if not afflicted) | `[V]` |
| `%afflictions_<id>_title%` | Level title (empty if not afflicted) | `Fledgling`, `Elder` |
| `%afflictions_<id>_permanent%` | Whether affliction is permanent | `true` / `false` |
| `%afflictions_<id>_duration%` | Duration in ms (-1 if permanent) | `-1` |
| `%afflictions_<id>_contracted%` | Timestamp when contracted | `1705312800000` |

### Curse Placeholders (Planned)

| Placeholder | Description | Example Output |
|-------------|-------------|----------------|
| `%afflictions_curse_count%` | Number of active curses | `2` |
| `%afflictions_curse_list%` | Comma-separated list of curse names | `Weakness, Blindness` |

### Custom Data Placeholders

| Placeholder | Description |
|-------------|-------------|
| `%afflictions_data_<id>_<key>%` | Custom data value stored on affliction instance |

### Time Placeholders

These placeholders require the player to be online (for world access). The display text is configurable in the locale files (`lang/*.yml`) under the `time` section.

| Placeholder | Description | Example Output |
|-------------|-------------|----------------|
| `%afflictions_time%` | Time of day with symbol (configurable) | `☀ Day` / `🌕 ꜰᴜʟʟ ᴍᴏᴏɴ` |
| `%afflictions_time_raw%` | Simple day/night text | `day` / `night` |
| `%afflictions_moon_phase%` | Current moon phase name (configurable) | `ꜰᴜʟʟ ᴍᴏᴏɴ`, `ɴᴇᴡ ᴍᴏᴏɴ` |
| `%afflictions_moon_symbol%` | Moon phase symbol (configurable) | `🌕`, `🌑`, `🌗` |

Configure time/moon display in your locale file:
```yaml
time:
  placeholder:
    day: "☀ Day"      # Shown during daytime
    night: "Night"    # Base text for night (symbol added from moon phase)
  moon:
    full-moon:
      name: "Full Moon"
      symbol: "🌕"
    new-moon:
      name: "New Moon"
      symbol: "🌑"
    # ... other phases
```

### Example Usage

Chat format with vampirism prefix:
```
%afflictions_vampirism_prefix%%player_name%
```

Scoreboard showing affliction status:
```
You are: %afflictions_vampirism_name%
Affliction: %afflictions_vampirism_affliction%
Rank: %afflictions_vampirism_title%
Level: %afflictions_level_vampirism%
```

## Building

```bash
./gradlew build
```

The plugin JAR will be in `build/libs/Afflictions-<version>.jar`.

## License

Apache License 2.0
