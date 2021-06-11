package com.flemmli97.tenshilib.mixin;

import net.minecraft.command.ISuggestionProvider;
import net.minecraft.util.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.function.Consumer;
import java.util.function.Function;

@Mixin(ISuggestionProvider.class)
public abstract class ISuggestionProviderMixin {

    @Inject(method = "func_210512_a", at = @At(value = "INVOKE"), cancellable = true, locals = LocalCapture.CAPTURE_FAILHARD)
    static <T> void autocomplete(Iterable<T> registry, String str, Function<T, ResourceLocation> func, Consumer<T> cons, CallbackInfoReturnable<T> info, boolean flag, T t, ResourceLocation resourceLocation) {
        if (ISuggestionProvider.func_237256_a_(str, resourceLocation.getPath())) {
            cons.accept(t);
            info.cancel();
        }
    }
}
