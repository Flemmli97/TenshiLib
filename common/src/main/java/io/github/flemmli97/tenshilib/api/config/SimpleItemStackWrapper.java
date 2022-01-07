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
import net.minecraft.world.item.ItemStack;

import java.lang.reflect.Type;

public class SimpleItemStackWrapper extends ItemWrapper {

    protected int count;

    public SimpleItemStackWrapper(String s) {
        this(s, 1);
    }

    public SimpleItemStackWrapper(String s, int count) {
        super(s);
        this.count = count;
    }

    public SimpleItemStackWrapper setIgnoreAmount() {
        this.count = 1;
        return this;
    }

    @Override
    public ItemStack getStack() {
        ItemStack stack = super.getStack();
        if (!stack.isEmpty())
            stack.setCount(this.count);
        return stack;
    }

    @Override
    public SimpleItemStackWrapper readFromString(String s) {
        String[] parts = s.split(",");
        super.readFromString(parts[0]);
        this.count = parts.length < 2 ? 1 : Integer.parseInt(parts[1]);
        return this;
    }

    @Override
    public String writeToString() {
        return super.writeToString() + (this.count != 1 ? "," + this.count : "");
    }

    public static String usage() {
        return "Usage: registryname,<amount>. Leave empty for no item";
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj instanceof SimpleItemStackWrapper prop) {
            return prop.toString().equals(this.toString());
        }
        return false;
    }

    public static class Serializer implements JsonDeserializer<SimpleItemStackWrapper>, JsonSerializer<SimpleItemStackWrapper> {

        @Override
        public JsonElement serialize(SimpleItemStackWrapper src, Type typeOfSrc, JsonSerializationContext context) {
            JsonObject obj = new JsonObject();
            obj.add("item", new JsonPrimitive(src.reg));
            if (src.count != 1)
                obj.add("count", new JsonPrimitive(src.count));
            return obj;
        }

        @Override
        public SimpleItemStackWrapper deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            JsonObject obj = json.getAsJsonObject();
            return new SimpleItemStackWrapper(obj.get("item").getAsString(), JsonUtils.get(obj, "count", 1));
        }
    }
}
