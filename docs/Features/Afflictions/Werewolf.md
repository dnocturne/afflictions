# Werewolf

> *"When the moon rises full, the beast within awakens. Some fight it. Others embrace the hunt."*

---

## Overview

| Property | Value |
|----------|-------|
| **Category** | Supernatural |
| **Type** | Lycanthropy |
| **Curable** | Yes (difficult) |
| **Contagious** | Yes (bite) |

---

## Hierarchy

Werewolves follow a strict social hierarchy with three tiers:

```
     ┌─────────┐
     │  Alpha  │  ← Clan leader (1 per clan)
     └────┬────┘
          │
     ┌────┴────┐
     │  Beta   │  ← Clan members
     └────┬────┘
          │
     ┌────┴────┐
     │  Omega  │  ← Lone wolves (no clan)
     └─────────┘
```

### Omega (Lone Wolf)
- Default state when first turned
- No clan affiliation
- Can be recruited by Alphas
- Can challenge to form a new clan

### Beta (Clan Member)
- Belongs to a clan under an Alpha
- Receives clan bonuses
- Can rise to Alpha if current Alpha dies/is defeated

### Alpha (Clan Leader)
- Leads a clan of Betas
- Can recruit Omegas
- Enhanced abilities
- Only one Alpha per clan

---

## Clans

> **Note for Server Owners:** All clan names, descriptions, and bonuses are fully configurable in `clans.yml`. The defaults below are thematic suggestions.

### Default Clans

| Clan | Theme | Bonus |
|------|-------|-------|
| **House of Fenrir** | The Destroyer - primal strength | +5% melee damage |
| **House of Skoll** | The Pursuer - relentless hunters | +5% movement speed |
| **House of Hati** | The Shadow - creatures of the night | Enhanced night vision |

### Clan Configuration

See [[Werewolf Configuration]] for full customization options including:
- Custom clan names and lore
- Adjustable bonuses per clan
- Maximum clan sizes
- Custom clan icons/colors

---

## Transformation

### Triggers
- **Forced:** Full moon (automatic)
- **Voluntary:** Skill unlock at higher levels
- **Rage:** Low health trigger (configurable)

### Transformed State
- Changed appearance (optional resource pack)
- Enhanced abilities active
- Hunger drains faster
- Cannot use certain items

### Moon Phases

| Phase | Effect |
|-------|--------|
| New Moon | Weakened, no voluntary transform |
| Waxing | Normal |
| Full Moon | Forced transformation, peak power |
| Waning | Normal |

---

## Abilities

### Passive (Always Active)
- Enhanced night vision
- Increased hunger from raw meat
- Wolves are neutral/friendly

### Transformed (Wolf Form)
- Increased damage
- Increased speed
- Enhanced jump
- Regeneration boost
- Clan-specific bonus active

---

## Progression

### Gaining Power
- Time as werewolf
- Kills while transformed
- Surviving full moons
- Clan activities

### Levels

| Level | Name | Unlock |
|-------|------|--------|
| 1 | Pup | Basic transformation |
| 2 | Hunter | Voluntary transformation |
| 3 | Packmaster | Reduced transformation cooldown |
| 4 | Elder | Extended transformation duration |
| 5 | Primal | Full moon immunity (optional transform) |

---

## Contraction

How to become a werewolf:
- Bitten by a transformed werewolf player (planned)
- Admin command (`/aff give <player> lycanthropy`)

> **Note:** This plugin does not add custom items. Server owners can create custom contraction methods via the API.

---

## Cure

How to remove lycanthropy:
- Admin command (`/aff remove <player> lycanthropy`)
- Planned: Ritual mechanics during new moon

> **Note:** Custom cure items can be implemented by server owners using the API with item plugins like ItemsAdder or Oraxen.

---

## Weaknesses

- **New Moon:** Significantly weakened, no voluntary transformation

---

## Technical Notes

### Commands
```
/werewolf info - View your werewolf status
/werewolf transform - Voluntary transformation (if unlocked)
/werewolf clan - View clan info
/werewolf clan invite <player> - (Alpha) Invite omega to clan
/werewolf clan leave - Leave current clan
```

### Permissions
```
afflictions.werewolf.transform - Allow voluntary transform
afflictions.werewolf.clan.create - Allow creating new clans
afflictions.werewolf.admin - Admin werewolf commands
```

---

## Related
- [[Werewolf Configuration]]
- [[Vampirism]] - Rival supernatural affliction
- [[Cure Mechanics]]
- [[Affliction Types]]
