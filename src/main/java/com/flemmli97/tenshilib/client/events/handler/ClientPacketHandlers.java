package com.flemmli97.tenshilib.client.events.handler;

import com.flemmli97.tenshilib.api.entity.AnimatedAction;
import com.flemmli97.tenshilib.api.entity.IAnimated;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;

public class ClientPacketHandlers {

    public static void updateAnim(int entityID, int animID) {
        Minecraft mc = Minecraft.getInstance();
        Entity e = mc.world.getEntityByID(entityID);
        if (e instanceof IAnimated) {
            IAnimated anim = (IAnimated) e;
            anim.getAnimationHandler().setAnimation(animID == -2 ? null : animID == -1 ? AnimatedAction.vanillaAttack : anim.getAnimationHandler().getAnimations()[animID]);
        }
    }
}
