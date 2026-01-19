# Permissions

Permission nodes for Afflictions.

---

## Permission Tree

```
afflictions.*
├── afflictions.list
├── afflictions.info
└── afflictions.admin.*
    ├── afflictions.admin.give
    ├── afflictions.admin.remove
    ├── afflictions.admin.clear
    └── afflictions.admin.reload
```

---

## Permission Details

### Player Permissions

| Permission | Default | Description |
|------------|---------|-------------|
| `afflictions.list` | true | View own afflictions |
| `afflictions.info` | true | View affliction information |

### Admin Permissions

| Permission | Default | Description |
|------------|---------|-------------|
| `afflictions.admin.*` | op | All admin permissions |
| `afflictions.admin.give` | op | Apply afflictions to players |
| `afflictions.admin.remove` | op | Remove afflictions from players |
| `afflictions.admin.clear` | op | Clear all afflictions |
| `afflictions.admin.reload` | op | Reload plugin configuration |

---

## Bypass Permissions

| Permission | Description |
|------------|-------------|
| `afflictions.bypass.*` | Bypass all affliction effects |
| `afflictions.bypass.<affliction>` | Bypass specific affliction |

---

## See Also
- [[Commands]]
- [[Configuration]]
