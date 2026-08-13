package io.github.flemmli97.tenshilib.fabric.client;

import io.github.flemmli97.tenshilib.TenshiLib;
import io.github.flemmli97.tenshilib.client.CustomRiderRendererManager;
import io.github.flemmli97.tenshilib.client.data.GeoAnimationManager;
import io.github.flemmli97.tenshilib.client.data.GeoModelManager;
import io.github.flemmli97.tenshilib.client.particles.ParticleRenderTypes;
import io.github.flemmli97.tenshilib.client.particles.advanced.AdvancedParticleRegistry;
import io.github.flemmli97.tenshilib.client.shader.TenshiLibShaders;
import io.github.flemmli97.tenshilib.common.item.SpawnEgg;
import io.github.flemmli97.tenshilib.fabric.TenshiLibFabric;
import io.github.flemmli97.tenshilib.fabric.client.events.ParticleTypeRegisterEvent;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.ColorProviderRegistry;
import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.FastColor;
import net.minecraft.util.profiling.ProfilerFiller;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

public class TenshiLibFabricClient implements ClientModInitializer, ClientSetupModInitializer {

    @Override
    public void onInitializeClient() {
        TenshiLibFabric.postInit();
        FabricLoader.getInstance().invokeEntrypoints("tenshilib_client", ClientSetupModInitializer.class, ClientSetupModInitializer::clientSetup);
        AdvancedParticleRegistry.verify();
        TenshiLibShaders.registerShader();
    }

    @Override
    public void clientSetup() {
        ParticleTypeRegisterEvent.EVENT.register(register -> register.addRenderType(ParticleRenderTypes.TRANSLUCENT_ADD_BLURRED));
        AdvancedParticleRegistry.init();
        itemColors();
        ResourceManagerHelper.get(PackType.CLIENT_RESOURCES).registerReloadListener(new IdentifiableResourceReloadListener() {
            @Override
            public CompletableFuture<Void> reload(PreparationBarrier preparationBarrier, ResourceManager resourceManager, ProfilerFiller preparationsProfiler, ProfilerFiller reloadProfiler, Executor backgroundExecutor, Executor gameExecutor) {
                return CompletableFuture.allOf(
                        GeoModelManager.getInstance().reload(preparationBarrier, resourceManager, preparationsProfiler, reloadProfiler, backgroundExecutor, gameExecutor),
                        GeoAnimationManager.getInstance().reload(preparationBarrier, resourceManager, preparationsProfiler, reloadProfiler, backgroundExecutor, gameExecutor));
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
    }

    public static void itemColors() {
        for (SpawnEgg egg : SpawnEgg.getEggs())
            ColorProviderRegistry.ITEM.register((stack, i) -> FastColor.ARGB32.opaque(egg.getColor(stack, i)), egg);
    }
}
