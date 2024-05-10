package io.github.flemmli97.tenshilib.client;

import io.github.flemmli97.tenshilib.api.entity.AnimatedAction;
import io.github.flemmli97.tenshilib.api.entity.IAnimated;
import io.github.flemmli97.tenshilib.api.entity.IOverlayEntityRender;
import io.github.flemmli97.tenshilib.api.item.IAOEWeapon;
import io.github.flemmli97.tenshilib.api.item.IExtendedWeapon;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.HitResult;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;

public class ClientHandlers {

    public static final Set<UUID> RIDING_RENDER_BLACKLIST = new HashSet<>();

    public static void updateAnim(int entityID, int animID) {
        Minecraft mc = Minecraft.getInstance();
        Entity e = mc.level.getEntity(entityID);
        if (e instanceof IAnimated anim) {
            anim.getAnimationHandler().setAnimation(animID == -2 ? null : animID == -1 ? AnimatedAction.VANILLA_ATTACK : anim.getAnimationHandler().getAnimations()[animID]);
        }
    }

    public static int getColor(LivingEntity entity, float f) {
        IOverlayEntityRender overlay = (IOverlayEntityRender) entity;
        int oV = (int) (f * 15);
        int oU = (entity.hurtTime > 0 || entity.deathTime > 0) ? 3 : 10;
        return OverlayTexture.pack(overlay.overlayU(oV), overlay.overlayV(oU));
    }

    public static Player clientPlayer() {
        return Minecraft.getInstance().player;
    }

    public static boolean shouldDisableRender(Entity entity) {
        if (entity.getVehicle() instanceof LivingEntity vehicle && CustomRiderRendererManager.getInstance().hasRiderLayerRenderer(vehicle.getType())) {
            if (ClientHandlers.RIDING_RENDER_BLACKLIST.contains(entity.getUUID()))
                return false;
            return entity != Minecraft.getInstance().player || !Minecraft.getInstance().options.getCameraType().isFirstPerson();
        }
        return false;
    }

    public static boolean emptyClick(Consumer<Boolean> packetHandler) {
        Minecraft client = Minecraft.getInstance();
        ItemStack main = client.player.getMainHandItem();
        if (client.hitResult != null && (client.hitResult.getType() != HitResult.Type.BLOCK || (main.getItem() instanceof IAOEWeapon aoe && !aoe.allowBlockAttack(client.player, main)))) {
            if (main.getItem() instanceof IExtendedWeapon weapon) {
                packetHandler.accept(false);
                if (weapon.resetAttackStrength(client.player, main))
                    client.player.resetAttackStrengthTicker();
                if (weapon.swingWeapon(client.player, main))
                    client.player.swing(InteractionHand.MAIN_HAND);
                return true;
            } else if (main.getItem() instanceof IAOEWeapon weapon) {
                packetHandler.accept(true);
                if (weapon.resetAttackStrength(client.player, main))
                    client.player.resetAttackStrengthTicker();
                if (weapon.swingWeapon(client.player, main))
                    client.player.swing(InteractionHand.MAIN_HAND);
                return true;
            }
        }
        return false;
    }
}
