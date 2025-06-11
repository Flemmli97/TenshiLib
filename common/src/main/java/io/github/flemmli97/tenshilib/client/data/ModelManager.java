package io.github.flemmli97.tenshilib.client.data;

import com.google.gson.JsonElement;
import io.github.flemmli97.tenshilib.TenshiLib;
import io.github.flemmli97.tenshilib.client.model.BedrockGeometryParser;
import io.github.flemmli97.tenshilib.client.model.ModelPartsContainer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Stores Bedrock-Geo-Models. The files are read under assets/<modid>/model/entity and assigned the
 * id modid:filename without ".json"
 * Example: an animation ./assets/foo/model/entity/bar.json has the id foo:bar
 */
public class ModelManager extends SimpleJsonResourceReloadListener {

    public static final String DIRECTORY = "model/entity";

    private static final ModelManager INSTANCE = new ModelManager();

    private final Map<ResourceLocation, ReloadableCache<ModelPartsContainer>> animations = new HashMap<>();

    private ModelManager() {
        super(BedrockGeometryParser.GSON, DIRECTORY);
    }

    public static ModelManager getInstance() {
        return INSTANCE;
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> map, ResourceManager manager, ProfilerFiller profiler) {
        map.forEach((res, json) -> {
            try {
                ModelPartsContainer read = BedrockGeometryParser.GSON.fromJson(json, ModelPartsContainer.class);
                ResourceLocation id = ResourceLocation.fromNamespaceAndPath(res.getNamespace(), res.getPath().replace(".geo", ""));
                this.getModel(id).update(read);
            } catch (Exception e) {
                TenshiLib.LOGGER.error("Unable to parse geo model file {}", res, e);
            }
        });
    }

    public ReloadableCache<ModelPartsContainer> getModel(ResourceLocation res) {
        return this.getModel(res, null);
    }

    public ReloadableCache<ModelPartsContainer> getModel(ResourceLocation res, Consumer<ModelPartsContainer> onChange) {
        return this.animations.computeIfAbsent(res, r -> new ReloadableCache<>())
                .onChange(onChange);
    }
}
