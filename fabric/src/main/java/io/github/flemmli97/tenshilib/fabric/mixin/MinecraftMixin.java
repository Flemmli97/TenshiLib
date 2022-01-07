package io.github.flemmli97.tenshilib.fabric.mixin;

import io.github.flemmli97.tenshilib.fabric.client.events.ClientEvents;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Minecraft.class)
public class MinecraftMixin {

    @Inject(method = "startAttack", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/phys/HitResult;getType()Lnet/minecraft/world/phys/HitResult$Type;", shift = At.Shift.BY, by = -3), cancellable = true)
    private void leftClickAttack(CallbackInfo info) {
        if (ClientEvents.clickSpecial())
            info.cancel();
    }
}
