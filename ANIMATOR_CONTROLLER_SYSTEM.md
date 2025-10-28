# Animator Controller System - Unity Mechanim Style

## Overview

This mod now uses a Unity Mechanim-inspired state machine system for gun animations. This provides full control over animation durations, state transitions, and allows easy customization per gun.

## Architecture

### Base Classes

```
AnimatorController (abstract)
  └── GunAnimatorController (base for guns)
        ├── KettleGunAnimatorController (custom)
        ├── RattlerGunAnimatorController (custom)
        └── ... (add more as needed)
```

### Key Concepts

- **AnimationState**: Represents a state in the state machine (e.g., IDLE, AIM, FIRE)
  - Has: name, animation name, isPlayOnce flag, duration
  
- **AnimationTransition**: Defines when to transition between states
  - Has: from state, to state, condition function
  
- **AnimatorController**: Manages the state machine
  - Updates every tick
  - Evaluates transitions
  - Returns to loop animations after PLAY_ONCE finishes

## Creating a Custom Gun Animator

### Example: Custom Gun with Different Animation Durations

```java
public class MyCustomGunAnimatorController extends GunAnimatorController {
    
    @Override
    protected AnimationDurations getAnimationDurations() {
        return new AnimationDurations(
            0.75f,  // fire: 0.75 seconds - slower fire animation
            0.75f,  // aimFire: 0.75 seconds  
            6.0f    // reload: 6 seconds - longer reload
        );
    }
}
```

**Note**: All durations are in **SECONDS**. The system automatically converts to ticks (20 ticks per second).

### Example: Adding Custom States

```java
public class AdvancedGunAnimatorController extends GunAnimatorController {
    
    // Define custom state
    private final AnimationState INSPECT;
    
    public AdvancedGunAnimatorController() {
        super();
        // Add custom inspect animation (2 seconds)
        this.INSPECT = new AnimationState("INSPECT", "weapon.inspect", true, 2.0f);
    }
    
    @Override
    protected AnimationTransition[] getTransitions() {
        AnimationTransition[] baseTransitions = super.getTransitions();
        
        // Add transition from IDLE to INSPECT when pressing a key
        // You'd need to add the condition logic in your client code
        // This is just to show the structure
        
        return baseTransitions; // Or extend with custom transitions
    }
    
    // You could add a method to trigger inspect
    public AnimationState getInspectState() {
        return INSPECT;
    }
}
```

### Example: Conditional Animations

```java
public class ConditionalGunAnimatorController extends GunAnimatorController {
    
    @Override
    protected AnimationDurations getAnimationDurations() {
        return new AnimationDurations(
            0.5f,  // fire: 0.5 seconds
            0.5f,  // aimFire: 0.5 seconds
            5.0f   // reload: 5 seconds
        );
    }
    
    // Override condition methods for custom behavior
    @Override
    protected boolean isAiming(LocalPlayer player, ItemStack itemStack) {
        // Custom aiming logic
        boolean baseAiming = super.isAiming(player, itemStack);
        
        // Example: Only allow aiming if player is not sprinting
        return baseAiming && !player.isSprinting();
    }
}
```

## Registering Guns with Custom Animators

### In RoaItems.java:

```java
public static final DeferredItem<GunItem> MY_GUN = GunRegistryHelper.gun("my_gun")
        .ammo(AmmoType.MEDIUM, 30)
        .fireModes(GunFireMode.AUTOMATIC_FIRE)
        .startsEmpty()
        .gunRenderer()
        .stats(5f, 0.9f, 3f, 2f, 600, 50, 0.3f, 0.12f, 0.1f, 4.0f)
        .animatorController(new MyCustomGunAnimatorController())  // ← Custom controller!
        .register();
```

### Default Behavior (No Custom Controller):

If you don't specify `.animatorController()`, it will use the default `GunAnimatorController` with:
- Fire: 0.5 seconds
- Aim Fire: 0.5 seconds
- Reload: 5 seconds

## Animation Duration Guidelines

### Recommended Durations by Gun Type

**Note**: All durations are in **seconds**. The system automatically converts to ticks (20 TPS).

**Pistols**:
```java
fire: 0.4f - 0.5f seconds
aimFire: 0.4f - 0.5f seconds
reload: 2.0f - 3.0f seconds
```

**Rifles (Single Shot)**:
```java
fire: 0.5f - 0.75f seconds
aimFire: 0.5f - 0.75f seconds
reload: 3.0f - 4.0f seconds
```

**Rifles (Automatic)**:
```java
fire: 0.3f - 0.4f seconds  // Fast for rapid fire
aimFire: 0.3f - 0.4f seconds
reload: 3.0f - 5.0f seconds
```

**Shotguns**:
```java
fire: 0.6f - 1.0f seconds  // Heavy kick
aimFire: 0.6f - 1.0f seconds
reload: 4.0f - 6.0f seconds  // Slow tactical reload
```

**Sniper Rifles**:
```java
fire: 0.75f - 1.25f seconds  // Bolt action feel
aimFire: 0.75f - 1.25f seconds
reload: 4.0f - 5.0f seconds
```

## State Machine Flow

### Loop Animations (Continuous)
```
IDLE ←→ AIM
 ↕       ↕
SPRINT ←→ (blocked when aiming)
```

### PLAY_ONCE Animations (One-shot)
```
Any State → FIRE → Returns to appropriate loop state
Any State → RELOAD → Returns to appropriate loop state
```

### Example Flow:
```
1. Player standing still: IDLE (loop)
2. Player right-clicks: IDLE → AIM (loop)
3. Player fires: AIM → AIM_FIRE (play once, 0.5 seconds)
4. After 0.5 seconds: AIM_FIRE → AIM (back to loop)
5. Player stops aiming: AIM → IDLE (loop)
6. Player sprints: IDLE → SPRINT (loop)
```

## Advanced Customization

### Adding New Animation States

1. Define the state in your controller:
```java
private final AnimationState MELEE_ATTACK = 
    new AnimationState("MELEE", "weapon.melee", true, 1.0f); // 1 second
```

2. Add transition conditions:
```java
new AnimationTransition(IDLE, MELEE_ATTACK, (player, stack) -> {
    // Your condition here
    return someKeyPressed;
});
```

3. Trigger it from client code:
```java
if (myKeyPressed) {
    AnimationState meleeState = customController.getMeleeState();
    controller.forceState(meleeState, player);
}
```

### Custom Transition Conditions

Override any of these methods in your controller:

```java
protected boolean isAiming(LocalPlayer player, ItemStack itemStack) {
    // Custom aiming check
}

protected boolean isSprinting(LocalPlayer player, ItemStack itemStack) {
    // Custom sprinting check
}

protected boolean isReloading(LocalPlayer player, ItemStack itemStack) {
    // Custom reloading check
}
```

### Complex State Logic

```java
@Override
protected AnimationState getStateAfterPlayOnce(LocalPlayer player, ItemStack itemStack) {
    // After PLAY_ONCE animations, decide where to go
    
    if (isReloading(player, itemStack)) {
        return RELOAD;
    }
    
    // Custom logic: Maybe check magazine count
    GunMagazineComponent mag = itemStack.get(RoaDataComponents.GUN_MAGAZINE.get());
    if (mag != null && mag.currentAmmo() == 0) {
        return EMPTY_IDLE; // Custom empty state
    }
    
    if (isSprinting(player, itemStack)) {
        return SPRINT;
    }
    
    if (isAiming(player, itemStack)) {
        return AIM;
    }
    
    return IDLE;
}
```

## Benefits of This System

✅ **Per-Gun Customization**: Each gun can have unique animation timings  
✅ **Extensible**: Easy to add new states and transitions  
✅ **Maintainable**: Clear separation of concerns  
✅ **Familiar**: Unity developers will feel right at home  
✅ **Type-Safe**: Compile-time checking of state names  
✅ **Automatic Return**: PLAY_ONCE animations automatically return to loop states  
✅ **No Hardcoding**: No more magic numbers scattered in code  

## Migration from Old System

### Before:
```java
// Hardcoded durations in ticks scattered throughout the code
GunAnimationStateManager.notifyPlayOnceAnimation(player, 10);  // What does 10 mean?
GunAnimationStateManager.notifyPlayOnceAnimation(player, 100); // What does 100 mean?
```

### After:
```java
// Defined per-gun in AnimatorController with clear, readable seconds
.animatorController(new MyGunAnimatorController())

// With custom durations in the controller class - much clearer!
protected AnimationDurations getAnimationDurations() {
    return new AnimationDurations(0.5f, 0.5f, 5.0f);  // Obvious what these mean!
}
```

## Troubleshooting

### Animations Not Playing
- Check if animator controller is set in gun registration
- Verify animation names match AzureLib animation file
- Check DEBUG logs for state transitions

### Wrong Animation Duration
- Adjust values in `getAnimationDurations()`
- All values are in seconds (e.g., 0.5f = half second)
- Test and iterate

### Animation Doesn't Return to Loop
- Ensure duration matches actual animation length
- Check `getStateAfterPlayOnce()` logic
- Verify state machine transitions

## Future Enhancements

Possible additions:
- Animation blending/crossfade
- Sub-state machines
- Animation events/callbacks
- Parameter-based transitions
- Animation layers (e.g., upper/lower body)

