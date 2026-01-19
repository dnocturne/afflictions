# Vampirism

> *"The sun is a cruel reminder of what you once were. But the night... the night is yours."*

---

## Implementation Status

| Feature | Status |
|---------|--------|
| Sun Damage | ✅ Implemented |
| Level Scaling | ✅ Implemented |
| Helmet Reduction | ✅ Implemented |
| Weather Protection | ✅ Implemented |
| PlaceholderAPI | ✅ Implemented |
| Night Vision | ⚪ Planned |
| Blood/Thirst | ⚪ Planned |
| Abilities | ⚪ Planned |
| Siring | ⚪ Planned |
| Cure Mechanic | ⚪ Planned |

---

## Overview

| Property | Value |
|----------|-------|
| **Category** | Supernatural |
| **Type** | Undeath / Blood Curse |
| **Curable** | Yes (difficult) |
| **Contagious** | Yes (bite/blood) |
| **Transformation** | None |

---

## Core Mechanics

Unlike Werewolves, Vampires do not transform. They are always vampires - always hungering, always burning in the light.

### The Sun

The sun is the vampire's eternal enemy. **This cannot be fully negated**, even at max level.

| Level | Sun Effect |
|-------|------------|
| 1 | Rapid burning, heavy damage |
| 2 | Burning, significant damage |
| 3 | Burning, moderate damage |
| 4 | Smoldering, light damage |
| 5 | Discomfort, minor damage + slowness |

> **Design Note:** Even the most powerful vampire feels the sun. Higher levels reduce severity but never eliminate it. This keeps daylight a meaningful threat.

### Mitigation (Not Immunity)
- Helmets reduce sun damage (but don't eliminate)
- Shade/indoors provides safety
- Water does not help (or makes it worse?)
- Potions can reduce but not prevent

---

## Day vs Night

### Daytime ☀️
- Sun damage when exposed to sky
- Weakened abilities
- Slower regeneration
- Survival mode - stay hidden

### Nighttime 🌙
- Full power unlocked
- Enhanced abilities active
- Blood healing amplified
- Hunt mode

> **Note:** Unlike Werewolves, moon phases do not affect Vampires. Every night is their domain.

---

## Progression

Vampires grow stronger through age and feeding.

### Levels

| Level | Title | Unlock |
|-------|-------|--------|
| 1 | Fledgling | Basic vampirism, severe sun weakness |
| 2 | Nightstalker | Reduced sun damage, night vision |
| 3 | Bloodborn | Blood abilities, further sun resistance |
| 4 | Elder | Enhanced abilities, can create thralls |
| 5 | Ancient | Peak power, minimal (but present) sun damage |

### Gaining Power
- Time as vampire
- Feeding on players/mobs
- Surviving days (ironic, but shows endurance)
- Blood rituals (optional mechanic)

---

## Abilities

### Passive (Always Active)
- Night vision (Level 2+)
- No fall damage (or reduced)
- Immune to poison
- Undead - some healing doesn't work

### Night Bonuses (Dusk to Dawn)
- Increased movement speed
- Increased damage
- Enhanced regeneration
- Blood abilities more effective

### Blood Abilities

| Ability | Level | Description |
|---------|-------|-------------|
| Blood Drain | 1 | Heal by attacking |
| Blood Sense | 2 | Detect nearby players (low health) |
| Blood Rush | 3 | Short speed burst, costs blood |
| Crimson Mist | 4 | Brief invisibility (night only) |
| Sire | 5 | Turn willing players into vampires |

---

## Blood & Hunger

### Blood Meter
- Replaces or overlays hunger
- Drains slowly over time
- Drains faster in sunlight
- Replenished by feeding

### Feeding
- Attack players/mobs to drain blood
- Higher level = more efficient feeding
- Some mobs give more blood than others
- Cannot feed on undead

### Starvation
- Low blood = weakened abilities
- Empty blood = constant damage

---

## Contraction

How to become a vampire:
- Bitten by a vampire player (Level 5 Sire ability) - Planned
- Admin command (`/aff give <player> vampirism`)

> **Note:** This plugin does not add custom items. Server owners can create custom contraction methods via the API.

---

## Cure

How to cure vampirism:
- Admin command (`/aff remove <player> vampirism`)
- Planned: Ritual mechanics using vanilla blocks/interactions

> **Note:** Custom cure items can be implemented by server owners using the API with item plugins like ItemsAdder or Oraxen.

---

## Weaknesses

| Weakness | Effect |
|----------|--------|
| **Sunlight** | Damage, cannot be fully negated |
| **Fire** | Increased fire damage (planned) |
| **Running Water** | Slowness when crossing (planned) |

---

## Technical Notes

### Commands
```
/vampire info - View your vampire status
/vampire blood - Check blood level
/vampire feed - (Admin) Set blood level
```

### Permissions
```
afflictions.vampire.abilities - Use vampire abilities
afflictions.vampire.admin - Admin vampire commands
```

---

## Comparison: Vampire vs Werewolf

| Aspect | Vampire | Werewolf |
|--------|---------|----------|
| Transformation | Never | Full moon / voluntary |
| Sun | Always dangerous | No effect |
| Moon | No effect | Empowers / triggers |
| Social | Solitary / Sire system | Pack / Clan system |
| Peak Power | Every night | Full moon |
| Weakness Period | Every day | New moon |

---

## Related
- [[Vampirism Configuration]]
- [[Werewolf]] - Rival supernatural affliction
- [[Cure Mechanics]]
- [[Affliction Types]]
