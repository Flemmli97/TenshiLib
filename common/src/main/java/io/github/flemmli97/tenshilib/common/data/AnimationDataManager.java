package io.github.flemmli97.tenshilib.common.data;

import com.google.common.collect.ImmutableMap;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.mojang.serialization.JsonOps;
import io.github.flemmli97.tenshilib.TenshiLib;
import io.github.flemmli97.tenshilib.common.entity.animated.AnimationDefinitionContainer;
import io.github.flemmli97.tenshilib.common.entity.animated.AnimationsBuilder;
import io.github.flemmli97.tenshilib.common.entity.animated.DefaultedAnimationContainer;
import io.github.flemmli97.tenshilib.common.network.S2CAnimationDataPacket;
import io.github.flemmli97.tenshilib.loader.LoaderNetwork;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.entity.EntityType;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class AnimationDataManager extends SimpleJsonResourceReloadListener implements SyncableReloadListener {

    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(TenshiLib.MODID, "animation_definitions");
    public static final String DIRECTORY = String.format("%s/%s", ID.getNamespace(), ID.getPath());

    private static final AnimationDataManager INSTANCE = createInstance();

    private Map<ResourceLocation, AnimationDefinitionContainer> animations = new HashMap<>();

    private AnimationDataManager() {
        super(new Gson(), DIRECTORY);
    }

    private static AnimationDataManager createInstance() {
        AnimationDataManager inst = new AnimationDataManager();
        SyncedReloadListeners.addOrUpdate(AnimationDataManager.ID, inst);
        return inst;
    }

    public static AnimationDataManager getInstance() {
        return INSTANCE;
    }

    @Override
    public void onSync(Collection<ServerPlayer> players) {
        LoaderNetwork.INSTANCE.sendToAll(new S2CAnimationDataPacket(this.animations), players);
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> object, ResourceManager resourceManager, ProfilerFiller profiler) {
        ImmutableMap.Builder<ResourceLocation, AnimationDefinitionContainer> builder = new ImmutableMap.Builder<>();
        object.forEach((res, json) -> {
            try {
                builder.put(res, AnimationsBuilder.CODEC.parse(JsonOps.INSTANCE, json).getOrThrow().build());
            } catch (Exception e) {
                TenshiLib.LOGGER.error("Unable to parse animation file {}", res, e);
            }
        });
        this.animations = builder.build();
    }

    public AnimationDefinitionContainer getAnimation(EntityType<?> type, AnimationDefinitionContainer defaulted) {
        return this.getAnimation(BuiltInRegistries.ENTITY_TYPE.getKey(type), defaulted);
    }

    @Nullable
    public AnimationDefinitionContainer getAnimation(ResourceLocation res, AnimationDefinitionContainer defaulted) {
        AnimationDefinitionContainer container = this.getAnimation(res);
        if (container == null)
            return defaulted;
        return new DefaultedAnimationContainer(defaulted, container);
    }

    public AnimationDefinitionContainer getAnimation(EntityType<?> type) {
        return this.getAnimation(BuiltInRegistries.ENTITY_TYPE.getKey(type));
    }

    @Nullable
    public AnimationDefinitionContainer getAnimation(ResourceLocation res) {
        return this.animations.get(res);
    }

    public void updateFrom(S2CAnimationDataPacket pkt) {
        this.animations = Map.copyOf(pkt.content());
    }
}
