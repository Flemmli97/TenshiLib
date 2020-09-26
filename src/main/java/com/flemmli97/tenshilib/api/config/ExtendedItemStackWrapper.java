package com.flemmli97.tenshilib.api.config;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.JsonToNBT;

import javax.annotation.Nullable;
import java.lang.reflect.Type;

/**
 * Itemstack supporting nbt. Not exactly made as a single line string config value. More as a json one.
 */
public class ExtendedItemStackWrapper extends SimpleItemStackWrapper {

    protected CompoundNBT nbtTagCompound;

    public ExtendedItemStackWrapper(CompoundNBT compound) {
        this(compound.getString("id"), compound.getInt("Count"), compound.getCompound("nbt"));
    }

    public ExtendedItemStackWrapper(String item){
        this(item, 1);
    }

    public ExtendedItemStackWrapper(String item, int count){
        this(item, count, null);
    }

    public ExtendedItemStackWrapper(String item, int count, CompoundNBT nbt){
        super(item, count);
    }

    public ExtendedItemStackWrapper setNBT(CompoundNBT nbt) {
        this.nbtTagCompound = nbt;
        return this;
    }

    public ExtendedItemStackWrapper ignoreNBT() {
        this.nbtTagCompound = null;
        return this;
    }

    @Override
    public ExtendedItemStackWrapper readFromString(String s) {
        try {
            CompoundNBT nbt = JsonToNBT.getTagFromJson(s);
            this.nbtTagCompound = nbt.getCompound("nbt");
            this.count = nbt.getInt("Count");
            this.reg = nbt.getString("id");
        } catch (CommandSyntaxException e) {
            e.printStackTrace();
        }
        return this;
    }

    @Override
    public String writeToString() {
        CompoundNBT nbt = new CompoundNBT();
        nbt.put("nbt", this.nbtTagCompound.copy());
        nbt.putString("id", this.item.getRegistryName().toString());
        nbt.putInt("Count", this.count);
        return nbt.toString();
    }

    public static String usage() {
        return "Complete ItemStack nbt (meaning with id and count)";
    }

    @Nullable
    public CompoundNBT getTag() {
        return this.nbtTagCompound == null ? null : this.nbtTagCompound.copy();
    }

    @Override
    public ItemStack getStack() {
        ItemStack stack = super.getStack();
        if(!stack.isEmpty())
            stack.setTag(this.nbtTagCompound.copy());
        return stack;
    }

    @Override
    public boolean equals(Object obj) {
        if(this == obj){
            return true;
        }
        if(obj instanceof ExtendedItemStackWrapper){
            ExtendedItemStackWrapper prop = (ExtendedItemStackWrapper) obj;
            return prop.toString().equals(this.toString());
        }
        return false;
    }

    public static class Serializer implements JsonDeserializer<ExtendedItemStackWrapper>, JsonSerializer<ExtendedItemStackWrapper> {

        @Override
        public JsonElement serialize(ExtendedItemStackWrapper src, Type typeOfSrc, JsonSerializationContext context) {
            JsonObject obj = new JsonObject();
            obj.add("item", new JsonPrimitive(src.reg));
            if(src.count != 1)
                obj.add("count", new JsonPrimitive(src.count));
            if(src.nbtTagCompound != null)
                obj.add("nbt", new JsonPrimitive(src.nbtTagCompound.toString()));
            return obj;
        }

        @Override
        public ExtendedItemStackWrapper deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            JsonObject obj = json.getAsJsonObject();
            int count = 1;
            if(obj.get("count") instanceof JsonPrimitive && obj.get("count").getAsJsonPrimitive().isNumber())
                count = obj.get("count").getAsInt();
            JsonObject nbt = obj.has("nbt") ? obj.get("nbt").getAsJsonObject() : null;
            CompoundNBT compound = null;
            if(nbt != null){
                try{
                    compound = JsonToNBT.getTagFromJson(nbt.toString());
                }catch(CommandSyntaxException e){
                    e.printStackTrace();
                }
            }
            return new ExtendedItemStackWrapper(obj.get("item").getAsString(), count, compound);
        }
    }
}
