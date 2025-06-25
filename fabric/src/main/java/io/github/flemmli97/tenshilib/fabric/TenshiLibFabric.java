package io.github.flemmli97.tenshilib.fabric;

import com.mojang.datafixers.util.Pair;
import io.github.flemmli97.tenshilib.TenshiLib;
import io.github.flemmli97.tenshilib.common.data.AnimationDataManager;
import io.github.flemmli97.tenshilib.common.entity.ai.brain.memory.MoreMemoryModules;
import io.github.flemmli97.tenshilib.common.entity.animated.AnimatedEntity;
import io.github.flemmli97.tenshilib.common.entity.animated.AnimationState;
import io.github.flemmli97.tenshilib.common.item.SpawnEgg;
import io.github.flemmli97.tenshilib.common.network.S2CEntityAnimation;
import io.github.flemmli97.tenshilib.fabric.events.CommonEvents;
import io.github.flemmli97.tenshilib.fabric.events.CommonSetupEvent;
import io.github.flemmli97.tenshilib.fabric.loader.patreon.TenshiLibPatreonImpl;
import io.github.flemmli97.tenshilib.fabric.loader.registry.DeferredRegistrationHandler;
import io.github.flemmli97.tenshilib.fabric.network.PacketHandler;
import io.github.flemmli97.tenshilib.loader.LoaderNetwork;
import net.fabricmc.api.DedicatedServerModInitializer;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.fabricmc.fabric.api.networking.v1.EntityTrackingEvents;
import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.DispenserBlock;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

public class TenshiLibFabric implements ModInitializer, DedicatedServerModInitializer {

    @Override
    public void onInitialize() {
        MoreMemoryModules.MODULES.registerContent();
        UseItemCallback.EVENT.register(CommonEvents::disableOffhand);
        EntityTrackingEvents.START_TRACKING.register(((entity, player) -> {
            if (entity instanceof AnimatedEntity animated && animated.getAnimationHandler().hasAnimation()) {
                AnimationState anim = animated.getAnimationHandler().getAnimation();
                LoaderNetwork.INSTANCE.sendToPlayer(S2CEntityAnimation.create((Entity & AnimatedEntity) entity,
                        anim.getStartTransition(), anim.getEndTransitionTime(), anim.getTick(1)), player);
            }
        }));
        ResourceManagerHelper.get(PackType.SERVER_DATA).registerReloadListener(new IdentifiableResourceReloadListener() {
            @Override
            public ResourceLocation getFabricId() {
                return ResourceLocation.fromNamespaceAndPath(TenshiLib.MODID, "animation_data");
            }

            @Override
            public CompletableFuture<Void> reload(PreparationBarrier preparationBarrier, ResourceManager resourceManager, ProfilerFiller preparationsProfiler, ProfilerFiller reloadProfiler, Executor backgroundExecutor, Executor gameExecutor) {
                return AnimationDataManager.getInstance().reload(preparationBarrier, resourceManager, preparationsProfiler, reloadProfiler, backgroundExecutor, gameExecutor);
            }
        });
        PacketHandler.register();
        TenshiLibPatreonImpl.initPatreonData();
    }

    /**
     * Runs after {@link ModInitializer#onInitialize()}
     */
    public static void postInit() {
        DeferredRegistrationHandler.finalizeRegister();
        for (SpawnEgg egg : SpawnEgg.getEggs())
            DispenserBlock.registerBehavior(egg, egg.dispenser());
        SpawnEgg.resolveEggs();
        List<Pair<String, Runnable>> runnables = new ArrayList<>();
        CommonSetupEvent.COMMON_SETUP.invoker().handle((modid, runnable) -> runnables.add(Pair.of(modid, runnable)));
        runnables.forEach(pair -> {
            try {
                pair.getSecond().run();
            } catch (Exception exception) {
                TenshiLib.LOGGER.error("Error running common setup work for {}", pair.getFirst(), exception);
            }
        });
    }

    @Override
    public void onInitializeServer() {
        postInit();
    }
}
