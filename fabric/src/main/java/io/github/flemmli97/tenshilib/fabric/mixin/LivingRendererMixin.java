package io.github.flemmli97.tenshilib.fabric.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import io.github.flemmli97.tenshilib.client.ClientHandlers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntityRenderer.class)
public abstract class LivingRendererMixin<T extends LivingEntity> {

    @Inject(method = "render", at = @At("HEAD"), cancellable = true)
    private void renderCheck(T entity, float entityYaw, float partialTicks, PoseStack matrixStack, MultiBufferSource buffer, int packedLight, CallbackInfo info) {
        if (ClientHandlers.shouldDisableRender(entity))
            info.cancel();
    }
}
