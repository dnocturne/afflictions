# Data Storage

How player and affliction data is persisted.

---

## Storage Backends

### SQLite (Default)
- Single database file (`plugins/Afflictions/afflictions.db`)
- No external dependencies
- Good for single servers
- Async operations to prevent lag

### MySQL/MariaDB (Planned)
- External database server
- Best for large servers / networks
- Supports cross-server sync
- **Status:** Not yet implemented

---

## Configuration

```yaml
storage:
  # Storage type: sqlite, mysql (mariadb)
  type: sqlite

  # Auto-save interval in seconds (0 to disable)
  auto-save-interval: 300

  # Offline mode support
  # - auto: Detect server online-mode setting automatically (recommended)
  # - uuid: Always use UUID-only lookup (for online-mode servers)
  # - name: Always use username-based lookup (for offline-mode servers)
  player-lookup: auto

  # MySQL/MariaDB settings (only used if type is mysql)
  mysql:
    host: localhost
    port: 3306
    database: afflictions
    username: root
    password: ""
    pool:
      maximum-pool-size: 10
      minimum-idle: 2
      connection-timeout: 30000
```

---

## Offline Mode Support

The plugin supports both online and offline mode servers:

### Online Mode (Default)
- Players are identified by their Mojang UUID
- UUID remains consistent across name changes

### Offline Mode
- UUIDs are generated from usernames (`UUID.nameUUIDFromBytes("OfflinePlayer:" + username)`)
- If a player changes their username, they get a new UUID
- The plugin stores usernames alongside UUIDs to handle this
- On join, players are looked up by username first, then UUID

### player-lookup Modes
| Mode | Description |
|------|-------------|
| `auto` | Automatically detect server's online-mode setting (recommended) |
| `uuid` | Always use UUID-only lookup (for online-mode servers) |
| `name` | Always use username-based lookup (for offline-mode with username registration) |

---

## Database Schema

### SQLite Schema

```sql
CREATE TABLE afflicted_players (
    uuid TEXT PRIMARY KEY,
    username TEXT NOT NULL,
    last_seen INTEGER NOT NULL
);

CREATE TABLE player_afflictions (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    player_uuid TEXT NOT NULL,
    affliction_id TEXT NOT NULL,
    level INTEGER NOT NULL DEFAULT 1,
    duration INTEGER NOT NULL DEFAULT -1,
    contracted_at INTEGER NOT NULL,
    data TEXT,  -- JSON for custom affliction data
    FOREIGN KEY (player_uuid) REFERENCES afflicted_players(uuid) ON DELETE CASCADE,
    UNIQUE(player_uuid, affliction_id)
);

CREATE INDEX idx_player_afflictions_uuid ON player_afflictions(player_uuid);
CREATE INDEX idx_afflicted_players_username ON afflicted_players(username COLLATE NOCASE);
```

---

## Data Flow

```
Player Join  → Storage.loadPlayer()  → AfflictedPlayer populated
Player Quit  → Storage.savePlayer()  → Data persisted
             → PlayerManager.remove() → Memory cleaned
```

All database operations are async (`CompletableFuture`) to prevent blocking the main thread.

---

## Implementation Classes

| Class | Description |
|-------|-------------|
| `Storage` | Interface for storage abstraction |
| `StorageManager` | Initializes and provides storage access |
| `SQLiteStorage` | SQLite implementation |
| `PlayerAfflictionData` | DTO for player data |
| `AfflictionData` | DTO for single affliction |
| `PlayerListener` | Handles join/quit for load/save |

---

## See Also
- [[Architecture]]
- [[Configuration]]
