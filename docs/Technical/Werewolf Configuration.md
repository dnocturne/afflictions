# Werewolf Configuration

Configuration options for the Werewolf system.

---

## File Location

```
plugins/Afflictions/
├── config.yml           # Main config
└── werewolf/
    ├── werewolf.yml     # General werewolf settings
    └── clans.yml        # Clan definitions (fully customizable)
```

---

## General Settings (werewolf.yml)

```yaml
werewolf:
  # Transformation
  transformation:
    duration: 600                    # Seconds in wolf form (voluntary)
    cooldown: 1800                   # Seconds between voluntary transforms
    full-moon-duration: -1           # -1 = entire night
    allow-voluntary-transform: true
    min-level-for-voluntary: 2       # Level required for voluntary transform

  # Combat
  combat:
    damage-multiplier: 1.25          # Damage boost while transformed
    speed-multiplier: 1.15           # Speed boost while transformed

  # Weaknesses
  weaknesses:
    wolfsbane-enabled: true
    silver-damage-multiplier: 1.5    # Extra damage from silver weapons
    new-moon-weakness: true          # Weakened during new moon

  # Spreading
  infection:
    bite-chance: 0.25                # Chance to infect on attack
    require-transformed: true        # Must be in wolf form to infect

  # Progression
  progression:
    xp-per-kill: 10
    xp-per-full-moon: 50
    levels:
      2: 100    # XP needed for level 2
      3: 300
      4: 600
      5: 1000
```

---

## Clan Configuration (clans.yml)

> **Server owners can fully customize clan names, lore, and bonuses.**

```yaml
# ==============================================
# WEREWOLF CLANS CONFIGURATION
# ==============================================
# Customize clan names and bonuses for your server.
# All display names, descriptions, and bonuses can be changed.
#
# To add a new clan: copy an existing block and modify.
# To remove a clan: delete its block or set enabled: false
# ==============================================

clans:
  # Internal ID (don't change after players join)
  fenrir:
    enabled: true

    # Display name - shown to players (CUSTOMIZE THIS)
    display-name: "House of Fenrir"

    # Short tag for chat/display
    tag: "Fenrir"
    tag-color: "&4"           # Dark red

    # Lore/description shown in menus
    description: |
      The Destroyer. Children of the great wolf
      who will devour the sun. They embody
      primal strength and savage fury.

    # Bonus type: DAMAGE, SPEED, NIGHT_VISION, HEALTH, RESISTANCE
    bonus:
      type: DAMAGE
      value: 1.05              # 5% damage increase

    # Optional settings
    max-members: 20            # -1 for unlimited
    icon: BONE                 # Item for GUI displays

  skoll:
    enabled: true
    display-name: "House of Skoll"
    tag: "Skoll"
    tag-color: "&6"           # Gold
    description: |
      The Pursuer. Named for the wolf who
      endlessly chases the sun. Swift,
      relentless hunters who never tire.
    bonus:
      type: SPEED
      value: 1.05              # 5% speed increase
    max-members: 20
    icon: FEATHER

  hati:
    enabled: true
    display-name: "House of Hati"
    tag: "Hati"
    tag-color: "&7"           # Gray
    description: |
      The Shadow. Named for the wolf who
      hunts the moon. Masters of darkness
      who see all in the night.
    bonus:
      type: NIGHT_VISION
      value: 1                 # Boolean abilities use 1 for enabled
    max-members: 20
    icon: ENDER_EYE

# ==============================================
# EXAMPLE: Adding a custom clan
# ==============================================
#
#   myserver_pack:
#     enabled: true
#     display-name: "The Crimson Pack"
#     tag: "Crimson"
#     tag-color: "&c"
#     description: |
#       Your custom lore here.
#       Multi-line supported.
#     bonus:
#       type: HEALTH
#       value: 1.10            # 10% more health
#     max-members: 15
#     icon: REDSTONE
#
# ==============================================
```

---

## Hierarchy Settings

```yaml
hierarchy:
  omega:
    display-name: "Omega"
    description: "Lone wolf, no clan"
    can-be-recruited: true

  beta:
    display-name: "Beta"
    description: "Clan member"

  alpha:
    display-name: "Alpha"
    description: "Clan leader"
    bonus-multiplier: 1.5      # Alphas get 1.5x clan bonus
    max-betas: 19              # Max members under one alpha

  # How alphas are determined
  alpha-succession:
    on-alpha-death: STRONGEST  # STRONGEST, OLDEST, VOTE, DISSOLVE
    challenge-enabled: true    # Allow challenging alpha for position
    challenge-cooldown: 86400  # Seconds between challenges
```

---

## Messages

```yaml
messages:
  werewolf:
    transformed: "&7You feel the beast take over..."
    reverted: "&7The beast retreats... for now."
    full-moon-warning: "&c&lThe full moon rises tonight..."
    infected: "&4You feel a burning in your veins..."

  clan:
    joined: "&aYou have joined {clan}!"
    left: "&7You have left {clan}."
    promoted-alpha: "&6You are now the Alpha of {clan}!"
    recruited: "&a{player} has joined {clan}!"
```

---

## Related
- [[Werewolf]]
- [[Configuration]]
- [[Commands]]
