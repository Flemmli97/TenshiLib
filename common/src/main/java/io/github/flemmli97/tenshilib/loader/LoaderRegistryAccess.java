package io.github.flemmli97.tenshilib.loader;

import io.github.flemmli97.tenshilib.loader.registry.LoaderRegister;
import io.github.flemmli97.tenshilib.loader.registry.VanillaRegisterHandler;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;

/**
 * Loader agnostic registry interface
 */
public abstract class LoaderRegistryAccess {

    public static final LoaderRegistryAccess INSTANCE = LoaderInitializer.getImplInstance(LoaderRegistryAccess.class,
            "io.github.flemmli97.tenshilib.fabric.loader.LoaderRegistryAccessImpl",
            "io.github.flemmli97.tenshilib.neoforge.loader.LoaderRegistryAccessImpl");

    /**
     * Creates a registry handler for the matching key.
     * The registry needs to exist for the key else an exeption will be thrown.
     */
    public <T> LoaderRegister<T> of(ResourceKey<? extends Registry<T>> key, String modid) {
        return new VanillaRegisterHandler<>(key, modid);
    }

    /**
     * Obtains a custom registry.
     * Note on fabric: Since there is no loading order be careful of calling this. The registry might not have been created yet
     * In most cases this shouldn't be used
     */
    public abstract <T> LoaderRegister<T> customRegistry(ResourceKey<? extends Registry<T>> registryKey, String modid);

    /**
     * Creates a new custom registry.
     * On fabric the registry is created immediately
     * On (neo)forge the registry is created on RegistryEvent.NewRegistry
     */
    public abstract <T> CustomLoaderRegistry<T> newRegistry(ResourceKey<? extends Registry<T>> registryKey, ResourceLocation defaultVal, boolean saveToDisk, boolean sync);

    /**
     * A pair of a {@link LoaderRegister} used to register new content and the underlying {@link Registry}
     * This is used for custom registry.
     *
     * @param register Register to register new content with
     * @param registry The actual registry to access registered content with
     */
    public record CustomLoaderRegistry<T>(LoaderRegister<T> register, Registry<T> registry) {
    }
}