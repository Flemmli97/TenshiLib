package io.github.flemmli97.tenshilib.mixin;

import io.github.flemmli97.tenshilib.mixinhelper.MixinUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.world.phys.HitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Minecraft.class)
public class MinecraftMixin {

    @Inject(method = "continueAttack", at = @At("HEAD"), cancellable = true)
    private void onContinueAttack(boolean leftClick, CallbackInfo info) {
        if (leftClick && ((Minecraft) (Object) this).hitResult != null && ((Minecraft) (Object) this).hitResult.getType() == HitResult.Type.BLOCK && MixinUtils.disableContinueAttack())
            info.cancel();
    }
}
