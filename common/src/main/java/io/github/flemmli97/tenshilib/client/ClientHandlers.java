package io.github.flemmli97.tenshilib.client;

import io.github.flemmli97.tenshilib.client.gui.AnimationScreen;
import io.github.flemmli97.tenshilib.common.entity.OverlayEntityRender;
import io.github.flemmli97.tenshilib.common.entity.animated.AnimatedEntity;
import io.github.flemmli97.tenshilib.common.item.AnimationDebugger;
import io.github.flemmli97.tenshilib.common.item.ExtendedWeapon;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class ClientHandlers {

    public static final Set<UUID> RIDING_RENDER_BLACKLIST = new HashSet<>();

    public static int getColor(LivingEntity entity, float f) {
        OverlayEntityRender overlay = (OverlayEntityRender) entity;
        int oV = (int) (f * 15);
        int oU = (entity.hurtTime > 0 || entity.deathTime > 0) ? 3 : 10;
        return OverlayTexture.pack(overlay.overlayU(oV), overlay.overlayV(oU));
    }

    public static boolean shouldDisableRender(Entity entity) {
        if (entity.getVehicle() instanceof LivingEntity vehicle && CustomRiderRendererManager.getInstance().hasRiderLayerRenderer(vehicle.getType())) {
            if (ClientHandlers.RIDING_RENDER_BLACKLIST.contains(entity.getUUID()))
                return false;
            return entity != Minecraft.getInstance().player || !Minecraft.getInstance().options.getCameraType().isFirstPerson();
        }
        return false;
    }

    public static boolean onClientClick() {
        Minecraft client = Minecraft.getInstance();
        ItemStack main = client.player.getMainHandItem();
        return main.getItem() instanceof ExtendedWeapon weapon && weapon.onClientStartAttack(client.player, main, client.hitResult);
    }

    public static <T extends LivingEntity & AnimatedEntity> void openAnimationGui(T entity, InteractionHand hand) {
        ItemStack stack = Minecraft.getInstance().player.getItemInHand(hand);
        if (stack.getItem() instanceof AnimationDebugger debug) {
            Minecraft.getInstance().setScreen(new AnimationScreen<>(entity, hand, debug.getId(stack)));
        }
    }
}
