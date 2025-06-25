package io.github.flemmli97.tenshilib.neoforge.loader;

import io.github.flemmli97.tenshilib.loader.LoaderRegistryAccess;
import io.github.flemmli97.tenshilib.loader.registry.LoaderRegister;
import io.github.flemmli97.tenshilib.neoforge.loader.registry.DeferredRegisterHandler;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.registries.DeferredRegister;

public class LoaderRegistryAccessImpl implements LoaderRegistryAccess {

    @Override
    public <T> LoaderRegister<T> of(ResourceKey<? extends Registry<T>> key, String modid) {
        return new DeferredRegisterHandler<>(DeferredRegister.create(key, modid));
    }

    @Override
    public <T> CustomLoaderRegistry<T> newRegistry(ResourceKey<? extends Registry<T>> registryKey, ResourceLocation defaultVal, boolean saveToDisk, boolean sync) {
        DeferredRegister<T> r = DeferredRegister.create(registryKey, registryKey.location().getNamespace());
        return new CustomLoaderRegistry<>(new DeferredRegisterHandler<>(r), r.makeRegistry(b -> b.defaultKey(defaultVal).sync(sync)));
    }
}
