package io.github.flemmli97.tenshilib.mixin.dual;

import io.github.flemmli97.tenshilib.common.item.DualWeapon;
import io.github.flemmli97.tenshilib.mixinhelper.LastSwungHand;
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
public abstract class LivingEntityMixin implements LastSwungHand {

    @Unique
    private InteractionHand tenshilib$prevSwungHand = InteractionHand.OFF_HAND;

    @Inject(method = "getOffhandItem", at = @At(value = "HEAD"), cancellable = true)
    private void offhandItem(CallbackInfoReturnable<ItemStack> info) {
        if (((LivingEntity) (Object) this).level().isClientSide && ((LivingEntity) (Object) this).getMainHandItem().getItem() instanceof DualWeapon dual) {
            info.setReturnValue(dual.offHandStack(((LivingEntity) (Object) this)));
            info.cancel();
        }
    }

    @Inject(method = "getItemInHand", at = @At(value = "HEAD"), cancellable = true)
    private void offhandItemGeneric(InteractionHand hand, CallbackInfoReturnable<ItemStack> info) {
        if (hand == InteractionHand.OFF_HAND && ((LivingEntity) (Object) this).level().isClientSide && ((LivingEntity) (Object) this).getMainHandItem().getItem() instanceof DualWeapon dual) {
            info.setReturnValue(dual.offHandStack(((LivingEntity) (Object) this)));
            info.cancel();
        }
    }

    @ModifyVariable(method = "swing(Lnet/minecraft/world/InteractionHand;Z)V", at = @At(value = "HEAD"), argsOnly = true)
    private InteractionHand swingHook(InteractionHand hand) {
        return MixinUtils.get(((LivingEntity) (Object) this), hand, this.tenshilib$prevSwungHand, v -> this.tenshilib$prevSwungHand = v);
    }

    @Override
    public InteractionHand tenshilib$lastSwungHand() {
        return this.tenshilib$prevSwungHand;
    }

    @Override
    public void tenshilib$SetLastSwungHand(InteractionHand hand) {
        this.tenshilib$prevSwungHand = hand;
    }
}
