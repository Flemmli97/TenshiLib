package io.github.flemmli97.tenshilib.neoforge.events;

import io.github.flemmli97.tenshilib.common.entity.animated.AnimatedEntity;
import io.github.flemmli97.tenshilib.common.entity.animated.AnimationState;
import io.github.flemmli97.tenshilib.common.item.AOEWeapon;
import io.github.flemmli97.tenshilib.common.item.AOEWeaponHandler;
import io.github.flemmli97.tenshilib.common.item.DualWeapon;
import io.github.flemmli97.tenshilib.common.network.S2CEntityAnimation;
import io.github.flemmli97.tenshilib.loader.LoaderNetwork;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.common.util.TriState;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

public class CommonEvents {

    public static void leftClickBlock(PlayerInteractEvent.LeftClickBlock event) {
        ItemStack stack = event.getEntity().getMainHandItem();
        if (stack.getItem() instanceof AOEWeapon weapon) {
            AOEWeaponHandler.onAOEWeaponSwing(event.getEntity(), stack, weapon);
            event.getEntity().resetAttackStrengthTicker();
        }
    }

    public static void disableOffhand(PlayerInteractEvent.RightClickItem event) {
        if (event.getHand() == InteractionHand.OFF_HAND && event.getEntity().getMainHandItem().getItem() instanceof DualWeapon weapon && weapon.disableOffhand())
            event.setCanceled(true);
    }

    public static void disableOffhandBlock(PlayerInteractEvent.RightClickBlock event) {
        if (event.getHand() == InteractionHand.OFF_HAND && event.getEntity().getMainHandItem().getItem() instanceof DualWeapon weapon && weapon.disableOffhand()) {
            event.setUseItem(TriState.FALSE);
        }
    }

    public static void onTracking(PlayerEvent.StartTracking event) {
        if (event.getTarget() instanceof AnimatedEntity animated && animated.getAnimationHandler().hasAnimation()) {
            AnimationState anim = animated.getAnimationHandler().getAnimation();
            LoaderNetwork.INSTANCE.sendToPlayer(S2CEntityAnimation.create((Entity & AnimatedEntity) event.getTarget(),
                    anim.getStartTransition(), anim.getEndTransitionTime(), anim.getTick(1)), (ServerPlayer) event.getEntity());
        }
    }
}
