package io.github.flemmli97.tenshilib.client.model;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import io.github.flemmli97.tenshilib.common.utils.ArrayUtils;
import io.github.flemmli97.tenshilib.common.utils.JsonUtils;
import io.github.flemmli97.tenshilib.common.utils.MathUtils;
import net.minecraft.util.Mth;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A Blockbench animation using the free model animation from Blockbench.
 */
public class BlockBenchAnimations {

    private final Map<String, Animation> animations = new HashMap<>();

    public void reload(JsonObject obj) {
        this.animations.clear();
        if (obj.has("animations")) {
            for (Map.Entry<String, JsonElement> anims : obj.getAsJsonObject("animations").entrySet())
                if (anims.getValue() instanceof JsonObject)
                    this.animations.put(anims.getKey(), new Animation((JsonObject) anims.getValue()));
        }
    }

    public void doAnimation(ExtendedModel model, String name, int ticker, float partialTicks) {
        Animation animation = this.animations.get(name);
        if (animation != null) {
            animation.animate(model, ticker, partialTicks);
        }
    }

    public int animationLength(String name) {
        Animation animation = this.animations.get(name);
        return animation != null ? animation.length : 0;
    }

    @Override
    public String toString() {
        return "Animation: " + this.animations;
    }

    public static class Animation {

        public final int length;
        public final boolean loop;
        private final List<AnimationComponent> components = new ArrayList<>();

        public Animation(JsonObject json) {
            this.length = (int) Math.floor((JsonUtils.get(json, "animation_length", 0.0) * 20) + 1);
            this.loop = JsonUtils.get(json, "loop", false);
            JsonObject components = JsonUtils.getObj(json, "bones");
            components.entrySet().forEach(e -> this.components.add(new AnimationComponent(e.getKey(), e.getValue().getAsJsonObject())));
        }

        public void animate(ExtendedModel model, int ticker, float partialTicks) {
            if (this.loop)
                ticker = ticker % this.length;
            for (AnimationComponent comp : this.components)
                comp.animate(model, ticker, partialTicks);
        }

        @Override
        public String toString() {
            return String.format("\nloop: %b, length: %d, components: %s", this.loop, this.length, this.components);
        }
    }

    public static class AnimationComponent {

        private final String name;
        private float[][] rotations;
        private float[][] positions;
        private float[][] scales;

        public AnimationComponent(String name, JsonObject obj) {
            this.name = name;
            int i = 0;
            if (obj.has("position")) {
                JsonObject position = this.tryGet(obj, "position");
                this.positions = new float[position.size()][4];
                for (Map.Entry<String, JsonElement> e : position.entrySet()) {
                    if (e.getValue() instanceof JsonArray arr) {
                        this.positions[i] = new float[]{Math.round(Float.parseFloat(e.getKey()) * 20), arr.get(0).getAsFloat(), arr.get(1).getAsFloat(), arr.get(2).getAsFloat()};
                        i++;
                    }
                }
                Arrays.sort(this.positions, Comparator.comparingDouble(arr -> arr[0]));
            }
            if (obj.has("rotation")) {
                JsonObject rotation = this.tryGet(obj, "rotation");
                this.rotations = new float[rotation.size()][4];
                i = 0;
                for (Map.Entry<String, JsonElement> e : rotation.entrySet()) {
                    if (e.getValue() instanceof JsonArray arr) {
                        this.rotations[i] = new float[]{Math.round(Float.parseFloat(e.getKey()) * 20), MathUtils.degToRad(arr.get(0).getAsFloat()),
                                MathUtils.degToRad(arr.get(1).getAsFloat()), MathUtils.degToRad(arr.get(2).getAsFloat())};
                        i++;
                    }
                }
                Arrays.sort(this.rotations, Comparator.comparingDouble(arr -> arr[0]));
            }
            if (obj.has("scale")) {
                JsonObject scale = this.tryGet(obj, "scale");
                this.scales = new float[scale.size()][4];
                i = 0;
                for (Map.Entry<String, JsonElement> e : scale.entrySet()) {
                    if (e.getValue() instanceof JsonArray arr) {
                        this.scales[i] = new float[]{Math.round(Float.parseFloat(e.getKey()) * 20), arr.get(0).getAsFloat() - 1, arr.get(1).getAsFloat() - 1, arr.get(2).getAsFloat() - 1};
                        i++;
                    }
                }
                Arrays.sort(this.scales, Comparator.comparingDouble(arr -> arr[0]));
            }
        }

        private JsonObject tryGet(JsonObject obj, String name) {
            JsonElement el = obj.get(name);
            if (el.isJsonObject())
                return (JsonObject) el;
            else if (el.isJsonArray()) {
                JsonObject val = new JsonObject();
                val.add("0", el);
                return val;
            }
            return null;
        }

        public void animate(ExtendedModel model, int ticker, float partialTicks) {
            ModelPartHandler.ModelPartExtended modelPart = model.getHandler().getPartNullable(this.name);
            if (modelPart == null)
                return;
            float actualTick = Math.max(ticker - 1 + partialTicks, 0);
            if (this.positions != null) {
                if (this.positions.length == 1) {
                    modelPart.x += this.positions[0][1];
                    modelPart.y -= this.positions[0][2];
                    modelPart.z += this.positions[0][3];
                } else {
                    int id = 1;
                    float[] pos = this.positions[id];
                    while (pos[0] < ticker && ++id < this.positions.length)
                        pos = this.positions[id];
                    float[] posPrev = this.positions[id - 1];
                    float prog = Mth.clamp((actualTick - posPrev[0]) / (pos[0] - posPrev[0]), 0F, 1F);
                    modelPart.x += this.interpolate(posPrev[1], pos[1], prog);
                    modelPart.y -= this.interpolate(posPrev[2], pos[2], prog);
                    modelPart.z += this.interpolate(posPrev[3], pos[3], prog);
                }
            }
            if (this.rotations != null) {
                if (this.rotations.length == 1) {
                    modelPart.xRot += this.rotations[0][1];
                    modelPart.yRot += this.rotations[0][2];
                    modelPart.zRot += this.rotations[0][3];
                } else {
                    int id = 1;
                    float[] rot = this.rotations[id];
                    while (rot[0] < ticker && ++id < this.rotations.length)
                        rot = this.rotations[id];
                    float[] rotPrev = this.rotations[id - 1];
                    float prog = Mth.clamp((actualTick - rotPrev[0]) / (rot[0] - rotPrev[0]), 0F, 1F);
                    modelPart.xRot += this.interpolate(rotPrev[1], rot[1], prog);
                    modelPart.yRot += this.interpolate(rotPrev[2], rot[2], prog);
                    modelPart.zRot += this.interpolate(rotPrev[3], rot[3], prog);
                }
            }
            if (this.scales != null) {
                if (this.scales.length == 1) {
                    modelPart.xScale += this.scales[0][3];
                    modelPart.yScale += this.scales[0][3];
                    modelPart.zScale += this.scales[0][3];
                } else {
                    int id = 1;
                    float[] scale = this.scales[id];
                    while (scale[0] < ticker && ++id < this.scales.length)
                        scale = this.scales[id];
                    float[] scalePrev = this.scales[id - 1];
                    float prog = Mth.clamp((actualTick - scalePrev[0]) / (scale[0] - scalePrev[0]), 0F, 1F);
                    modelPart.xScale += this.interpolate(scalePrev[1], scale[1], prog);
                    modelPart.yScale += this.interpolate(scalePrev[2], scale[2], prog);
                    modelPart.zScale += this.interpolate(scalePrev[3], scale[3], prog);
                }
            }
        }

        private float interpolate(float start, float end, float progress) {
            return start + (end - start) * progress;
        }

        @Override
        public String toString() {
            return String.format("%s rot: {%s}; pos: {%s}; scale: {%s}", this.name, ArrayUtils.arrayToString(this.rotations, arr -> "[" + ArrayUtils.arrayToString(arr) + "]"),
                    ArrayUtils.arrayToString(this.positions, arr -> "[" + ArrayUtils.arrayToString(arr) + "]"), ArrayUtils.arrayToString(this.scales, arr -> "[" + ArrayUtils.arrayToString(arr) + "]"));
        }
    }
}
