package io.github.flemmli97.tenshilib.fabric.client;

import io.github.flemmli97.tenshilib.client.AnimationManager;
import io.github.flemmli97.tenshilib.fabric.client.events.ClientEvents;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.ResourceManager;

public class TenshiLibFabricClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ClientPacketHandler.register();
        ClientEvents.itemColors();
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
