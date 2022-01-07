package io.github.flemmli97.tenshilib.fabric.events;

import io.github.flemmli97.tenshilib.api.item.IAOEWeapon;
import io.github.flemmli97.tenshilib.api.item.IDualWeapon;
import io.github.flemmli97.tenshilib.common.utils.AOEWeaponHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class CommonEvents {

    public static InteractionResult leftClickBlock(Player player, Level world, InteractionHand hand, BlockPos pos, Direction direction) {
        ItemStack stack = player.getMainHandItem();
        if (stack.getItem() instanceof IAOEWeapon weapon) {
            AOEWeaponHandler.onAOEWeaponSwing(player, stack, weapon);
            player.resetAttackStrengthTicker();
        }
        return InteractionResult.PASS;
    }

    public static InteractionResultHolder<ItemStack> disableOffhand(Player player, Level level, InteractionHand hand) {
        if (hand == InteractionHand.OFF_HAND && player.getMainHandItem().getItem() instanceof IDualWeapon weapon && weapon.disableOffhand()) {
            return InteractionResultHolder.fail(player.getOffhandItem());
        }
        return InteractionResultHolder.pass(player.getOffhandItem());
    }

    public static boolean disableOffhandBlock(Player player, Level level, InteractionHand hand) {
        if (hand == InteractionHand.OFF_HAND && player.getMainHandItem().getItem() instanceof IDualWeapon weapon && weapon.disableOffhand()) {
            return true;
        }
        return false;
    }
}
