package com.flemmli97.tenshilib.client;

import com.flemmli97.tenshilib.api.entity.IOverlayEntityRender;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.entity.LivingEntity;

public class OverlayRenderUtils {

    public static int getColor(LivingEntity entity, float f) {
        IOverlayEntityRender overlay = (IOverlayEntityRender) entity;
        int oV = (int) (f * 15);
        int oU = (entity.hurtTime > 0 || entity.deathTime > 0) ? 3 : 10;
        return OverlayTexture.getPackedUV(overlay.overlayU(oV), overlay.overlayV(oU));
    }
}
