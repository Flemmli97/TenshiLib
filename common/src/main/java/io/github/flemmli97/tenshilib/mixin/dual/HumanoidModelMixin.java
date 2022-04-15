package io.github.flemmli97.tenshilib.mixin.dual;

import io.github.flemmli97.tenshilib.api.item.IDualWeapon;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(HumanoidModel.class)
public abstract class HumanoidModelMixin<T extends LivingEntity> {

    @Unique
    private boolean renderOtherHand;

    @Inject(method = "setupAnim", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;getUsedItemHand()Lnet/minecraft/world/InteractionHand;"))
    private void modifyArmPose(T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, CallbackInfo info) {
        this.renderOtherHand = entity.getUsedItemHand() == InteractionHand.MAIN_HAND && entity.getMainHandItem().getItem() instanceof IDualWeapon;
    }

    @SuppressWarnings("unchecked")
    @Inject(method = "poseRightArm", at = @At(value = "RETURN"))
    private void onRenderRightArm(T livingEntity, CallbackInfo info) {
        if (this.renderOtherHand) {
            ((HumanoidModel<T>) (Object) this).leftArmPose = ((HumanoidModel<T>) (Object) this).rightArmPose;
            this.poseLeftArm(livingEntity);
        }
    }

    @Shadow
    abstract void poseLeftArm(T livingEntity);
}
