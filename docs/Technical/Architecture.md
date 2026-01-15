# Architecture

Technical design of the Afflictions plugin.

---

## Package Structure

```
com.dnocturne.afflictions
├── Afflictions.java              # Main plugin class
├── api/
│   ├── affliction/
│   │   ├── Affliction.java       # Base affliction interface
│   │   ├── AbstractAffliction.java
│   │   ├── AfflictionInstance.java
│   │   └── AfflictionCategory.java
│   └── component/
│       ├── AfflictionComponent.java
│       ├── TickableComponent.java
│       ├── ConditionalComponent.java
│       ├── effect/
│       │   └── Effect.java
│       └── trigger/
│           └── Trigger.java
├── affliction/
│   └── impl/
│       └── Vampirism.java            # Vampirism affliction
├── component/
│   ├── effect/
│   │   ├── DamageEffect.java
│   │   ├── PotionEffectComponent.java
│   │   ├── AttributeModifierEffect.java
│   │   └── SunlightDamageComponent.java
│   └── trigger/
│       ├── TimeTrigger.java
│       ├── SunlightTrigger.java
│       └── MoonPhaseTrigger.java
├── command/
│   ├── CommandManager.java
│   ├── commands/
│   │   └── AfflictionsCommand.java
│   └── subcommand/
│       ├── SubCommand.java
│       ├── player/
│       │   ├── ListCommand.java
│       │   └── InfoCommand.java
│       └── admin/
│           ├── GiveCommand.java
│           ├── RemoveCommand.java
│           ├── ClearCommand.java
│           └── ReloadCommand.java
├── config/
│   └── ConfigManager.java
├── hook/
│   └── HookManager.java
├── listener/
│   ├── PlayerListener.java
│   └── TimeListener.java
├── locale/
│   ├── LocalizationManager.java
│   └── MessageKey.java
├── manager/
│   ├── AfflictionManager.java
│   └── PlayerManager.java
├── player/
│   └── AfflictedPlayer.java
├── registry/
│   └── AfflictionRegistry.java
├── storage/
│   ├── Storage.java
│   ├── StorageManager.java
│   ├── data/
│   │   ├── PlayerAfflictionData.java
│   │   └── AfflictionData.java
│   └── impl/
│       └── SQLiteStorage.java
└── util/
    ├── TaskUtil.java
    ├── TimeUtil.java
    └── MessageUtil.java
```

---

## Core Components

### AfflictionManager
Central manager for all affliction logic.
- Register/unregister afflictions via `AfflictionRegistry`
- Apply/remove afflictions from players
- Tick active afflictions (configurable rate)

### PlayerManager
Handles in-memory player data.
- Track active afflictions per player (`AfflictedPlayer`)
- Get/create/remove player data

### StorageManager
Handles data persistence.
- Initializes storage backend (SQLite)
- Async load/save operations
- MySQL/MariaDB planned for future

### CommandManager
Manages Cloud command framework.
- Modular subcommand architecture
- Tab completion support

### LocalizationManager
Multi-language message support.
- MiniMessage formatting
- Placeholder support
- Configurable language files

---

## Component-Based Architecture

Afflictions are built from composable components:

```
Affliction
├── Effects (what happens)
│   ├── DamageEffect
│   ├── PotionEffectComponent
│   ├── AttributeModifierEffect
│   └── SunlightDamageComponent
├── Triggers (when it activates)
│   ├── TimeTrigger
│   ├── SunlightTrigger
│   └── MoonPhaseTrigger
└── Cures (how to remove)
    └── (planned)
```

---

## Built-in Afflictions

### Vampirism
**ID:** `vampirism`

A supernatural curse that burns in sunlight but grants power in darkness.

**Components:**
- `SunlightDamageComponent` - Takes damage when exposed to sunlight
  - Base damage: 2.0 hearts per tick
  - Level scaling: Higher levels reduce damage (more control)
  - Helmet reduces damage by 50%
  - Rain/storm provides full protection
  - Sets player on fire visually

**Data keys:**
- `burning` (boolean) - Currently taking sun damage
- `has_helmet` (boolean) - Wearing helmet protection

---

## Data Flow

```
Event/Command → AfflictionManager → PlayerManager → Storage
                     ↓
              Affliction Components
                     ↓
              TickableComponent.onTick()
                     ↓
              Apply Effects to Player
```

---

## Dependencies

| Dependency | Purpose |
|------------|---------|
| Paper API 1.21.1 | Server API |
| Cloud (Incendo) | Command framework |
| BoostedYAML | Configuration |
| PlaceholderAPI | Placeholder support (soft) |
| Vault | Economy integration (soft) |

---

## See Also
- [[Data Storage]]
- [[API Design]]
- [[Commands]]
