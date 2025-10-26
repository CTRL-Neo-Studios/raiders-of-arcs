package dev.ctrlneo.roa.foundation.mixin;

import dev.ctrlneo.roa.foundation.RoaItems;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ItemStack.class)
public abstract class ItemStackMixin {

    @Shadow
    public abstract net.minecraft.world.item.Item getItem();

    /**
     * Override max stack size for our ammo items
     */
    @Inject(method = "getMaxStackSize", at = @At("RETURN"), cancellable = true)
    private void overrideMaxStack(CallbackInfoReturnable<Integer> cir) {
        if (getItem() == RoaItems.LIGHT_AMMO.get()) {
            cir.setReturnValue(100);
        } else if (getItem() == RoaItems.MEDIUM_AMMO.get()) {
            cir.setReturnValue(80);
        } else if (getItem() == RoaItems.HEAVY_AMMO.get()) {
            cir.setReturnValue(40);
        }
    }
}