package io.github.flemmli97.tenshilib.forge.events;

import io.github.flemmli97.tenshilib.api.item.IAOEWeapon;
import io.github.flemmli97.tenshilib.api.item.IDualWeapon;
import io.github.flemmli97.tenshilib.common.utils.AOEWeaponHandler;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.Event;

public class CommonEvents {

    public static void leftClickBlock(PlayerInteractEvent.LeftClickBlock event) {
        ItemStack stack = event.getEntity().getMainHandItem();
        if (stack.getItem() instanceof IAOEWeapon weapon) {
            AOEWeaponHandler.onAOEWeaponSwing(event.getEntity(), stack, weapon);
            event.getEntity().resetAttackStrengthTicker();
        }
    }

    public static void disableOffhand(PlayerInteractEvent.RightClickItem event) {
        if (event.getHand() == InteractionHand.OFF_HAND && event.getEntity().getMainHandItem().getItem() instanceof IDualWeapon weapon && weapon.disableOffhand())
            event.setCanceled(true);
    }

    public static void disableOffhandBlock(PlayerInteractEvent.RightClickBlock event) {
        if (event.getHand() == InteractionHand.OFF_HAND && event.getEntity().getMainHandItem().getItem() instanceof IDualWeapon weapon && weapon.disableOffhand()) {
            event.setUseItem(Event.Result.DENY);
        }
    }
}
