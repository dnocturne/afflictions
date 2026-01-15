# Architecture

Technical design of the Afflictions plugin.

---

## Package Structure

```
com.dnocturne.afflictions
├── Afflictions.java          # Main plugin class
├── affliction/
│   ├── Affliction.java       # Base affliction interface/class
│   ├── AfflictionType.java   # Affliction registry
│   ├── AfflictionManager.java# Handles active afflictions
│   └── impl/                 # Affliction implementations
├── player/
│   ├── AfflictedPlayer.java  # Player affliction data
│   └── PlayerManager.java    # Player data handling
├── command/
│   └── ...                   # Command implementations
├── config/
│   └── ...                   # Configuration classes
├── listener/
│   └── ...                   # Event listeners
└── util/
    └── ...                   # Utilities
```

---

## Core Components

### AfflictionManager
Central manager for all affliction logic.
- Register/unregister afflictions
- Apply/remove afflictions from players
- Tick active afflictions

### PlayerManager
Handles player-specific data.
- Load/save player affliction data
- Track active afflictions per player
- Handle join/quit events

---

## Data Flow

```
Event/Command → AfflictionManager → PlayerManager → Storage
                     ↓
              Affliction.tick()
                     ↓
              Apply Effects
```

---

## See Also
- [[Data Storage]]
- [[API Design]]
