package io.github.flemmli97.tenshilib.neoforge.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import io.github.flemmli97.tenshilib.mixinhelper.PlayerAttackAccess;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Player.class)
public class NeoForgeSweepCheck {

    @ModifyExpressionValue(method = "attack", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;canPerformAction(Lnet/neoforged/neoforge/common/ItemAbility;)Z"))
    private boolean onSweepCheckPost(boolean orig) {
        return !((PlayerAttackAccess) this).tenshilib$IsSweepDisabled() && orig;
    }
}
