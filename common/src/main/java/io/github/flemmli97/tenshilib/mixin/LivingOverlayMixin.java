package io.github.flemmli97.tenshilib.mixin;

import io.github.flemmli97.tenshilib.api.entity.IOverlayEntityRender;
import io.github.flemmli97.tenshilib.client.ClientHandlers;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Intercepts the overlay uv in LivingRenderer
 */
@Mixin(LivingEntityRenderer.class)
public abstract class LivingOverlayMixin {

    @Inject(method = "getOverlayCoords", at = @At(value = "HEAD"), cancellable = true)
    private static void overlay(LivingEntity entity, float f, CallbackInfoReturnable<Integer> info) {
        if (entity instanceof IOverlayEntityRender) {
            info.setReturnValue(ClientHandlers.getColor(entity, f));
            info.cancel();
        }
    }
}
