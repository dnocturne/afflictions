# Afflictions

A supernatural afflictions plugin for Paper 1.21+ servers. Transform players into vampires, curse them with debilitating effects, and more.

## Features

- **Vampirism** - Sunlight damage, night bonuses, blood system with feeding mechanics
- **Curses** - Configurable debuff afflictions (weakness, blindness, decay, or create your own)
- **Moon Phases** - 8-phase lunar cycle affecting gameplay
- **PlaceholderAPI** - Full integration for scoreboards, TAB, and chat plugins
- **Persistent Storage** - SQLite with offline player support
- **Fully Configurable** - MiniMessage formatting, per-affliction YAML configs

## Requirements

- Paper 1.21.11 (The plan is to support latest versions of Minecraft as Minecraft is moving away from older java versions and as such, to offer best performance the plugin can have, we will always require latest possible java versions.)
- Java 21+

## Optional

- [PlaceholderAPI](https://www.spigotmc.org/resources/placeholderapi.6245/)

## Quick Start

1. Drop the JAR in your `plugins/` folder
2. Restart the server
3. Use `/afflictions give <player> vampirism` to test

## Commands

| Command | Description |
|---------|-------------|
| `/aff give <player> <affliction> [level]` | Give an affliction |
| `/aff remove <player> <affliction>` | Remove an affliction |
| `/aff clear <player>` | Clear all afflictions |
| `/aff list [player]` | List afflictions |
| `/aff reload` | Reload configuration |

## Documentation

See the [Wiki](../../wiki) for full documentation:
- [Commands & Permissions](../../wiki/Commands)
- [Vampirism](../../wiki/Vampirism)
- [Curses](../../wiki/Curses)
- [Configuration](../../wiki/Configuration)
- [PlaceholderAPI](../../wiki/PlaceholderAPI)

## Building

```bash
./gradlew build
```

## License

Apache License 2.0
