package com.flemmli97.tenshilib.mixin;

import com.flemmli97.tenshilib.api.item.IDualWeapon;
import com.flemmli97.tenshilib.common.entity.ILastHand;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public class LivingEntityMixin implements ILastHand {

    @Unique
    private Hand prevSwungHand = Hand.OFF_HAND;

    @Inject(method = "getHeldItemOffhand", at = @At(value = "HEAD"), cancellable = true)
    private void offhandItem(CallbackInfoReturnable<ItemStack> info) {
        if (((LivingEntity) (Object) this).world.isRemote && ((LivingEntity) (Object) this).getHeldItemMainhand().getItem() instanceof IDualWeapon) {
            info.setReturnValue(((IDualWeapon) ((LivingEntity) (Object) this).getHeldItemMainhand().getItem()).offHandStack(((LivingEntity) (Object) this)));
            info.cancel();
        }
    }

    @Inject(method = "getHeldItem", at = @At(value = "HEAD"), cancellable = true)
    private void offhandItemGeneric(Hand hand, CallbackInfoReturnable<ItemStack> info) {
        if (hand == Hand.OFF_HAND && ((LivingEntity) (Object) this).world.isRemote && ((LivingEntity) (Object) this).getHeldItemMainhand().getItem() instanceof IDualWeapon) {
            info.setReturnValue(((IDualWeapon) ((LivingEntity) (Object) this).getHeldItemMainhand().getItem()).offHandStack(((LivingEntity) (Object) this)));
            info.cancel();
        }
    }

    @Inject(method = "swingHand", at = @At(value = "HEAD"), cancellable = true) //mcp: swing
    private void swingHook(Hand hand, boolean updateSelf, CallbackInfo info) {
        if (((LivingEntity) (Object) this).getHeldItemMainhand().getItem() instanceof IDualWeapon) {
            if (hand == this.prevSwungHand) {
                info.cancel();
                ((LivingEntity) (Object) this).swingHand(hand == Hand.MAIN_HAND ? Hand.OFF_HAND : Hand.MAIN_HAND, updateSelf);
            } else
                this.prevSwungHand = hand;
        } else
            this.prevSwungHand = Hand.OFF_HAND;
    }

    @Override
    public Hand lastSwungHand() {
        return this.prevSwungHand;
    }

    @Override
    public void updateLastHand() {
        this.prevSwungHand = this.prevSwungHand == Hand.MAIN_HAND ? Hand.OFF_HAND : Hand.MAIN_HAND;
    }
}
