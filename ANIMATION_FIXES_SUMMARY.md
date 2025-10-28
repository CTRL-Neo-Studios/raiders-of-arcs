# Animation System Fixes

## Problems Fixed

### 1. ✅ Sprint Animation Persistence Bug

**Problem**: When switching away from gun while sprinting, the sprint animation persisted. When switching back, the gun still showed sprint animation even if not sprinting.

**Root Cause**: `GunAnimationStateManager` uses static fields to track animation state. When switching away from the gun, `reset()` was never called, so the state persisted as `SPRINT`. When switching back, the system thought it was still in SPRINT state and wouldn't re-dispatch until state changed.

**Solution**:
```java
// ClientTickHandler.java - when NOT holding a gun
if (!(mainHandStack.getItem() instanceof GunItem)) {
    AdsStateManager.reset();
    RecoilManager.reset();
    GunAnimationStateManager.reset(); // ← Added this!
    ...
}
```

**Result**: Animation state now properly resets when switching items.

---

### 2. ✅ PLAY_ONCE Animations Not Returning to Loop Animations

**Problem**: After firing or reloading (PLAY_ONCE animations), the gun would return to its default display state with no hands visible, instead of resuming the idle/aim/sprint animation.

**Root Cause**: 
- Fire/reload animations are PLAY_ONCE 
- After they complete, AzureLib returns to default pose
- The animation state manager only dispatches on state changes
- Since the state didn't change (still IDLE/AIM), it never re-dispatched the loop animation

**Solution**: Implemented a notification system that tracks when PLAY_ONCE animations are playing:

```java
// GunAnimationStateManager.java
private static long playOnceAnimationEndTick = 0;
private static boolean needsLoopAnimationReturn = false;

public static void notifyPlayOnceAnimation(LocalPlayer player, int durationTicks) {
    playOnceAnimationEndTick = currentTick + durationTicks;
    needsLoopAnimationReturn = true;
}
```

Then in `updateAnimationState()`:
```java
// Check if we need to return to loop animation after PLAY_ONCE finished
if (needsLoopAnimationReturn && currentTick >= playOnceAnimationEndTick) {
    needsLoopAnimationReturn = false;
    // Force re-dispatch current loop animation
    AnimationState targetState = determineTargetState(player, gunStack);
    dispatchAnimation(player, gunStack, gunItem, targetState);
    currentState = targetState;
    return;
}
```

**Notification Points**:
1. **Single/Burst Fire**: `MinecraftMixin.onLeftClick()` - 10 tick duration
2. **Automatic Fire**: `ClientTickHandler.handleAutomaticFire()` - 10 tick duration  
3. **Reload**: `ClientTickHandler.handleKeybinds()` - 100 tick duration

**Result**: After fire or reload animations complete, the system automatically returns to the appropriate loop animation (idle/aim/sprint).

---

## Animation Flow

### Before Fix
```
Player fires gun:
1. Fire animation plays (PLAY_ONCE)
2. Animation completes
3. Default pose (no hands) ← BUG!
4. Stays in default pose until state changes
```

### After Fix
```
Player fires gun:
1. Fire animation plays (PLAY_ONCE)
2. notifyPlayOnceAnimation(10 ticks) called
3. Animation completes after 10 ticks
4. System detects completion
5. Re-dispatches appropriate loop animation (idle/aim/sprint) ← FIXED!
6. Hands visible, correct animation playing
```

## Files Modified

1. **ClientTickHandler.java**
   - Added `GunAnimationStateManager.reset()` when switching away from guns
   - Added `notifyPlayOnceAnimation()` calls for automatic fire and reload

2. **MinecraftMixin.java**
   - Added `notifyPlayOnceAnimation()` call for single/burst fire

3. **GunAnimationStateManager.java**
   - Added PLAY_ONCE animation tracking system
   - Added automatic return to loop animations after completion
   - Updated `reset()` to clear tracking state

## Animation Durations

These are estimates and can be tuned:

```java
// Fire animations: 10 ticks (500ms)
GunAnimationStateManager.notifyPlayOnceAnimation(player, 10);

// Reload animations: 100 ticks (5 seconds)
GunAnimationStateManager.notifyPlayOnceAnimation(player, 100);
```

You can adjust these values in:
- `MinecraftMixin.java:45` - Single fire duration
- `ClientTickHandler.java:189` - Automatic fire duration
- `ClientTickHandler.java:199` - Reload duration

## Testing Checklist

- [ ] Sprint animation correctly plays while sprinting with gun
- [ ] Sprint animation stops when switching to another item
- [ ] When switching back to gun while not sprinting, correct animation (idle/aim) plays
- [ ] After firing, gun returns to idle animation (if standing still)
- [ ] After firing while aiming, gun returns to aim animation
- [ ] After reloading, gun returns to appropriate loop animation
- [ ] Automatic fire maintains correct animation loop
- [ ] Hands are always visible (no default pose)

## Notes

- Fire animation duration (10 ticks) works well for fast-paced shooting
- Reload animation duration (100 ticks) should match your actual reload animation length
- If animations don't sync properly, adjust the duration values to match your animation files
- The system uses game ticks (20 TPS), so multiply seconds by 20 for tick values

