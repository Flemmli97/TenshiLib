package io.github.flemmli97.tenshilib.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import io.github.flemmli97.tenshilib.loader.event.TagModifyEvent;
import io.github.flemmli97.tenshilib.mixinhelper.TagLoaderRegistryContext;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagLoader;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

import java.util.Collection;
import java.util.Map;

@Mixin(TagLoader.class)
public class TagLoaderMixin<T> implements TagLoaderRegistryContext<T> {

    @Unique
    private ResourceKey<? extends Registry<T>> tenshilib$Registry;
    @Unique
    private RegistryAccess tenshilib$RegistryAccess;

    @Override
    public void tenshilib$setRegistry(ResourceKey<? extends Registry<T>> key, RegistryAccess registryAccess) {
        this.tenshilib$Registry = key;
        this.tenshilib$RegistryAccess = registryAccess;
    }

    @SuppressWarnings("unchecked")
    @ModifyReturnValue(method = "build(Ljava/util/Map;)Ljava/util/Map;", at = @At("RETURN"))
    private Map<ResourceLocation, Collection<?>> onBuild(Map<ResourceLocation, Collection<?>> original) {
        if (this.tenshilib$Registry != null) {
            TagModifyEvent.INSTANCE.trigger(this.tenshilib$Registry, this.tenshilib$RegistryAccess, (Map<ResourceLocation, Collection<Holder<T>>>) (Object) original);
        }
        return original;
    }
}
