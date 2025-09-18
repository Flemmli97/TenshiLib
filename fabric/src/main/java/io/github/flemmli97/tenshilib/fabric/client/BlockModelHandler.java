package io.github.flemmli97.tenshilib.fabric.client;

import com.google.common.collect.ImmutableMap;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.client.renderer.block.model.BlockModel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemDisplayContext;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

public class BlockModelHandler {

    public static final ResourceLocation SEPARATE_ID = ResourceLocation.fromNamespaceAndPath("neoforge", "separate_transforms");

    /**
     * From NeoForges SeparateTransformsModel#Loader.
     * This is able to read and handle the neo equivalent json
     */
    public static BlockModel tryDeserialize(JsonElement json, Type type, JsonDeserializationContext context) {
        JsonObject obj = json.getAsJsonObject();
        if (!obj.has("loader"))
            return null;
        ResourceLocation name;
        if (obj.get("loader").isJsonObject()) {
            JsonObject loaderObj = obj.getAsJsonObject("loader");
            name = ResourceLocation.parse(GsonHelper.getAsString(loaderObj, "id"));
        } else {
            name = ResourceLocation.parse(GsonHelper.getAsString(obj, "loader"));
        }
        if (name.equals(SEPARATE_ID)) {
            obj.remove("loader");
            BlockModel root = context.deserialize(obj, BlockModel.class);
            BlockModel baseModel = context.deserialize(GsonHelper.getAsJsonObject(obj, "base"), BlockModel.class);
            JsonObject perspectiveData = GsonHelper.getAsJsonObject(obj, "perspectives");
            Map<ItemDisplayContext, BlockModel> perspectives = new HashMap<>();
            for (ItemDisplayContext transform : ItemDisplayContext.values()) {
                if (perspectiveData.has(transform.getSerializedName())) {
                    BlockModel perspectiveModel = context.deserialize(GsonHelper.getAsJsonObject(perspectiveData, transform.getSerializedName()), BlockModel.class);
                    perspectives.put(transform, perspectiveModel);
                }
            }
            return new SeparateTransformsModel(root, baseModel, ImmutableMap.copyOf(perspectives));
        }
        return null;
    }
}
