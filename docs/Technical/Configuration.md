# Configuration

Plugin configuration options.

---

## Config Files

```
plugins/Afflictions/
├── config.yml        # Main configuration
├── afflictions/      # Individual affliction configs
│   └── *.yml
├── messages.yml      # Customizable messages
└── data/            # Player data storage
```

---

## Main Config (config.yml)

```yaml
# General Settings
debug: false
check-for-updates: true

# Affliction Settings
afflictions:
  tick-rate: 20              # Ticks between affliction updates
  max-per-player: 5          # Maximum simultaneous afflictions
  persist-on-death: true     # Keep afflictions after death

# Storage
storage:
  type: yaml                 # yaml, sqlite, mysql
  auto-save-interval: 300    # Seconds between auto-saves

# Integration
hooks:
  placeholderapi: true
```

---

## Messages (messages.yml)

```yaml
prefix: "&8[&cAfflictions&8] "
affliction-gained: "&cYou have contracted {affliction}!"
affliction-cured: "&aYou have been cured of {affliction}!"
affliction-progressed: "&4Your {affliction} has worsened!"
```

---

## See Also
- [[Commands]]
- [[Data Storage]]
