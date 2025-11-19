package io.github.flemmli97.tenshilib.fabric;

import com.mojang.datafixers.util.Pair;
import io.github.flemmli97.tenshilib.TenshiLib;
import io.github.flemmli97.tenshilib.common.data.AnimationDataManager;
import io.github.flemmli97.tenshilib.common.effect.ExtendedMobEffect;
import io.github.flemmli97.tenshilib.common.entity.animated.AnimatedEntity;
import io.github.flemmli97.tenshilib.common.entity.animated.AnimationState;
import io.github.flemmli97.tenshilib.common.entity.data.SyncedMobDataHandler;
import io.github.flemmli97.tenshilib.common.item.SpawnEgg;
import io.github.flemmli97.tenshilib.common.network.S2CEntityAnimation;
import io.github.flemmli97.tenshilib.fabric.attachment.AttachmentHandler;
import io.github.flemmli97.tenshilib.fabric.events.CommonEvents;
import io.github.flemmli97.tenshilib.fabric.events.EntityStartTrackEvent;
import io.github.flemmli97.tenshilib.fabric.loader.TenshiLibCrossPlatImpl;
import io.github.flemmli97.tenshilib.fabric.loader.events.CommonSetupEvent;
import io.github.flemmli97.tenshilib.fabric.loader.events.EntityAttributeModifierEvent;
import io.github.flemmli97.tenshilib.fabric.loader.patreon.TenshiLibPatreonImpl;
import io.github.flemmli97.tenshilib.fabric.loader.registry.AttachmentRegisterImpl;
import io.github.flemmli97.tenshilib.fabric.loader.registry.DeferredRegistrationHandler;
import io.github.flemmli97.tenshilib.fabric.mixin.DefaultAttributeSupplierAccessor;
import io.github.flemmli97.tenshilib.fabric.network.PacketHandler;
import io.github.flemmli97.tenshilib.loader.LoaderNetwork;
import net.fabricmc.api.DedicatedServerModInitializer;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.entity.event.v1.ServerEntityWorldChangeEvents;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.protocol.game.ClientboundUpdateMobEffectPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.DispenserBlock;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

public class TenshiLibFabric implements ModInitializer, DedicatedServerModInitializer {

    @Override
    public void onInitialize() {
        AttachmentRegisterImpl.init();
        TenshiLib.registerRegistry();
        UseItemCallback.EVENT.register(CommonEvents::disableOffhand);
        EntityStartTrackEvent.START_TRACKING.register(((entity, player) -> {
            if (entity instanceof AnimatedEntity animated && animated.getAnimationHandler().hasAnimation()) {
                AnimationState anim = animated.getAnimationHandler().getAnimation();
                LoaderNetwork.INSTANCE.sendToPlayer(S2CEntityAnimation.create((Entity & AnimatedEntity) entity, anim.getTick(1)), player);
            }
            if (entity instanceof SyncedMobDataHandler handler) {
                handler.getDataContainer().sendEntriesTo(player);
            }
            if (!(entity instanceof Player) && entity instanceof LivingEntity living) {
                for (MobEffectInstance instance : living.getActiveEffects()) {
                    if (instance.getEffect().value() instanceof ExtendedMobEffect ext && ext.shouldSync())
                        player.connection.send(new ClientboundUpdateMobEffectPacket(living.getId(), instance, false));
                }
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
        ServerLifecycleEvents.SERVER_STARTED.register(server -> TenshiLibCrossPlatImpl.CURRENT_SERVER = server);
        ServerLifecycleEvents.SERVER_STOPPED.register(server -> TenshiLibCrossPlatImpl.CURRENT_SERVER = null);

        PacketHandler.register();
        TenshiLibPatreonImpl.initPatreonData();

        ServerPlayerEvents.AFTER_RESPAWN.register((oldPlayer, newPlayer, alive) ->
                AttachmentHandler.copyAttachments(oldPlayer, newPlayer, !alive)
        );
        ServerEntityWorldChangeEvents.AFTER_ENTITY_CHANGE_WORLD.register(((originalEntity, newEntity, origin, destination) ->
                AttachmentHandler.copyAttachments(originalEntity, newEntity, false))
        );
        ServerLivingEntityEvents.MOB_CONVERSION.register((previous, converted, keepEquipment) ->
                AttachmentHandler.copyAttachments(previous, converted, true)
        );
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
        CommonSetupEvent.EVENT.invoker().handle((modid, runnable) -> runnables.add(Pair.of(modid, runnable)));
        runnables.forEach(pair -> {
            try {
                pair.getSecond().run();
            } catch (Exception exception) {
                TenshiLib.LOGGER.error("Error running common setup work for {}", pair.getFirst(), exception);
            }
        });
        modifyEntityAttributes();
    }

    private static void modifyEntityAttributes() {
        EntityAttributeModifierEvent.AttributeModifications modifications = new EntityAttributeModifierEvent.AttributeModifications(DefaultAttributeSupplierAccessor.getDefaults());
        EntityAttributeModifierEvent.EVENT.invoker().call(modifications);
        modifications.view().forEach((type, contents) -> {
            AttributeSupplier sup = DefaultAttributeSupplierAccessor.getDefaults()
                    .get(type);
            if (sup == null) {
                TenshiLib.LOGGER.error("Tried modifying atttributes of an entity {} that was not registered!", BuiltInRegistries.ENTITY_TYPE.getKey(type));
            } else {
                // Self note: Map is modifiable cause fabric changes it to an IdentityHashMap
                DefaultAttributeSupplierAccessor.getDefaults()
                        .put(type, ((EntityAttributeModifierEvent.AttributeSupplierMerger) sup).tenshilib$mergeWith(contents));
            }
        });
    }

    @Override
    public void onInitializeServer() {
        postInit();
    }
}
