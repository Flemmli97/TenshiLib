package io.github.flemmli97.tenshilib.fabric.platform;

import com.mojang.serialization.Lifecycle;
import io.github.flemmli97.tenshilib.fabric.events.AOEAttackEvent;
import io.github.flemmli97.tenshilib.platform.EventCalls;
import io.github.flemmli97.tenshilib.platform.PlatformUtils;
import io.github.flemmli97.tenshilib.platform.registry.PlatformRegistry;
import io.github.flemmli97.tenshilib.platform.registry.VanillaRegistryHandler;
import net.fabricmc.fabric.api.event.registry.FabricRegistryBuilder;
import net.fabricmc.fabric.api.event.registry.RegistryAttribute;
import net.minecraft.core.DefaultedMappedRegistry;
import net.minecraft.core.Registry;
import net.minecraft.core.WritableRegistry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.List;
import java.util.function.Consumer;

public class PlatformUtilsImpl extends PlatformUtils {

    @Override
    public <T> PlatformRegistry<T> customRegistry(ResourceKey<? extends Registry<T>> registryKey, String modid) {
        return new VanillaRegistryHandler<>(registryKey, modid);
    }

    @Override
    public <T> PlatformRegistry<T> newRegistry(ResourceKey<? extends Registry<T>> registryKey, ResourceLocation defaultVal, boolean saveToDisk, boolean sync,
                                               Consumer<Registry<T>> registryRef) {
        FabricRegistryBuilder<T, WritableRegistry<T>> builder = FabricRegistryBuilder.from(new DefaultedMappedRegistry<>(defaultVal.toString(), registryKey, Lifecycle.stable(), false));
        if (saveToDisk)
            builder.attribute(RegistryAttribute.SYNCED);
        if (sync)
            builder.attribute(RegistryAttribute.SYNCED);
        registryRef.accept(builder.buildAndRegister());
        return new VanillaRegistryHandler<>(registryKey, registryKey.location().getNamespace());
    }

    @Override
    public void registerAOEEventHandler(EventCalls.Func3<Player, ItemStack, List<Entity>, Boolean> func) {
        AOEAttackEvent.ATTACK.register(func::apply);
    }
}
