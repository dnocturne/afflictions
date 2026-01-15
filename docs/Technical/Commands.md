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

Commands use the **Cloud (Incendo)** framework with a modular subcommand architecture:

```
command/
├── CommandManager.java          # Bootstraps Cloud
├── commands/
│   └── AfflictionsCommand.java  # Registers subcommands
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
```

Adding new commands: Create a class implementing `SubCommand` and register it in `AfflictionsCommand`.

---

## See Also
- [[Permissions]]
- [[Configuration]]
