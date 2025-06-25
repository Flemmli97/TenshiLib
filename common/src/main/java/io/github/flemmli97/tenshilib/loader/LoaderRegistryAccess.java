package io.github.flemmli97.tenshilib.loader;

import io.github.flemmli97.tenshilib.loader.registry.LoaderRegister;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;

/**
 * Loader agnostic registry interface
 */
public interface LoaderRegistryAccess {

    LoaderRegistryAccess INSTANCE = LoaderInitializer.getImplInstance(LoaderRegistryAccess.class,
            "io.github.flemmli97.tenshilib.fabric.loader.LoaderRegistryAccessImpl",
            "io.github.flemmli97.tenshilib.neoforge.loader.LoaderRegistryAccessImpl");

    /**
     * Creates a registry handler for the matching key.
     */
    <T> LoaderRegister<T> of(ResourceKey<? extends Registry<T>> key, String modid);

    /**
     * Creates a new custom registry.
     * On fabric the registry is created immediately
     * On (neo)forge the registry is created on RegistryEvent.NewRegistry
     */
    <T> CustomLoaderRegistry<T> newRegistry(ResourceKey<? extends Registry<T>> registryKey, ResourceLocation defaultVal, boolean saveToDisk, boolean sync);

    /**
     * A pair of a {@link LoaderRegister} used to register new content and the underlying {@link Registry}
     * This is used for custom registry.
     *
     * @param register Register to register new content with
     * @param registry The actual registry to access registered content with
     */
    record CustomLoaderRegistry<T>(LoaderRegister<T> register, Registry<T> registry) {

    }
}