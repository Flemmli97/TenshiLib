package io.github.flemmli97.tenshilib.forge.platform;

import io.github.flemmli97.tenshilib.forge.events.AOEAttackEvent;
import io.github.flemmli97.tenshilib.forge.platform.registry.ForgeRegistryHandler;
import io.github.flemmli97.tenshilib.platform.EventCalls;
import io.github.flemmli97.tenshilib.platform.PlatformUtils;
import io.github.flemmli97.tenshilib.platform.registry.PlatformRegistry;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.List;
import java.util.function.Consumer;

public class PlatformUtilsImpl extends PlatformUtils {

    @Override
    public <T> PlatformRegistry<T> of(ResourceKey<? extends Registry<T>> key, String modid) {
        return new ForgeRegistryHandler<>(DeferredRegister.create(key, modid));
    }

    @Override
    public <T> PlatformRegistry<T> customRegistry(ResourceKey<? extends Registry<T>> registryKey, String modid) {
        return new ForgeRegistryHandler<>(DeferredRegister.create(registryKey, modid));
    }

    @Override
    public <T> PlatformRegistry<T> customRegistry(ResourceKey<? extends Registry<T>> registryKey, ResourceLocation defaultVal, boolean saveToDisk, boolean sync) {
        DeferredRegister<T> r = DeferredRegister.create(registryKey, registryKey.location().getNamespace());
        r.makeRegistry(b -> b.defaultKey(defaultVal).sync(sync));
        return new ForgeRegistryHandler<>(r);
    }

    @Override
    public void registerAOEEventHandler(EventCalls.Func3<Player, ItemStack, List<Entity>, Boolean> func) {
        Consumer<AOEAttackEvent> cons = event -> {
            if (!func.apply(event.getEntity(), event.usedItem, event.attackList()))
                event.setCanceled(true);
        };
        NeoForge.EVENT_BUS.addListener(cons);
    }
}
