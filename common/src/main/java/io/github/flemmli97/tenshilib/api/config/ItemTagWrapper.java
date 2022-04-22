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
import io.github.flemmli97.tenshilib.platform.PlatformUtils;
import net.minecraft.core.HolderSet;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ItemTagWrapper extends SimpleItemStackWrapper {

    private List<Item> list = new ArrayList<>();
    private TagKey<Item> tag;

    public ItemTagWrapper(String item, int count) {
        super(item, count);
    }

    public ItemTagWrapper(TagKey<Item> tag) {
        this(tag, 1);
    }

    public ItemTagWrapper(TagKey<Item> tag, int count) {
        super("", count);
        this.tag = tag;
    }

    @Override
    public Item getItem() {
        if (this.item == null) {
            if (!this.reg.isEmpty()) {
                return super.getItem();
            }
            if (this.tag == null) {
                this.item = Items.AIR;
                return this.item;
            }
            Optional<HolderSet.Named<Item>> t = Registry.ITEM.getTag(this.tag);
            t.ifPresent(set -> set.forEach(holder -> this.list.add(holder.value())));
            if (!this.list.isEmpty())
                this.item = this.list.get(0);
            else
                this.item = Items.AIR;
        }
        return this.item;
    }

    @Override
    public List<Item> getItemList() {
        this.getItem();
        return this.list;
    }

    @Override
    public boolean hasList() {
        return true;
    }

    @Override
    public boolean match(ItemStack stack) {
        return stack.is(this.tag);
    }

    @Override
    public ItemTagWrapper readFromString(String s) {
        String[] parts = s.split(",");
        if (parts[0].startsWith("#"))
            this.tag = PlatformUtils.INSTANCE.itemTag(new ResourceLocation(parts[0].substring(1)));
        else
            this.reg = parts[0];
        this.count = parts.length < 2 ? 1 : Integer.parseInt(parts[1]);
        return this;
    }

    @Override
    public String writeToString() {
        if (this.tag != null)
            return "#" + this.tag.location() + (this.count != 1 ? "," + this.count : "");
        return super.writeToString();
    }

    public static String usage() {
        return "Usage: <#item-tag|item>,<amount>";
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
            if (src.tag != null)
                obj.add("tag", new JsonPrimitive(src.tag.location().toString()));
            else
                obj.add("item", new JsonPrimitive(src.reg));
            if (src.count != 1)
                obj.add("count", new JsonPrimitive(src.count));
            return obj;
        }

        @Override
        public ItemTagWrapper deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            JsonObject obj = json.getAsJsonObject();
            int count = JsonUtils.get(obj, "count", 1);
            if (obj.has("tag"))
                return new ItemTagWrapper(PlatformUtils.INSTANCE.itemTag(new ResourceLocation(obj.get("tag").getAsString())), count);
            else
                return new ItemTagWrapper(obj.get("item").getAsString(), count);
        }
    }
}
