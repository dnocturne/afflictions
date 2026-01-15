# Commands

Plugin commands for Afflictions.

---

## Command Structure

Base command: `/afflictions` or `/aff`

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
| `/aff give <player> <affliction>` | Give affliction to player | `afflictions.admin.give` |
| `/aff remove <player> <affliction>` | Remove affliction | `afflictions.admin.remove` |
| `/aff clear <player>` | Remove all afflictions | `afflictions.admin.clear` |
| `/aff reload` | Reload configuration | `afflictions.admin.reload` |

---

## Tab Completion

- Player names
- Affliction names
- Online players with afflictions

---

## See Also
- [[Permissions]]
- [[Configuration]]
