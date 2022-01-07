package io.github.flemmli97.tenshilib.mixin.dual;

import io.github.flemmli97.tenshilib.mixinhelper.MixinUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ItemInHandRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(ItemInHandRenderer.class)
public abstract class ItemInHandRendererMixin {

    @Shadow
    private float offHandHeight;
    @Shadow
    private Minecraft minecraft;

    @ModifyArg(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/Mth;clamp(FFF)F", ordinal = 3), index = 0)
    private float update(float val) {
        return MixinUtils.offHandHeight(this.minecraft.player, val, this.offHandHeight);
    }
}
