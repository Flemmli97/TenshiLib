package io.github.flemmli97.tenshilib.mixinhelper;

import io.github.flemmli97.tenshilib.api.item.IAOEWeapon;
import io.github.flemmli97.tenshilib.api.item.IDualWeapon;
import net.minecraft.client.Minecraft;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffectUtil;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.function.Consumer;

public class MixinUtils {

    public static InteractionHand get(LivingEntity entity, InteractionHand hand, InteractionHand prevSwungHand, Consumer<InteractionHand> update) {
        if (entity.level.isClientSide && hand == InteractionHand.MAIN_HAND) {
            if (entity.getMainHandItem().getItem() instanceof IDualWeapon) {
                if (!entity.swinging || entity.swingTime >= getCurrentSwingDuration(entity) / 2 || entity.swingTime < 0) {
                    InteractionHand newHand = prevSwungHand == InteractionHand.MAIN_HAND ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND;
                    update.accept(newHand);
                    return newHand;
                }
                return hand;
            } else
                update.accept(InteractionHand.OFF_HAND);
        }
        return hand;
    }

    private static int getCurrentSwingDuration(LivingEntity entity) {
        if (MobEffectUtil.hasDigSpeed(entity)) {
            return 6 - (1 + MobEffectUtil.getDigSpeedAmplification(entity));
        }
        if (entity.hasEffect(MobEffects.DIG_SLOWDOWN)) {
            return 6 + (1 + entity.getEffect(MobEffects.DIG_SLOWDOWN).getAmplifier()) * 2;
        }
        return 6;
    }

    public static float offHandHeight(Player player, float val, float offHandHeight) {
        if (!(player.getMainHandItem().getItem() instanceof IDualWeapon) || (val + offHandHeight) == 0)
            return val;
        float strength = ((OffHandStrength) player).getOffhandStrengthScale(1);
        return strength * strength * strength - offHandHeight;
    }

    public static boolean disableContinueAttack() {
        Minecraft client = Minecraft.getInstance();
        ItemStack main = client.player.getMainHandItem();
        return (main.getItem() instanceof IAOEWeapon aoe && !aoe.allowBlockAttack(client.player, main));
    }
}
