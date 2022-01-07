package io.github.flemmli97.tenshilib.client;

import io.github.flemmli97.tenshilib.api.entity.AnimatedAction;
import io.github.flemmli97.tenshilib.api.entity.IAnimated;
import io.github.flemmli97.tenshilib.api.entity.IOverlayEntityRender;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;

public class ClientHandlers {

    public static void updateAnim(int entityID, int animID) {
        Minecraft mc = Minecraft.getInstance();
        Entity e = mc.level.getEntity(entityID);
        if (e instanceof IAnimated anim) {
            anim.getAnimationHandler().setAnimation(animID == -2 ? null : animID == -1 ? AnimatedAction.vanillaAttack : anim.getAnimationHandler().getAnimations()[animID]);
        }
    }

    public static int getColor(LivingEntity entity, float f) {
        IOverlayEntityRender overlay = (IOverlayEntityRender) entity;
        int oV = (int) (f * 15);
        int oU = (entity.hurtTime > 0 || entity.deathTime > 0) ? 3 : 10;
        return OverlayTexture.pack(overlay.overlayU(oV), overlay.overlayV(oU));
    }
}
