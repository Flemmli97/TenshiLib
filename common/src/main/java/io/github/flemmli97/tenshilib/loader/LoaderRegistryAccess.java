package io.github.flemmli97.tenshilib.loader;

import io.github.flemmli97.tenshilib.loader.registry.LoaderRegistry;
import io.github.flemmli97.tenshilib.loader.registry.VanillaRegistryHandler;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;

import java.util.function.Consumer;

public abstract class LoaderRegistryAccess {

    public static final LoaderRegistryAccess INSTANCE = LoaderInitializer.getImplInstance(LoaderRegistryAccess.class,
            "io.github.flemmli97.tenshilib.fabric.loader.LoaderRegistryAccessImpl",
            "io.github.flemmli97.tenshilib.neoforge.loader.LoaderRegistryAccessImpl");

    /**
     * Creates a registry handler for the matching key.
     * The registry needs to exist for the key else an exeption will be thrown.
     */
    public <T> LoaderRegistry<T> of(ResourceKey<? extends Registry<T>> key, String modid) {
        return new VanillaRegistryHandler<>(key, modid);
    }

    /**
     * Obtains a custom registry.
     * Note on fabric: Since there is no loading order be careful of calling this. The registry might not have been created yet
     * In most cases this shouldn't be used
     */
    public abstract <T> LoaderRegistry<T> customRegistry(ResourceKey<? extends Registry<T>> registryKey, String modid);

    /**
     * Creates a custom registry.
     * On fabric the registry is created immediately
     * On (neo)forge the registry is created on RegistryEvent.NewRegistry
     *
     * @param registryRef A callback to the newly created Registry
     */
    public abstract <T> LoaderRegistry<T> newRegistry(ResourceKey<? extends Registry<T>> registryKey, ResourceLocation defaultVal, boolean saveToDisk, boolean sync, Consumer<Registry<T>> registryRef);

}