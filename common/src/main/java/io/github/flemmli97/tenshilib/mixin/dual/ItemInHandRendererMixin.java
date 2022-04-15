package io.github.flemmli97.tenshilib.mixin.dual;

import io.github.flemmli97.tenshilib.api.item.IDualWeapon;
import io.github.flemmli97.tenshilib.mixinhelper.MixinUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.world.InteractionHand;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemInHandRenderer.class)
public abstract class ItemInHandRendererMixin {

    @Shadow
    private float offHandHeight;
    @Shadow
    @Final
    private Minecraft minecraft;

    @ModifyVariable(method = "renderArmWithItem", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/AbstractClientPlayer;isUsingItem()Z", ordinal = 1), argsOnly = true)
    private InteractionHand renderDualInArm(InteractionHand hand, AbstractClientPlayer player) {
        if (player.getMainHandItem().getItem() instanceof IDualWeapon)
            return player.getUsedItemHand();
        return hand;
    }

    @ModifyArg(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/Mth;clamp(FFF)F", ordinal = 3), index = 0)
    private float update(float val) {
        return MixinUtils.offHandHeight(this.minecraft.player, val, this.offHandHeight);
    }

    @Inject(method = "itemUsed", at = @At("HEAD"))
    private void itemUsed(InteractionHand hand, CallbackInfo info) {
        if (hand == InteractionHand.MAIN_HAND && this.minecraft.player.getMainHandItem().getItem() instanceof IDualWeapon)
            this.offHandHeight = 0;
    }
}
