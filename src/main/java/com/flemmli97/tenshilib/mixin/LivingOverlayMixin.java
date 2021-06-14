package com.flemmli97.tenshilib.mixin;

import com.flemmli97.tenshilib.api.entity.IOverlayEntityRender;
import com.flemmli97.tenshilib.client.OverlayRenderUtils;
import net.minecraft.client.renderer.entity.LivingRenderer;
import net.minecraft.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Intercepts the overlay uv in LivingRenderer
 */
@Mixin(LivingRenderer.class)
public abstract class LivingOverlayMixin {

    @Inject(method = "getPackedOverlay", at = @At(value = "HEAD"), cancellable = true)
    private static void overlay(LivingEntity entity, float f, CallbackInfoReturnable<Integer> info) {
        if (entity instanceof IOverlayEntityRender) {
            info.setReturnValue(OverlayRenderUtils.getColor(entity, f));
            info.cancel();
        }
    }
}
