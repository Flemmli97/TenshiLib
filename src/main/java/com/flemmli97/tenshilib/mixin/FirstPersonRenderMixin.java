package com.flemmli97.tenshilib.mixin;

import com.flemmli97.tenshilib.api.item.IDualWeapon;
import com.flemmli97.tenshilib.common.entity.ILastHand;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.AbstractClientPlayerEntity;
import net.minecraft.client.renderer.FirstPersonRenderer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.math.MathHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(FirstPersonRenderer.class)
public class FirstPersonRenderMixin {

    @Shadow
    private ItemStack itemStackMainHand;
    @Shadow
    private ItemStack itemStackOffHand;
    @Shadow
    private float equippedProgressMainHand;
    @Shadow
    private float equippedProgressOffHand;

    @Inject(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/entity/player/ClientPlayerEntity;getCooledAttackStrength(F)F"), cancellable = true)
    private void adjustForDual(CallbackInfo info) {
        if (Minecraft.getInstance().player.getHeldItemMainhand().getItem() instanceof IDualWeapon) {
            info.cancel();
            this.handleDualAnims();
        }
    }

    private void handleDualAnims() {
        AbstractClientPlayerEntity player = Minecraft.getInstance().player;
        ItemStack main = player.getHeldItemMainhand();
        ItemStack off = player.getHeldItemOffhand();
        boolean requipM = net.minecraftforge.client.ForgeHooksClient.shouldCauseReequipAnimation(this.itemStackMainHand, main, player.inventory.currentItem);
        boolean requipO = net.minecraftforge.client.ForgeHooksClient.shouldCauseReequipAnimation(this.itemStackOffHand, off, -1);

        float f;
        float f2;

        if (requipM) {
            requipO = true;
            f = player.getCooledAttackStrength(1.0F);
            f2 = f;
        } else if (((ILastHand) player).lastSwungHand() == Hand.MAIN_HAND) {
            f = player.getCooledAttackStrength(1.0F);
            f2 = 1;//((IPlayerOffhandCooldown)player).getOffhandCooldown(1.0F);
        } else {
            f2 = player.getCooledAttackStrength(1.0F);
            f = 1;//((IPlayerOffhandCooldown)player).getOffhandCooldown(1.0F);
        }

        if (!requipM && this.itemStackMainHand != main)
            this.itemStackMainHand = main;
        if (!requipO && this.itemStackOffHand != off)
            this.itemStackOffHand = off;
        this.equippedProgressMainHand += MathHelper.clamp((!requipM ? f * f * f : 0) - this.equippedProgressMainHand, -0.4F, 0.4F);
        this.equippedProgressOffHand += MathHelper.clamp((!requipO ? f2 * f2 * f2 : 0) - this.equippedProgressOffHand, -0.4F, 0.4F);
        if (this.equippedProgressMainHand < 0.1F) {
            this.itemStackMainHand = main;
        }

        if (this.equippedProgressOffHand < 0.1F) {
            this.itemStackOffHand = off;
        }
    }
}
