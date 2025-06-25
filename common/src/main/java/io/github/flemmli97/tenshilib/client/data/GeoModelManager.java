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
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

/**
 * Stores Bedrock-Geo-Models.
 * The files are read under assets/<modid>/tenshilib/models
 */
public class GeoModelManager extends SimpleJsonResourceReloadListener {

    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(TenshiLib.MODID, "models");
    public static final String DIRECTORY = String.format("%s/%s", ID.getNamespace(), ID.getPath());

    private static final GeoModelManager INSTANCE = new GeoModelManager();

    private final Map<ResourceLocation, ReloadableCache<ModelPartsContainer>> models = new HashMap<>();
    private boolean reloaded;

    private GeoModelManager() {
        super(BedrockGeometryParser.GSON, DIRECTORY);
    }

    public static GeoModelManager getInstance() {
        return INSTANCE;
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> map, ResourceManager manager, ProfilerFiller profiler) {
        Set<ResourceLocation> present = new HashSet<>();
        map.forEach((res, json) -> {
            try {
                ModelPartsContainer read = BedrockGeometryParser.GSON.fromJson(json, ModelPartsContainer.class);
                ResourceLocation id = ResourceLocation.fromNamespaceAndPath(res.getNamespace(), res.getPath().replace(".geo", ""));
                this.getModel(id).update(read);
                present.add(id);
            } catch (Exception e) {
                TenshiLib.LOGGER.error("Unable to parse geo model file {}", res, e);
            }
        });
        this.reloaded = true;
        this.models.keySet().forEach(id -> {
            if (!present.contains(id)) {
                TenshiLib.LOGGER.error("Unable to locate model {}!", id);
            }
        });
    }

    public ReloadableCache<ModelPartsContainer> getModel(ResourceLocation id) {
        return this.getModel(id, null);
    }

    public ReloadableCache<ModelPartsContainer> getModel(ResourceLocation id, Consumer<ModelPartsContainer> onChange) {
        return this.models.computeIfAbsent(id, r -> {
            if (this.reloaded)
                TenshiLib.LOGGER.error("Model {} is not present! Returned result will be empty!", r);
            return new ReloadableCache<>();
        }).onChange(onChange);
    }
}
