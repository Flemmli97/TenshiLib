package io.github.flemmli97.tenshilib.fabric.mixin;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Map;
import java.util.function.Supplier;

@Mixin(BuiltInRegistries.class)
public interface BuiltinRegistryAccessor {

    @Accessor("LOADERS")
    static Map<ResourceLocation, Supplier<?>> getLoaders() {
        throw new AssertionError();
    }
}
