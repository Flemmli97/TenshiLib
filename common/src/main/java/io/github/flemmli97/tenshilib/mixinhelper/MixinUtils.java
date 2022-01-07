package io.github.flemmli97.tenshilib.mixinhelper;

import io.github.flemmli97.tenshilib.api.item.IDualWeapon;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

import java.util.function.Consumer;

public class MixinUtils {

    public static InteractionHand get(LivingEntity entity, InteractionHand hand, InteractionHand prevSwungHand, Consumer<InteractionHand> update) {
        if (entity.level.isClientSide && hand == InteractionHand.MAIN_HAND) {
            if (entity.getMainHandItem().getItem() instanceof IDualWeapon) {
                InteractionHand newHand = prevSwungHand == InteractionHand.MAIN_HAND ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND;
                update.accept(newHand);
                entity.swinging = false;
                return newHand;
            } else
                update.accept(InteractionHand.OFF_HAND);
        }
        return hand;
    }

    public static float offHandHeight(Player player, float val, float offHandHeight) {
        if (!(player.getMainHandItem().getItem() instanceof IDualWeapon) || (val + offHandHeight) == 0)
            return val;
        float strength = ((OffHandStrength) player).getOffhandStrengthScale(1);
        return strength * strength * strength - offHandHeight;
    }
}
