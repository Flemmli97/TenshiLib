package io.github.flemmli97.tenshilib.fabric.loader;

import com.mojang.serialization.Lifecycle;
import io.github.flemmli97.tenshilib.fabric.loader.registry.VanillaRegisterHandler;
import io.github.flemmli97.tenshilib.loader.LoaderRegistryAccess;
import io.github.flemmli97.tenshilib.loader.registry.LoaderRegister;
import net.fabricmc.fabric.api.event.registry.FabricRegistryBuilder;
import net.fabricmc.fabric.api.event.registry.RegistryAttribute;
import net.minecraft.core.DefaultedMappedRegistry;
import net.minecraft.core.Registry;
import net.minecraft.core.WritableRegistry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;

public class LoaderRegistryAccessImpl implements LoaderRegistryAccess {

    @Override
    public <T> LoaderRegister<T> of(ResourceKey<? extends Registry<T>> key, String modid) {
        return new VanillaRegisterHandler<>(key, modid);
    }

    @Override
    public <T> CustomLoaderRegistry<T> newRegistry(ResourceKey<? extends Registry<T>> registryKey, ResourceLocation defaultVal, boolean saveToDisk, boolean sync) {
        FabricRegistryBuilder<T, WritableRegistry<T>> builder = FabricRegistryBuilder.from(new DefaultedMappedRegistry<>(defaultVal.toString(), registryKey, Lifecycle.stable(), false));
        if (saveToDisk)
            builder.attribute(RegistryAttribute.SYNCED);
        if (sync)
            builder.attribute(RegistryAttribute.SYNCED);
        return new CustomLoaderRegistry<>(new VanillaRegisterHandler<>(registryKey, registryKey.location().getNamespace()), builder.buildAndRegister());
    }
}
