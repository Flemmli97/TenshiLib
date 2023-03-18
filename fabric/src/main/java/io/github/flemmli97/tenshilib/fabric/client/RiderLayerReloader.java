package io.github.flemmli97.tenshilib.fabric.client;

import io.github.flemmli97.tenshilib.TenshiLib;
import io.github.flemmli97.tenshilib.client.CustomRiderRendererManager;
import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;

public class RiderLayerReloader implements ResourceManagerReloadListener, IdentifiableResourceReloadListener {

    @Override
    public void onResourceManagerReload(ResourceManager resourceManager) {
        CustomRiderRendererManager.getInstance().onResourceManagerReload(resourceManager);
    }

    @Override
    public ResourceLocation getFabricId() {
        return new ResourceLocation(TenshiLib.MODID, "rider_layer_manager");
    }
}
