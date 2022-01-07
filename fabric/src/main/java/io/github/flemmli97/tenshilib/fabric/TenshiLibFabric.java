package io.github.flemmli97.tenshilib.fabric;

import io.github.flemmli97.tenshilib.client.AnimationManager;
import io.github.flemmli97.tenshilib.common.item.SpawnEgg;
import io.github.flemmli97.tenshilib.fabric.client.events.ClientEvents;
import io.github.flemmli97.tenshilib.fabric.events.CommonEvents;
import io.github.flemmli97.tenshilib.fabric.network.PacketHandler;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.player.AttackBlockCallback;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.level.block.DispenserBlock;

public class TenshiLibFabric implements ModInitializer {

    @Override
    public void onInitialize() {
        AttackBlockCallback.EVENT.register(CommonEvents::leftClickBlock);
        UseItemCallback.EVENT.register(CommonEvents::disableOffhand);
        PacketHandler.register();
        if (FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT) {
            ClientEvents.itemColors();
        }
        for (SpawnEgg egg : SpawnEgg.getEggs())
            DispenserBlock.registerBehavior(egg, egg.dispenser());
        if (!FabricLoader.getInstance().isDevelopmentEnvironment()) //For some reason this doesnt get remapped in dev and thus crashes
            ResourceManagerHelper.get(PackType.CLIENT_RESOURCES).registerReloadListener(new SimpleSynchronousResourceReloadListener() {

                @Override
                public void onResourceManagerReload(ResourceManager resourceManager) {
                    AnimationManager.getInstance().onResourceManagerReload(resourceManager);
                }

                @Override
                public ResourceLocation getFabricId() {
                    return new ResourceLocation("tenshilib", "entity_animations");
                }
            });
    }
}
