# Changelog

All notable changes to Afflictions will be documented here.

---

## [Unreleased]

### Added
- Initial project setup with Gradle + Shadow plugin
- Paper plugin structure (paper-plugin.yml)
- Obsidian documentation vault

**Core Systems:**
- Component-based affliction architecture
  - `Affliction`, `AfflictionInstance`, `AfflictionComponent` interfaces
  - Effect components: `DamageEffect`, `PotionEffectComponent`, `AttributeModifierEffect`
  - Trigger components: `TimeTrigger`, `SunlightTrigger`, `MoonPhaseTrigger`
- `AfflictionManager` with configurable tick rate
- `AfflictionRegistry` for affliction type registration
- `PlayerManager` for in-memory player data

**Commands (Cloud framework):**
- `/afflictions list` - View your active afflictions
- `/afflictions info <affliction>` - View affliction details
- `/afflictions give <player> <affliction> [level]` - Admin: give affliction
- `/afflictions remove <player> <affliction>` - Admin: remove affliction
- `/afflictions clear <player>` - Admin: clear all afflictions
- `/afflictions reload` - Admin: reload configuration
- Aliases: `/aff`, `/afflict`

**Storage:**
- SQLite storage implementation (async)
- `Storage` interface for future backends
- Automatic player data load on join, save on quit
- Custom affliction data serialization (JSON)
- Hybrid offline/online mode support
  - `player-lookup` config option: `auto`, `uuid`, `name`
  - Username-based lookup for offline mode servers
  - Automatic database migration for existing databases

**Localization:**
- `LocalizationManager` with MiniMessage support
- Small caps Unicode styling (EssentialsX-inspired)
- Custom symbols and hex colors
- English (`en.yml`) language file

**Utilities:**
- `TimeUtil` with moon phase calculation (`day mod 8`)
- `TimeListener` for day/night transition broadcasts
- `TaskUtil` for scheduler helpers

**Configuration:**
- BoostedYAML for config management
- `config.yml` with storage, language, tick-rate settings
- MySQL settings placeholder (not yet implemented)

**Afflictions:**
- Vampirism affliction implementation
  - `SunlightDamageComponent` - deals damage when exposed to sunlight
  - Damage scales inversely with level (higher level = more control)
  - Helmet provides 50% damage reduction
  - Weather (rain/storm) provides protection
  - Visual fire effect when burning

**PlaceholderAPI Integration:**
- `AfflictionsExpansion` for PlaceholderAPI
- General placeholders: `count`, `list`, `has_any`
- Affliction-specific: `has_<id>`, `level_<id>`, `<id>_name`, `<id>_affliction`, `<id>_prefix`
- Time placeholders: `time`, `time_raw`, `moon_phase`, `moon_symbol`
- MiniMessage to legacy conversion for PAPI compatibility
- Full support for TAB, scoreboards, and chat plugins

**Time & Moon System:**
- `TimeUtil.MoonPhase` enum with 8 phases and symbols
- Day/night transition broadcasts via `TimeListener`
- Configurable moon phase names and symbols in locale files

### Planned
- MySQL/MariaDB storage support
- Werewolf affliction implementation
- Vampirism additional features (night vision, blood thirst, abilities)
- Custom events API

---

## Version History

<!-- Future releases go here -->
<!--
## [1.0.0] - YYYY-MM-DD
### Added
### Changed
### Fixed
### Removed
-->
