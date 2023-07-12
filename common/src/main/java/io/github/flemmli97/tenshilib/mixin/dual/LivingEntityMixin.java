package io.github.flemmli97.tenshilib.mixin.dual;

import io.github.flemmli97.tenshilib.api.item.IDualWeapon;
import io.github.flemmli97.tenshilib.mixinhelper.ILastHand;
import io.github.flemmli97.tenshilib.mixinhelper.MixinUtils;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin implements ILastHand {

    @Unique
    private InteractionHand prevSwungHand = InteractionHand.OFF_HAND;

    @Inject(method = "getOffhandItem", at = @At(value = "HEAD"), cancellable = true)
    private void offhandItem(CallbackInfoReturnable<ItemStack> info) {
        if (((LivingEntity) (Object) this).level().isClientSide && ((LivingEntity) (Object) this).getMainHandItem().getItem() instanceof IDualWeapon dual) {
            info.setReturnValue(dual.offHandStack(((LivingEntity) (Object) this)));
            info.cancel();
        }
    }

    @Inject(method = "getItemInHand", at = @At(value = "HEAD"), cancellable = true)
    private void offhandItemGeneric(InteractionHand hand, CallbackInfoReturnable<ItemStack> info) {
        if (hand == InteractionHand.OFF_HAND && ((LivingEntity) (Object) this).level().isClientSide && ((LivingEntity) (Object) this).getMainHandItem().getItem() instanceof IDualWeapon dual) {
            info.setReturnValue(dual.offHandStack(((LivingEntity) (Object) this)));
            info.cancel();
        }
    }

    @ModifyVariable(method = "swing(Lnet/minecraft/world/InteractionHand;Z)V", at = @At(value = "HEAD"), argsOnly = true)
    private InteractionHand swingHook(InteractionHand hand) {
        return MixinUtils.get(((LivingEntity) (Object) this), hand, this.prevSwungHand, v -> this.prevSwungHand = v);
    }

    @Override
    public InteractionHand lastSwungHand() {
        return this.prevSwungHand;
    }

    @Override
    public void setLastSwungHand(InteractionHand hand) {
        this.prevSwungHand = hand;
    }
}
