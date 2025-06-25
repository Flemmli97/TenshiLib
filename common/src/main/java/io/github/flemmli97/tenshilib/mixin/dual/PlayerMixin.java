package io.github.flemmli97.tenshilib.mixin.dual;

import io.github.flemmli97.tenshilib.mixinhelper.LastSwungHand;
import io.github.flemmli97.tenshilib.mixinhelper.OffHandStrength;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Player.class)
public abstract class PlayerMixin implements OffHandStrength {

    @Unique
    private int tenshilib$attackStrengthOffhand;

    @Inject(method = "tick", at = @At(value = "RETURN"))
    private void tickStrength(CallbackInfo info) {
        ++this.tenshilib$attackStrengthOffhand;
    }

    @Inject(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Player;resetAttackStrengthTicker()V"))
    private void onSwapItem(CallbackInfo info) {
        this.tenshilib$attackStrengthOffhand = 0;
        ((LastSwungHand) this).tenshilib$SetLastSwungHand(InteractionHand.OFF_HAND);
    }

    @Inject(method = "resetAttackStrengthTicker", at = @At(value = "HEAD"), cancellable = true)
    private void resetOffhand(CallbackInfo info) {
        if (((LastSwungHand) this).tenshilib$lastSwungHand() == InteractionHand.MAIN_HAND) {
            this.tenshilib$attackStrengthOffhand = 0;
            info.cancel();
        }
    }

    @Override
    public float tenshilib$GetOffhandStrengthScale(float f) {
        return Mth.clamp(((float) this.tenshilib$attackStrengthOffhand + f) / ((Player) (Object) this).getCurrentItemAttackStrengthDelay(), 0.0F, 1.0F);
    }
}
