package io.github.flemmli97.tenshilib.fabric.mixin;

import io.github.flemmli97.tenshilib.common.item.SpawnEgg;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.SpawnEggItem;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(SpawnEggItem.class)
public abstract class SpawnEggMixin {

    @Inject(method = "byId", at = @At("HEAD"))
    private static void onById(@Nullable EntityType<?> type, CallbackInfoReturnable<SpawnEggItem> info) {
        SpawnEgg.resolveEggs();
    }
}
