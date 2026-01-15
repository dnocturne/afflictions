# API Design

Public API for other plugins to interact with Afflictions.

---

## Getting the API

```java
AfflictionsAPI api = AfflictionsProvider.get();
```

---

## Core API Methods

### Affliction Management

```java
// Apply an affliction
api.applyAffliction(Player player, String afflictionType);
api.applyAffliction(Player player, String afflictionType, int severity);

// Remove an affliction
api.removeAffliction(Player player, String afflictionType);
api.clearAfflictions(Player player);

// Check afflictions
boolean hasAffliction(Player player, String afflictionType);
List<ActiveAffliction> getAfflictions(Player player);
```

### Affliction Registry

```java
// Register custom afflictions
api.registerAffliction(CustomAffliction affliction);

// Get affliction info
AfflictionType getAfflictionType(String name);
Collection<AfflictionType> getAllAfflictionTypes();
```

---

## Events

```java
// Fired when a player contracts an affliction
AfflictionGainEvent
  - Player getPlayer()
  - AfflictionType getAffliction()
  - int getSeverity()
  - setCancelled(boolean)

// Fired when an affliction is cured/removed
AfflictionLostEvent
  - Player getPlayer()
  - AfflictionType getAffliction()
  - LostReason getReason() // CURED, EXPIRED, ADMIN, OTHER

// Fired when affliction severity changes
AfflictionProgressEvent
  - Player getPlayer()
  - AfflictionType getAffliction()
  - int getOldSeverity()
  - int getNewSeverity()
  - setCancelled(boolean)
```

---

## Custom Afflictions

```java
public class MyAffliction implements Affliction {
    @Override
    public String getName() { return "my_affliction"; }

    @Override
    public void onApply(Player player, int severity) { }

    @Override
    public void onTick(Player player, int severity) { }

    @Override
    public void onRemove(Player player) { }
}
```

---

## See Also
- [[Architecture]]
