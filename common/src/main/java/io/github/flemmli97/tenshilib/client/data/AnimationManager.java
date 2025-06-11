package io.github.flemmli97.tenshilib.client.data;

import com.google.gson.JsonElement;
import io.github.flemmli97.tenshilib.TenshiLib;
import io.github.flemmli97.tenshilib.client.model.BedrockAnimations;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;

import java.util.HashMap;
import java.util.Map;

/**
 * Stores BedrockAnimations. The files are read under assets/<modid>/animation/entity and assigned the
 * id modid:filename without ".json"
 * Example: an animation ./assets/foo/animation/entity/bar.json has the id foo:bar
 */
public class AnimationManager extends SimpleJsonResourceReloadListener {

    public static final String DIRECTORY = "animation/entity";

    private static final AnimationManager INSTANCE = new AnimationManager();

    private final Map<ResourceLocation, ReloadableCache<BedrockAnimations>> animations = new HashMap<>();

    private AnimationManager() {
        super(BedrockAnimations.GSON, DIRECTORY);
    }

    public static AnimationManager getInstance() {
        return INSTANCE;
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> object, ResourceManager resourceManager, ProfilerFiller profiler) {
        object.forEach((res, json) -> {
            try {
                ResourceLocation id = ResourceLocation.fromNamespaceAndPath(res.getNamespace(), res.getPath());
                BedrockAnimations read = BedrockAnimations.GSON.fromJson(json, BedrockAnimations.class);
                this.getAnimation(id).update(read);
            } catch (Exception e) {
                TenshiLib.LOGGER.error("Unable to parse animation file {}", res, e);
            }
        });
    }

    public ReloadableCache<BedrockAnimations> getAnimation(ResourceLocation res) {
        return this.animations.computeIfAbsent(res, r -> new ReloadableCache<>());
    }
}
