package io.github.flemmli97.tenshilib.fabric.client;

import io.github.flemmli97.tenshilib.TenshiLib;
import io.github.flemmli97.tenshilib.client.CustomRiderRendererManager;
import io.github.flemmli97.tenshilib.client.TenshilibShaders;
import io.github.flemmli97.tenshilib.client.data.AnimationManager;
import io.github.flemmli97.tenshilib.client.data.ModelManager;
import io.github.flemmli97.tenshilib.fabric.client.events.ClientEvents;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.CoreShaderRegistrationCallback;
import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

public class TenshiLibFabricClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        ClientEvents.itemColors();
        ResourceManagerHelper.get(PackType.CLIENT_RESOURCES).registerReloadListener(new IdentifiableResourceReloadListener() {

            @Override
            public CompletableFuture<Void> reload(PreparationBarrier preparationBarrier, ResourceManager resourceManager, ProfilerFiller preparationsProfiler, ProfilerFiller reloadProfiler, Executor backgroundExecutor, Executor gameExecutor) {
                return CompletableFuture.allOf(
                        ModelManager.getInstance().reload(preparationBarrier, resourceManager, preparationsProfiler, reloadProfiler, backgroundExecutor, gameExecutor),
                        AnimationManager.getInstance().reload(preparationBarrier, resourceManager, preparationsProfiler, reloadProfiler, backgroundExecutor, gameExecutor));
            }

            @Override
            public ResourceLocation getFabricId() {
                return ResourceLocation.fromNamespaceAndPath(TenshiLib.MODID, "model_assets");
            }
        });
        ResourceManagerHelper.get(PackType.CLIENT_RESOURCES).registerReloadListener(new IdentifiableResourceReloadListener() {

            @Override
            public CompletableFuture<Void> reload(PreparationBarrier preparationBarrier, ResourceManager resourceManager, ProfilerFiller preparationsProfiler, ProfilerFiller reloadProfiler, Executor backgroundExecutor, Executor gameExecutor) {
                return CustomRiderRendererManager.getInstance().reload(preparationBarrier, resourceManager, preparationsProfiler, reloadProfiler, backgroundExecutor, gameExecutor);
            }

            @Override
            public ResourceLocation getFabricId() {
                return ResourceLocation.fromNamespaceAndPath(TenshiLib.MODID, "rider_layer_manager");
            }
        });
        CoreShaderRegistrationCallback.EVENT.register(reg -> TenshilibShaders.registerShader(reg::register));
    }
}
