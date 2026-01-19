# Vampirism Configuration

Configuration options for the Vampirism system.

---

## File Location

```
plugins/Afflictions/
├── config.yml           # Main config
└── vampire/
    └── vampire.yml      # Vampire settings
```

---

## Vampire Settings (vampire.yml)

```yaml
# ==============================================
# VAMPIRISM CONFIGURATION
# ==============================================

vampire:

  # ------------------------------------------
  # SUN DAMAGE
  # ------------------------------------------
  # The sun ALWAYS affects vampires. Higher levels reduce damage
  # but can NEVER fully negate it. This is intentional.
  # ------------------------------------------
  sun:
    enabled: true

    # Damage per second at each level (cannot be set to 0)
    # Even level 5 must have SOME damage
    damage-per-level:
      1: 4.0      # Rapid burning - very dangerous
      2: 3.0      # Burning - significant
      3: 2.0      # Burning - moderate
      4: 1.0      # Smoldering - light
      5: 0.5      # Discomfort - minor but persistent

    # Additional effects while in sun
    effects:
      slowness: true
      weakness: true
      no-regeneration: true

    # Helmet protection (reduces damage, not eliminates)
    helmet-reduction:
      leather: 0.1    # 10% reduction
      chainmail: 0.15
      iron: 0.2
      gold: 0.25
      diamond: 0.3
      netherite: 0.35

    # Maximum total reduction from all sources (never 100%)
    max-reduction: 0.75   # Can never reduce more than 75%

  # ------------------------------------------
  # NIGHT BONUSES
  # ------------------------------------------
  night:
    # Time range (in ticks, 0 = dawn, 12000 = dusk, 13000-23000 = night)
    start-time: 13000
    end-time: 23000

    # Bonuses active at night
    bonuses:
      damage-multiplier: 1.2      # 20% more damage
      speed-multiplier: 1.15      # 15% faster
      regeneration: true          # Passive regen at night
      regeneration-rate: 40       # Ticks between regen (lower = faster)

  # ------------------------------------------
  # BLOOD SYSTEM
  # ------------------------------------------
  blood:
    max-blood: 20                 # Like hunger bar
    drain-rate: 1200              # Ticks between passive drain
    drain-rate-in-sun: 200        # Much faster in sunlight

    # Blood gained from feeding (attack damage)
    feeding:
      enabled: true
      blood-per-hit: 2            # Base blood gained
      efficiency-per-level:       # Multiplier per vampire level
        1: 1.0
        2: 1.2
        3: 1.4
        4: 1.6
        5: 2.0

    # Blood from different sources
    mob-blood-values:
      default: 1
      VILLAGER: 3
      PLAYER: 5
      PIG: 2
      COW: 2
      SHEEP: 1
      # Undead give no blood
      ZOMBIE: 0
      SKELETON: 0
      PHANTOM: 0

    # Starvation effects
    starvation:
      threshold: 4                # Below this = starving
      damage-when-empty: 1.0      # Damage per second at 0 blood
      weakness-when-low: true

  # ------------------------------------------
  # ABILITIES
  # ------------------------------------------
  abilities:
    blood-drain:
      enabled: true
      unlock-level: 1

    blood-sense:
      enabled: true
      unlock-level: 2
      range: 30                   # Blocks
      health-threshold: 0.5       # Detect players below 50% health

    blood-rush:
      enabled: true
      unlock-level: 3
      speed-boost: 2              # Potion amplifier
      duration: 60                # Ticks
      blood-cost: 4

    crimson-mist:
      enabled: true
      unlock-level: 4
      duration: 100               # Ticks
      blood-cost: 6
      night-only: true

    sire:
      enabled: true
      unlock-level: 5
      require-consent: true       # Target must accept
      cooldown: 86400             # Seconds (24 hours)

  # ------------------------------------------
  # PROGRESSION
  # ------------------------------------------
  progression:
    xp-per-feed: 5
    xp-per-player-feed: 20
    xp-per-night-survived: 10
    xp-per-day-survived: 25       # Bonus for enduring the sun

    levels:
      2: 100
      3: 300
      4: 600
      5: 1000

  # ------------------------------------------
  # WEAKNESSES
  # ------------------------------------------
  weaknesses:
    fire-damage-multiplier: 1.5
    holy-water:
      enabled: true
      damage: 4.0
    garlic:
      enabled: false              # Optional feature
      effect: WEAKNESS
      duration: 200

  # ------------------------------------------
  # INFECTION
  # ------------------------------------------
  infection:
    # Only level 5 vampires can sire others
    require-sire-ability: true
    # Or allow bite chance at any level
    bite-chance: 0.0              # 0 = disabled, only Sire ability works

# ------------------------------------------
# MESSAGES
# ------------------------------------------
messages:
  vampire:
    infected: "&4You feel your heart slow... the thirst begins."
    sun-warning: "&c&oThe sun burns your flesh..."
    night-falls: "&7Darkness embraces you. The hunt begins."
    dawn-approaches: "&eThe horizon lightens. Seek shelter."
    blood-low: "&4Your thirst grows unbearable..."
    feeding: "&4You drink deep..."
    ability-locked: "&cYou must reach level {level} to use this ability."
```

---

## Related
- [[Vampirism]]
- [[Configuration]]
- [[Commands]]
