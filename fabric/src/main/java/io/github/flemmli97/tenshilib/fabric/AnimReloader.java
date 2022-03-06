package io.github.flemmli97.tenshilib.fabric;

import io.github.flemmli97.tenshilib.TenshiLib;
import io.github.flemmli97.tenshilib.client.AnimationManager;
import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;

/**
 * Cant use an anonymous class in ResourceManagerHelper#registerReloadListener
 * case for some reason this then doesnt get remapped in dev and thus crashes.
 */
public class AnimReloader implements ResourceManagerReloadListener, IdentifiableResourceReloadListener {

    @Override
    public void onResourceManagerReload(ResourceManager resourceManager) {
        AnimationManager.getInstance().onResourceManagerReload(resourceManager);
    }

    @Override
    public ResourceLocation getFabricId() {
        return new ResourceLocation(TenshiLib.MODID, "entity_animations");
    }
}
