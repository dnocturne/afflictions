# Commands

Plugin commands for Afflictions.

---

## Command Structure

Base command: `/afflictions`
Aliases: `/aff`, `/afflict`

---

## Player Commands

| Command | Description | Permission |
|---------|-------------|------------|
| `/aff list` | List your active afflictions | `afflictions.list` |
| `/aff info <affliction>` | View affliction details | `afflictions.info` |

---

## Admin Commands

| Command | Description | Permission |
|---------|-------------|------------|
| `/aff give <player> <affliction> [level]` | Give affliction to player | `afflictions.admin.give` |
| `/aff remove <player> <affliction>` | Remove affliction from player | `afflictions.admin.remove` |
| `/aff clear <player>` | Remove all afflictions from player | `afflictions.admin.clear` |
| `/aff reload` | Reload plugin configuration | `afflictions.admin.reload` |

---

## Tab Completion

- Player names (online players)
- Affliction IDs (registered afflictions)
- Player's active afflictions (for info/remove commands)

---

## Implementation

Commands use the **Cloud (Incendo)** framework with **PaperCommandManager** (Paper 1.20.6+ Brigadier API) and a modular subcommand architecture:

```
command/
├── CommandManager.java          # Bootstraps Cloud PaperCommandManager
├── commands/
│   └── AfflictionsCommand.java  # Uses SubCommandRegistry
└── subcommand/
    ├── SubCommand.java          # Interface
    ├── player/
    │   ├── ListCommand.java
    │   └── InfoCommand.java
    └── admin/
        ├── GiveCommand.java
        ├── RemoveCommand.java
        ├── ClearCommand.java
        └── ReloadCommand.java

registry/
└── SubCommandRegistry.java      # Fluent API for command registration
```

### Adding New Commands

1. Create a class implementing `SubCommand`
2. Register it in `AfflictionsCommand` using the registry:

```java
this.registry = SubCommandRegistry.create(plugin, manager)
    .register(ListCommand::new)
    .register(InfoCommand::new)
    .register(MyNewCommand::new)  // Add your command
    .registerIf(someCondition, OptionalCommand::new);  // Conditional
```

---

## See Also
- [[Permissions]]
- [[Configuration]]
