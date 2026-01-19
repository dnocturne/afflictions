# Cure Mechanics

How players can remove or treat afflictions.

> **Note:** This plugin does not add custom items. Cures are implemented through admin commands, vanilla mechanics, or integration with other plugins.

---

## Cure Types

### Admin Commands
- `/aff remove <player> <affliction>` - Remove specific affliction
- `/aff clear <player>` - Remove all afflictions

### Actions (Planned)
- Visiting specific locations
- Time-based recovery
- Ritual sequences (vanilla block interactions)

### External Integration
- Other plugins can use the API to cure afflictions
- Server owners can create custom cure systems

---

## Cure Properties

| Property | Description |
|----------|-------------|
| **Success Rate** | Chance the cure works (configurable) |
| **Requirements** | Prerequisites to use |
| **Cooldown** | Time before reuse |

---

## Design Philosophy

The plugin focuses on affliction mechanics, not item creation. Server owners who want cure items should:

1. Use existing item plugins (e.g., ItemsAdder, Oraxen, MMOItems)
2. Connect them via the Afflictions API
3. Create custom scripts/plugins for their specific needs

This keeps the plugin lightweight and avoids duplicating functionality that dedicated item plugins do better.

---

## See Also
- [[Afflictions Overview]]
- [[Progression System]]
- [[API Design]]
