package io.github.flemmli97.tenshilib.common.data.provider;

import com.google.common.collect.ImmutableMap;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.mojang.serialization.JsonOps;
import io.github.flemmli97.tenshilib.TenshiLib;
import io.github.flemmli97.tenshilib.common.entity.animated.AnimationDefinitionContainer;
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
import java.util.Map;

public class AnimationDataProvider extends SimpleJsonResourceReloadListener {

    public static final String DIRECTORY = "animation/data";

    private static final AnimationDataProvider INSTANCE = new AnimationDataProvider();

    private Map<ResourceLocation, AnimationDefinitionContainer> animations;

    private AnimationDataProvider() {
        super(new Gson(), DIRECTORY);
    }

    public static AnimationDataProvider getInstance() {
        return INSTANCE;
    }

    public void syncTo(Collection<ServerPlayer> server) {
        LoaderNetwork.INSTANCE.sendToAll(new S2CAnimationDataPacket(this.animations), server);
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> object, ResourceManager resourceManager, ProfilerFiller profiler) {
        ImmutableMap.Builder<ResourceLocation, AnimationDefinitionContainer> builder = new ImmutableMap.Builder<>();
        object.forEach((res, json) -> {
            try {
                builder.put(res, AnimationDefinitionContainer.CODEC.parse(JsonOps.INSTANCE, json).getOrThrow());
            } catch (Exception e) {
                TenshiLib.LOGGER.error("Unable to parse animation file {}", res, e);
            }
        });
        this.animations = builder.build();
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
