package io.github.flemmli97.tenshilib.neoforge.loader;

import io.github.flemmli97.tenshilib.loader.LoaderRegistryAccess;
import io.github.flemmli97.tenshilib.loader.registry.LoaderRegistry;
import io.github.flemmli97.tenshilib.neoforge.loader.registry.DeferredRegistryHandler;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Consumer;

public class LoaderRegistryAccessImpl extends LoaderRegistryAccess {

    @Override
    public <T> LoaderRegistry<T> of(ResourceKey<? extends Registry<T>> key, String modid) {
        return new DeferredRegistryHandler<>(DeferredRegister.create(key, modid));
    }

    @Override
    public <T> LoaderRegistry<T> customRegistry(ResourceKey<? extends Registry<T>> registryKey, String modid) {
        return new DeferredRegistryHandler<>(DeferredRegister.create(registryKey, modid));
    }

    @Override
    public <T> LoaderRegistry<T> newRegistry(ResourceKey<? extends Registry<T>> registryKey, ResourceLocation defaultVal, boolean saveToDisk, boolean sync,
                                             Consumer<Registry<T>> registryRef) {
        DeferredRegister<T> r = DeferredRegister.create(registryKey, registryKey.location().getNamespace());
        registryRef.accept(r.makeRegistry(b -> b.defaultKey(defaultVal).sync(sync)));
        return new DeferredRegistryHandler<>(r);
    }
}
