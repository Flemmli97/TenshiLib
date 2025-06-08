package io.github.flemmli97.tenshilib.fabric.loader;

import com.mojang.serialization.Lifecycle;
import io.github.flemmli97.tenshilib.loader.LoaderRegistryAccess;
import io.github.flemmli97.tenshilib.loader.registry.LoaderRegistry;
import io.github.flemmli97.tenshilib.loader.registry.VanillaRegistryHandler;
import net.fabricmc.fabric.api.event.registry.FabricRegistryBuilder;
import net.fabricmc.fabric.api.event.registry.RegistryAttribute;
import net.minecraft.core.DefaultedMappedRegistry;
import net.minecraft.core.Registry;
import net.minecraft.core.WritableRegistry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;

import java.util.function.Consumer;

public class LoaderRegistryAccessImpl extends LoaderRegistryAccess {

    @Override
    public <T> LoaderRegistry<T> customRegistry(ResourceKey<? extends Registry<T>> registryKey, String modid) {
        return new VanillaRegistryHandler<>(registryKey, modid);
    }

    @Override
    public <T> LoaderRegistry<T> newRegistry(ResourceKey<? extends Registry<T>> registryKey, ResourceLocation defaultVal, boolean saveToDisk, boolean sync,
                                             Consumer<Registry<T>> registryRef) {
        FabricRegistryBuilder<T, WritableRegistry<T>> builder = FabricRegistryBuilder.from(new DefaultedMappedRegistry<>(defaultVal.toString(), registryKey, Lifecycle.stable(), false));
        if (saveToDisk)
            builder.attribute(RegistryAttribute.SYNCED);
        if (sync)
            builder.attribute(RegistryAttribute.SYNCED);
        registryRef.accept(builder.buildAndRegister());
        return new VanillaRegistryHandler<>(registryKey, registryKey.location().getNamespace());
    }
}
