package io.github.flemmli97.tenshilib.client.data;

import com.google.gson.JsonElement;
import io.github.flemmli97.tenshilib.TenshiLib;
import io.github.flemmli97.tenshilib.client.model.BedrockAnimations;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

/**
 * Stores BedrockAnimations.
 * The files are read under assets/<modid>/tenshilib/animations
 */
public class GeoAnimationManager extends SimpleJsonResourceReloadListener {

    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(TenshiLib.MODID, "animations");
    public static final String DIRECTORY = String.format("%s/%s", ID.getNamespace(), ID.getPath());

    private static final GeoAnimationManager INSTANCE = new GeoAnimationManager();

    private final Map<ResourceLocation, ReloadableCache<BedrockAnimations>> animations = new HashMap<>();
    private final Set<ResourceLocation> optional = new HashSet<>();
    private boolean reloaded;

    private GeoAnimationManager() {
        super(BedrockAnimations.GSON, DIRECTORY);
    }

    public static GeoAnimationManager getInstance() {
        return INSTANCE;
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> map, ResourceManager resourceManager, ProfilerFiller profiler) {
        Set<ResourceLocation> present = new HashSet<>();
        map.forEach((id, json) -> {
            try {
                BedrockAnimations read = BedrockAnimations.GSON.fromJson(json, BedrockAnimations.class);
                this.getAnimation(id).update(read);
                present.add(id);
            } catch (Exception e) {
                TenshiLib.LOGGER.error("Unable to parse animation file {}", id, e);
            }
        });
        this.reloaded = true;
        List<ResourceLocation> missing = new ArrayList<>();
        List<ResourceLocation> optionalMissing = new ArrayList<>();
        this.animations.keySet().forEach(id -> {
            if (!present.contains(id)) {
                if (!this.optional.contains(id)) {
                    missing.add(id);
                } else {
                    optionalMissing.add(id);
                }
            }
        });
        if (!missing.isEmpty()) {
            throw new IllegalStateException("Following animations could not be found: " + missing);
        }
        if (!optionalMissing.isEmpty()) {
            TenshiLib.LOGGER.error("Following optional animations could not be found: {}", optionalMissing);
        }
    }

    public ReloadableCache<BedrockAnimations> getOptionalAnimation(ResourceLocation id) {
        this.optional.add(id);
        return this.tryGetAnimation(id, cache -> cache.update(BedrockAnimations.empty()));
    }

    public ReloadableCache<BedrockAnimations> getAnimation(ResourceLocation id) {
        return this.tryGetAnimation(id, null);
    }

    private ReloadableCache<BedrockAnimations> tryGetAnimation(ResourceLocation id, @Nullable Consumer<ReloadableCache<BedrockAnimations>> init) {
        return this.animations.computeIfAbsent(id, r -> {
            ReloadableCache<BedrockAnimations> cache = new ReloadableCache<>();
            if (init != null) {
                init.accept(cache);
            }
            if (this.reloaded) {
                if (cache.get() == null) {
                    throw new IllegalStateException("Animation is not present! " + r);
                } else {
                    TenshiLib.LOGGER.error("Animation {} is not present! Returned result will be empty!", r);
                }
            }
            return cache;
        });
    }
}
