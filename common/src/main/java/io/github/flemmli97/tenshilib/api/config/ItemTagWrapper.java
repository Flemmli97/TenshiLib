package io.github.flemmli97.tenshilib.api.config;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import io.github.flemmli97.tenshilib.common.utils.JsonUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.Tag;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;

import java.lang.reflect.Type;
import java.util.List;

public class ItemTagWrapper extends SimpleItemStackWrapper {

    private String tag;
    private List<Item> list;

    public ItemTagWrapper(String tagName) {
        this(tagName, 1);
    }

    public ItemTagWrapper(String tagName, int count) {
        super("", count);
        this.tag = tagName;
    }

    @Override
    public Item getItem() {
        Tag<Item> tags = ItemTags.getAllTags().getTagOrEmpty(new ResourceLocation(this.tag));
        this.list = tags.getValues();
        if (!this.list.isEmpty())
            this.item = this.list.get(0);
        else
            this.item = Items.AIR;
        return this.item;
    }

    @Override
    public List<Item> getItemList() {
        return this.list;
    }

    @Override
    public boolean hasList() {
        return true;
    }

    @Override
    public ItemTagWrapper readFromString(String s) {
        String[] parts = s.split(",");
        this.tag = parts[0];
        this.count = parts.length < 2 ? 1 : Integer.parseInt(parts[1]);
        return this;
    }

    @Override
    public String writeToString() {
        return this.tag + (this.count != 1 ? "," + this.count : "");
    }

    public static String usage() {
        return "Usage: item-tag,<amount>";
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj instanceof ItemTagWrapper prop) {
            return prop.writeToString().equals(this.toString());
        }
        return false;
    }

    @Override
    public int hashCode() {
        return this.writeToString().hashCode();
    }

    public static class Serializer implements JsonDeserializer<ItemTagWrapper>, JsonSerializer<ItemTagWrapper> {

        @Override
        public JsonElement serialize(ItemTagWrapper src, Type typeOfSrc, JsonSerializationContext context) {
            JsonObject obj = new JsonObject();
            obj.add("tag", new JsonPrimitive(src.tag));
            if (src.count != 1)
                obj.add("count", new JsonPrimitive(src.count));
            return obj;
        }

        @Override
        public ItemTagWrapper deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            JsonObject obj = json.getAsJsonObject();
            int count = JsonUtils.get(obj, "count", 1);
            return new ItemTagWrapper(obj.get("tag").getAsString(), count);
        }
    }
}
