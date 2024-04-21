package io.github.flemmli97.tenshilib.forge.events;

import io.github.flemmli97.tenshilib.api.entity.IAnimated;
import io.github.flemmli97.tenshilib.api.item.IDualWeapon;
import io.github.flemmli97.tenshilib.common.network.S2CEntityAnimation;
import io.github.flemmli97.tenshilib.forge.network.PacketHandler;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.Event;

public class CommonEvents {

    public static void disableOffhand(PlayerInteractEvent.RightClickItem event) {
        if (event.getHand() == InteractionHand.OFF_HAND && event.getPlayer().getMainHandItem().getItem() instanceof IDualWeapon weapon && weapon.disableOffhand())
            event.setCanceled(true);
    }

    public static void disableOffhandBlock(PlayerInteractEvent.RightClickBlock event) {
        if (event.getHand() == InteractionHand.OFF_HAND && event.getPlayer().getMainHandItem().getItem() instanceof IDualWeapon weapon && weapon.disableOffhand()) {
            event.setUseItem(Event.Result.DENY);
        }
    }

    public static void onTracking(PlayerEvent.StartTracking event) {
        if (event.getTarget() instanceof IAnimated animated && animated.getAnimationHandler().hasAnimation()) {
            PacketHandler.sendToClientChecked(S2CEntityAnimation.create((Entity & IAnimated) event.getTarget()), (ServerPlayer) event.getPlayer());
        }
    }
}
