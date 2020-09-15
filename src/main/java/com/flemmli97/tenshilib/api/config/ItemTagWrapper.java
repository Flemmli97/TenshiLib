package com.flemmli97.tenshilib.api.config;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import net.minecraft.entity.boss.dragon.EnderDragonEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.tags.ITag;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.Tag;
import net.minecraft.tags.TagRegistryManager;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;

import java.lang.reflect.Type;
import java.util.List;

//TODO: this needs rework
public class ItemTagWrapper extends SimpleItemStackWrapper {

    private String tag;
    private ItemStack firstTag = ItemStack.EMPTY;
    private List<Item> list;

    public ItemTagWrapper(String tagName) {
        this(tagName, 1);
    }

    public ItemTagWrapper(String tagName, int amount) {
        super(Items.AIR, amount);
        this.tag = tagName;
    }

    private void reloadItem() {
        ITag tags = ItemTags.getCollection().func_241834_b(new ResourceLocation(this.tag));
        this.list = tags.getAllElements();
        if(!this.list.isEmpty())
            this.firstTag = new ItemStack(this.list.get(0));
        else
            this.firstTag = ItemStack.EMPTY;
        this.firstTag.setCount(this.count);
    }

    @Override
    public ItemStack getStack() {
        return this.firstTag.copy();
    }

    @Override
    public Item getItem() {
        return this.firstTag.getItem();
    }

    @Override
    public NonNullList<ItemStack> getStackList() {
        return null;
    }

    @Override
    public boolean hasList() {
        return true;
    }

    @Override
    public ItemTagWrapper readFromString(String s) {
        String[] parts = s.split(",");
        this.tag = parts[0];
        this.reloadItem();
        return this;
    }

    @Override
    public String writeToString() {
        return this.tag + (this.count != 1 ? "," + this.count : "");
    }

    @Override
    public String usage() {
        return "Usage: oreDict<,amount>";
    }

    @Override
    public boolean equals(Object obj) {
        if(this == obj){
            return true;
        }
        if(obj instanceof ItemTagWrapper){
            ItemTagWrapper prop = (ItemTagWrapper) obj;
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
            obj.add("oredict", new JsonPrimitive(src.tag));
            if(src.count != 1)
                obj.add("count", new JsonPrimitive(src.count));
            return obj;
        }

        @Override
        public ItemTagWrapper deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            JsonObject obj = json.getAsJsonObject();
            int count = 1;
            if(obj.get("count") instanceof JsonPrimitive && obj.get("count").getAsJsonPrimitive().isNumber())
                count = obj.get("count").getAsInt();
            return new ItemTagWrapper(obj.get("oredict").getAsString(), count);
        }
    }
}
