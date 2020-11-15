package com.flemmli97.tenshilib.mixin;

import com.flemmli97.tenshilib.api.entity.IOverlayEntityRender;
import net.minecraft.client.renderer.entity.LivingRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Intercepts the overlay uv in LivingRenderer
 */
@Mixin(LivingRenderer.class)
public class LivingOverlayMixin {

    @Inject(method = "getOverlay", at = @At(value = "HEAD"), cancellable = true)
    private static void overlay(LivingEntity entity, float f, CallbackInfoReturnable<Integer> info) {
        if (entity instanceof IOverlayEntityRender) {
            info.setReturnValue(runecraftory_get(entity, f));
            info.cancel();
        }
    }

    private static int runecraftory_get(LivingEntity entity, float f) {
        IOverlayEntityRender overlay = (IOverlayEntityRender) entity;
        int oV = (int) (f * 15);
        int oU = (entity.hurtTime > 0 || entity.deathTime > 0) ? 3 : 10;
        return OverlayTexture.packUv(overlay.overlayU(oV), overlay.overlayV(oU));
    }
}
