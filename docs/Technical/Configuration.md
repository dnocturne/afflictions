# Configuration

Plugin configuration options.

---

## Config Files

```
plugins/Afflictions/
├── config.yml           # Main configuration
├── afflictions/         # Individual affliction configs
│   └── vampirism.yml
├── lang/                # Localized messages
│   └── en.yml
└── afflictions.db       # SQLite database
```

---

## Main Config (config.yml)

```yaml
# General Settings
debug: false

# Storage
storage:
  type: sqlite           # sqlite (mysql planned)
  auto-save-interval: 300

# Offline mode support
# - auto: Detect server online-mode automatically
# - uuid: Always use UUID lookup
# - name: Always use username lookup
player-lookup: auto
```

---

## Localization (lang/en.yml)

Messages use MiniMessage format for rich text formatting.

```yaml
prefix: "<gray>[<red>Afflictions</red>]</gray> "

affliction:
  contracted: "<red>You have contracted <affliction>!</red>"
  cured: "<green>You have been cured of <affliction>!</green>"

time:
  placeholder:
    day: "☀ Day"
    night: "Night"
  moon:
    full-moon:
      name: "Full Moon"
      symbol: "🌕"
```

---

## Affliction Config (afflictions/vampirism.yml)

Each affliction has its own configuration file. See [[Vampirism Configuration]] for full details.

```yaml
vampirism:
  enabled: true
  max-level: 5

  sun-damage:
    enabled: true
    base-damage: 2.0
    # ... level scaling, helmet reduction, etc.
```

---

## See Also
- [[Commands]]
- [[Data Storage]]
- [[Vampirism Configuration]]
