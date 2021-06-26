package com.flemmli97.tenshilib.client.model;


import com.flemmli97.tenshilib.TenshiLib;
import com.flemmli97.tenshilib.common.utils.ArrayUtils;
import com.flemmli97.tenshilib.common.utils.JsonUtils;
import com.flemmli97.tenshilib.common.utils.MathUtils;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.client.renderer.model.Model;
import net.minecraft.client.renderer.model.ModelRenderer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
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

    private static final Gson gson = new Gson();

    private final Map<String, Animation> animations = new HashMap<>();

    public BlockBenchAnimations(Model model, ResourceLocation res) {
        InputStream input = BlockBenchAnimations.class.getResourceAsStream("/assets/" + res.getNamespace() + "/" + res.getPath());
        if (input == null) {
            TenshiLib.logger.error("Couldn't find animation: " + res);
            return;
        }
        try {
            JsonObject obj = gson.getAdapter(JsonObject.class).read(gson.newJsonReader(new InputStreamReader(input)));
            if (obj.has("animations"))
                for (Map.Entry<String, JsonElement> anims : obj.getAsJsonObject("animations").entrySet())
                    if (anims.getValue() instanceof JsonObject)
                        this.animations.put(anims.getKey(), new Animation(model, (JsonObject) anims.getValue()));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void doAnimation(String name, int ticker, float partialTicks) {
        Animation animation = this.animations.get(name);
        if (animation != null) {
            animation.animate(ticker, partialTicks);
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

        public Animation(Model model, JsonObject json) {
            this.length = (int) Math.floor((JsonUtils.get(json, "animation_length", 0.0) * 20) + 1);
            this.loop = JsonUtils.get(json, "loop", false);
            JsonObject components = JsonUtils.getObj(json, "bones");
            for (Field field : model.getClass().getFields()) {
                if (ModelRenderer.class.isAssignableFrom(field.getType()) && components.has(field.getName())) {
                    try {
                        this.components.add(new AnimationComponent(field.getName(), (ModelRenderer) field.get(model), components.getAsJsonObject(field.getName())));
                    } catch (IllegalAccessException e) {
                        TenshiLib.logger.error("Error accessing field value: {}", field.getName());
                        e.printStackTrace();
                    }
                }
            }
        }

        public void animate(int ticker, float partialTicks) {
            if (this.loop)
                ticker = ticker % this.length;
            for (AnimationComponent comp : this.components)
                comp.animate(ticker, partialTicks);
        }

        @Override
        public String toString() {
            return String.format("\nloop: %b, length: %d, components: %s", this.loop, this.length, this.components);
        }
    }

    public static class AnimationComponent {

        private final String name;
        private final ModelRenderer model;
        private float[][] rotations;
        private float[][] positions;
        private float[][] scales;

        public AnimationComponent(String name, ModelRenderer part, JsonObject obj) {
            this.name = name;
            this.model = part;
            int i = 0;
            if (obj.has("position")) {
                JsonObject position = this.tryGet(obj, "position");
                this.positions = new float[position.size()][4];
                for (Map.Entry<String, JsonElement> e : position.entrySet()) {
                    if (e.getValue() instanceof JsonArray) {
                        JsonArray arr = (JsonArray) e.getValue();
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
                    if (e.getValue() instanceof JsonArray) {
                        JsonArray arr = (JsonArray) e.getValue();
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
                    if (e.getValue() instanceof JsonArray) {
                        JsonArray arr = (JsonArray) e.getValue();
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

        //For now till i find a better way (maybe without loops)
        public void animate(int ticker, float partialTicks) {
            float actualTick = Math.max(ticker - 1 + partialTicks, 0);
            if (this.positions != null) {
                if (this.positions.length == 1) {
                    this.model.rotationPointX += this.positions[0][1];
                    this.model.rotationPointY -= this.positions[0][2];
                    this.model.rotationPointZ += this.positions[0][3];
                } else {
                    int id = 1;
                    float[] pos = this.positions[id];
                    while (pos[0] < ticker && ++id < this.positions.length)
                        pos = this.positions[id];
                    float[] posPrev = this.positions[id - 1];
                    float prog = MathHelper.clamp((actualTick - posPrev[0]) / (pos[0] - posPrev[0]), 0F, 1F);
                    this.model.rotationPointX += this.interpolate(posPrev[1], pos[1], prog);
                    this.model.rotationPointY -= this.interpolate(posPrev[2], pos[2], prog);
                    this.model.rotationPointZ += this.interpolate(posPrev[3], pos[3], prog);
                }
            }
            if (this.rotations != null) {
                if (this.rotations.length == 1) {
                    this.model.rotateAngleX += this.rotations[0][1];
                    this.model.rotateAngleY += this.rotations[0][2];
                    this.model.rotateAngleZ += this.rotations[0][3];
                } else {
                    int id = 1;
                    float[] rot = this.rotations[id];
                    while (rot[0] < ticker && ++id < this.rotations.length)
                        rot = this.rotations[id];
                    float[] rotPrev = this.rotations[id - 1];
                    float prog = MathHelper.clamp((actualTick - rotPrev[0]) / (rot[0] - rotPrev[0]), 0F, 1F);
                    this.model.rotateAngleX += this.interpolate(rotPrev[1], rot[1], prog);
                    this.model.rotateAngleY += this.interpolate(rotPrev[2], rot[2], prog);
                    this.model.rotateAngleZ += this.interpolate(rotPrev[3], rot[3], prog);
                }
            }
            if (this.scales != null && this.model instanceof ModelRendererPlus) {
                ModelRendererPlus plus = (ModelRendererPlus) this.model;
                if (this.scales.length == 1) {
                    plus.scaleX += this.scales[0][3];
                    plus.scaleY += this.scales[0][3];
                    plus.scaleZ += this.scales[0][3];
                } else {
                    int id = 1;
                    float[] scale = this.scales[id];
                    while (scale[0] < ticker && ++id < this.scales.length)
                        scale = this.scales[id];
                    float[] scalePrev = this.scales[id - 1];
                    float prog = MathHelper.clamp((actualTick - scalePrev[0]) / (scale[0] - scalePrev[0]), 0F, 1F);
                    plus.scaleX += this.interpolate(scalePrev[1], scale[1], prog);
                    plus.scaleY += this.interpolate(scalePrev[2], scale[2], prog);
                    plus.scaleZ += this.interpolate(scalePrev[3], scale[3], prog);
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
