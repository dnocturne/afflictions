# Data Storage

How player and affliction data is persisted.

---

## Storage Backends

### YAML (Default)
- Simple file-based storage
- One file per player
- Good for small servers

### SQLite
- Single database file
- Better performance for medium servers
- No external dependencies

### MySQL
- External database server
- Best for large servers / networks
- Supports cross-server sync

---

## Player Data Schema

```yaml
# players/<uuid>.yml
uuid: "..."
name: "PlayerName"
afflictions:
  - type: "poison"
    severity: 2
    contracted: 1704067200000
    duration: 72000
    data:
      custom-key: value
immunities:
  - type: "plague"
    level: 2
    expires: 1704153600000
```

---

## Database Schema (SQL)

```sql
CREATE TABLE afflicted_players (
    uuid VARCHAR(36) PRIMARY KEY,
    name VARCHAR(16),
    data JSON,
    updated_at TIMESTAMP
);

CREATE TABLE player_afflictions (
    id INT AUTO_INCREMENT PRIMARY KEY,
    player_uuid VARCHAR(36),
    affliction_type VARCHAR(64),
    severity INT,
    contracted_at BIGINT,
    duration BIGINT,
    extra_data JSON,
    FOREIGN KEY (player_uuid) REFERENCES afflicted_players(uuid)
);
```

---

## See Also
- [[Architecture]]
- [[Configuration]]
