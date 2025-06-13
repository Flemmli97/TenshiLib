package io.github.flemmli97.tenshilib.fabric.events;

import io.github.flemmli97.tenshilib.common.item.DualWeapon;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class CommonEvents {

    public static InteractionResultHolder<ItemStack> disableOffhand(Player player, Level level, InteractionHand hand) {
        if (hand == InteractionHand.OFF_HAND && player.getMainHandItem().getItem() instanceof DualWeapon weapon && weapon.disableOffhand()) {
            return InteractionResultHolder.fail(player.getOffhandItem());
        }
        return InteractionResultHolder.pass(player.getOffhandItem());
    }

    public static boolean disableOffhandBlock(Player player, Level level, InteractionHand hand) {
        return hand == InteractionHand.OFF_HAND && player.getMainHandItem().getItem() instanceof DualWeapon weapon && weapon.disableOffhand();
    }
}
