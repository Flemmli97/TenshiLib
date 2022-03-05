package io.github.flemmli97.tenshilib.common.config;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import io.github.flemmli97.tenshilib.TenshiLib;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.Mth;
import org.apache.commons.lang3.tuple.Pair;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

/**
 * Json config with comments that is syntax wise simliar to setup to the forge config.
 * Used for serialization only.
 * Use the provided builder to create a config instance
 */
public class CommentedJsonConfig {

    private final Map<String, CommentedVal<?>> configVals;

    private final int confVersion;

    private CommentedJsonConfig(int version, Map<String, CommentedVal<?>> map) {
        this.confVersion = version;
        this.configVals = map;
    }

    public JsonObject serialize(Gson gson) {
        JsonObject obj = new JsonObject();
        obj.addProperty("version", this.confVersion);
        this.configVals.forEach((key, val) -> {
            JsonObject vO = new JsonObject();
            vO.add("__comment", gson.toJsonTree(val.__comments));
            vO.add("input", gson.toJsonTree(val.input));
            obj.add(key, vO);
        });
        return obj;
    }

    @SuppressWarnings("unchecked")
    public Set<String> deserialize(JsonObject object, Gson gson) {
        Set<String> faulty = new HashSet<>();
        int version = GsonHelper.getAsInt(object, "version", 0);
        if (version < this.confVersion)
            faulty.add("Config Version");
        for (Map.Entry<String, CommentedVal<?>> e : this.configVals.entrySet()) {
            if (object.has(e.getKey())) {
                CommentedVal<Object> val = (CommentedVal<Object>) e.getValue();
                try {
                    val.set(gson.fromJson(object.get(e.getKey()).getAsJsonObject().get("input"), val.input.getClass()));
                } catch (JsonSyntaxException | IllegalStateException ex) {
                    faulty.add(e.getKey());
                }
            }
        }
        return faulty;
    }

    public static class CommentedVal<T> {

        protected final List<String> __comments;
        protected T input;

        private CommentedVal(List<String> comments, T input) {
            this.__comments = comments;
            this.input = input;
        }

        public T get() {
            return this.input;
        }

        public void set(T value) {
            this.input = value;
        }
    }

    public static class IntVal extends CommentedVal<Integer> {

        private transient final int min, max;

        private IntVal(List<String> comments, int input, int min, int max) {
            super(comments, input);
            this.min = min;
            this.max = max;
        }

        @Override
        public Integer get() {
            return Mth.clamp(this.input, this.min, this.max);
        }
    }

    public static class DoubleVal extends CommentedVal<Double> {

        private final transient double min, max;

        private DoubleVal(List<String> comments, double input, double min, double max) {
            super(comments, input);
            this.min = min;
            this.max = max;
        }

        @Override
        public Double get() {
            return Mth.clamp(this.input, this.min, this.max);
        }
    }

    public static class Builder {

        private List<String> comments;
        private final Map<String, CommentedVal<?>> conf = new LinkedHashMap<>();
        private String path = "";

        public Builder comment(String... comments) {
            List<String> list = new ArrayList<>();
            for (String s : comments) {
                String[] split = s.split("\n");
                list.addAll(Arrays.asList(split));
            }
            this.comments = list;
            return this;
        }

        public Builder push(String path) {
            if (this.path.isEmpty())
                this.path = path;
            else
                this.path = path + "." + this.path;
            return this;
        }

        public Builder pop() {
            if (this.path.isEmpty()) {
                TenshiLib.logger.error("Tried to pop config with empty path!");
                return this;
            }
            int i = this.path.indexOf(".");
            if (i >= 0 && i + 1 < this.path.length())
                this.path = this.path.substring(i + 1);
            else
                this.path = "";
            return this;
        }

        public <T> CommentedVal<T> define(String name, T value) {
            return this.define(name, new CommentedVal<>(this.comments, value));
        }

        private <C, T extends CommentedVal<C>> T define(String name, T val) {
            if (!this.path.isEmpty())
                name = this.path + "." + name;
            this.conf.put(name, val);
            this.comments = null;
            return val;
        }

        public IntVal defineInRange(String name, int value, int min, int max) {
            List<String> comment = this.comments != null ? this.comments : new ArrayList<>();
            if (Integer.MAX_VALUE == max)
                comment.add("Range: > " + min);
            else if (Integer.MIN_VALUE == min)
                comment.add("Range: < " + min);
            else
                comment.add("Range: " + min + " ~ " + max);
            return this.define(name, new IntVal(comment, value, min, max));
        }

        public DoubleVal defineInRange(String name, double value, double min, double max) {
            List<String> comment = this.comments != null ? this.comments : new ArrayList<>();
            if (Double.MAX_VALUE == max)
                comment.add("Range: > " + min);
            else if (Double.MIN_VALUE == min)
                comment.add("Range: < " + min);
            else
                comment.add("Range: " + min + " ~ " + max);
            return this.define(name, new DoubleVal(comment, value, min, max));
        }

        public static <C> Pair<JsonConfig<CommentedJsonConfig>, C> create(Path file, int version, Function<Builder, C> cons) {
            Builder builder = new Builder();
            C c = cons.apply(builder);
            return Pair.of(new JsonConfig<>(file, CommentedJsonConfig.class, new CommentedJsonConfig(version, builder.conf)), c);
        }
    }
}
