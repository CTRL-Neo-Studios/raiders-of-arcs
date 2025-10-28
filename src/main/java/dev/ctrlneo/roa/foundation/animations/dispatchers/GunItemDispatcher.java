package dev.ctrlneo.roa.foundation.animations.dispatchers;

import mod.azure.azurelib.common.animation.dispatch.command.AzCommand;
import mod.azure.azurelib.common.animation.play_behavior.AzPlayBehaviors;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;

public class GunItemDispatcher {
    private static final AzCommand IDLE_COMMAND = AzCommand.create("base_controller", "weapon.idle", AzPlayBehaviors.LOOP);
    private static final AzCommand AIM_COMMAND = AzCommand.create("base_controller", "weapon.aim", AzPlayBehaviors.LOOP);
    private static final AzCommand SPRINT_COMMAND = AzCommand.create("base_controller", "weapon.sprinting", AzPlayBehaviors.LOOP);
    private static final AzCommand FIRE_COMMAND = AzCommand.create("base_controller", "weapon.fire", AzPlayBehaviors.HOLD_ON_LAST_FRAME);
    private static final AzCommand AIM_FIRE_COMMAND = AzCommand.create("base_controller", "weapon.aim_fire", AzPlayBehaviors.HOLD_ON_LAST_FRAME);
    private static final AzCommand RELOAD_COMMAND = AzCommand.create("base_controller", "weapon.reload", AzPlayBehaviors.HOLD_ON_LAST_FRAME);

    public void fire(Entity entity, ItemStack itemStack) {
        FIRE_COMMAND.sendForItem(entity, itemStack);
//        idle(entity, itemStack);
    }

    public void idle(Entity entity, ItemStack itemStack) {
        IDLE_COMMAND.sendForItem(entity, itemStack);
    }

    public void aim(Entity entity, ItemStack itemStack) {
        AIM_COMMAND.sendForItem(entity, itemStack);
    }

    public void aimFire(Entity entity, ItemStack itemStack) {
        AIM_FIRE_COMMAND.sendForItem(entity, itemStack);
//        aim(entity, itemStack);
    }

    public void sprint(Entity entity, ItemStack itemStack) {
        SPRINT_COMMAND.sendForItem(entity, itemStack);
    }

    public void reload(Entity entity, ItemStack itemStack) {
        RELOAD_COMMAND.sendForItem(entity, itemStack);
//        idle(entity, itemStack);
    }
}
