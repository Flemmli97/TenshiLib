package io.github.flemmli97.tenshilib.forge.events;

import io.github.flemmli97.tenshilib.api.item.IDualWeapon;
import net.minecraft.world.InteractionHand;
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
}
