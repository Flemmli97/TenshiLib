package io.github.flemmli97.tenshilib.client.model;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import io.github.flemmli97.tenshilib.common.utils.ArrayUtils;
import io.github.flemmli97.tenshilib.common.utils.JsonUtils;
import net.minecraft.util.Mth;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
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
        this.doAnimation(model, name, ticker, partialTicks, 1);
    }

    public void doAnimation(ExtendedModel model, String name, int ticker, float partialTicks, float interpolation) {
        this.doAnimation(model, name, ticker, partialTicks, interpolation, false);
    }

    /**
     * Run the given animation
     *
     * @param model         The model to run the animation on
     * @param name          The name of the animation
     * @param ticker        Animation ticker
     * @param partialTicks  Partial tick for lerping
     * @param interpolation An interpolation value between 0-1 indicating
     * @param mirror        If true mirrors the animation. Components with "left"/"right" in their names will be swapped
     */
    public void doAnimation(ExtendedModel model, String name, int ticker, float partialTicks, float interpolation, boolean mirror) {
        Animation animation = this.animations.get(name);
        if (animation != null) {
            animation.animate(model, ticker, partialTicks, Mth.clamp(interpolation, 0, 1), mirror);
        }
    }

    /**
     * Animation length in ticks
     */
    public float animationLength(String name) {
        Animation animation = this.animations.get(name);
        return animation != null ? animation.length : 0;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("Animation: ");
        this.animations.forEach((key, anim) -> builder.append(String.format("\n%s = %s", key, anim)));
        return builder.toString();
    }

    public static class Animation {

        public final float length;
        public final boolean loop;
        private final List<AnimationComponent> components = new ArrayList<>();

        public Animation(JsonObject json) {
            this.length = (float) (JsonUtils.get(json, "animation_length", 0.0) * 20);
            this.loop = JsonUtils.get(json, "loop", false);
            JsonObject components = JsonUtils.getObj(json, "bones");
            components.entrySet().forEach(e -> this.components.add(new AnimationComponent(e.getKey(), e.getValue().getAsJsonObject())));
        }

        public void animate(ExtendedModel model, int ticker, float partialTicks, float interpolation, boolean mirror) {
            float actualTick = Math.max(ticker - 1 + partialTicks, 0);
            if (this.loop)
                actualTick = actualTick % this.length;
            for (AnimationComponent comp : this.components)
                comp.animate(model, actualTick, interpolation, mirror);
        }

        @Override
        public String toString() {
            return String.format("\nloop: %b, length: %s, components: %s", this.loop, this.length, this.components);
        }
    }

    public static class AnimationComponent {

        private final String name, mirroredName;
        private AnimationValue[] rotations;
        private AnimationValue[] positions;
        private AnimationValue[] scales;

        public AnimationComponent(String name, JsonObject obj) {
            this.name = name;
            this.mirroredName = name.toLowerCase(Locale.ROOT).contains("right") ? name.replace("Right", "Left").replace("right", "left")
                    : name.replace("Left", "Right").replace("left", "right");
            int i = 0;
            if (obj.has("position")) {
                JsonObject position = this.tryGet(obj, "position");
                this.positions = new AnimationValue[position.size()];
                for (Map.Entry<String, JsonElement> e : position.entrySet()) {
                    if (e.getValue() instanceof JsonArray arr) {
                        this.positions[i] = new AnimationValue(Float.parseFloat(e.getKey()) * 20,
                                SimpleAnimationExpression.of(arr.get(0).getAsString()),
                                SimpleAnimationExpression.of(arr.get(1).getAsString()),
                                SimpleAnimationExpression.of(arr.get(2).getAsString()));
                        i++;
                    }
                }
                Arrays.sort(this.positions, Comparator.comparingDouble(arr -> arr.startTick));
            }
            if (obj.has("rotation")) {
                JsonObject rotation = this.tryGet(obj, "rotation");
                this.rotations = new AnimationValue[rotation.size()];
                i = 0;
                for (Map.Entry<String, JsonElement> e : rotation.entrySet()) {
                    if (e.getValue() instanceof JsonArray arr) {
                        this.rotations[i] = new AnimationValue(Float.parseFloat(e.getKey()) * 20,
                                SimpleAnimationExpression.of(arr.get(0).getAsString()),
                                SimpleAnimationExpression.of(arr.get(1).getAsString()),
                                SimpleAnimationExpression.of(arr.get(2).getAsString()));
                        i++;
                    }
                }
                Arrays.sort(this.rotations, Comparator.comparingDouble(arr -> arr.startTick));
            }
            if (obj.has("scale")) {
                JsonObject scale = this.tryGet(obj, "scale");
                this.scales = new AnimationValue[scale.size()];
                i = 0;
                for (Map.Entry<String, JsonElement> e : scale.entrySet()) {
                    if (e.getValue() instanceof JsonArray arr) {
                        this.scales[i] = new AnimationValue(Float.parseFloat(e.getKey()) * 20,
                                SimpleAnimationExpression.of(arr.get(0).getAsString()),
                                SimpleAnimationExpression.of(arr.get(1).getAsString()),
                                SimpleAnimationExpression.of(arr.get(2).getAsString()));
                        i++;
                    }
                }
                Arrays.sort(this.scales, Comparator.comparingDouble(arr -> arr.startTick));
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

        public void animate(ExtendedModel model, float actualTick, float interpolation, boolean mirror) {
            ModelPartHandler.ModelPartExtended modelPart = model.getHandler().getPartNullable(this.name);
            if (mirror) {
                //Try getting the mirrored modelpart
                ModelPartHandler.ModelPartExtended mirrored = model.getHandler().getPartNullable(this.mirroredName);
                if (mirrored != null)
                    modelPart = mirrored;
            }
            if (modelPart == null)
                return;
            float secTime = actualTick * 0.05f;
            float mirrorMult = (mirror ? -1 : 1);
            if (this.positions != null) {
                if (this.positions.length == 1) {
                    modelPart.x += this.positions[0].getXVal(secTime) * interpolation * mirrorMult;
                    modelPart.y -= this.positions[0].getYVal(secTime) * interpolation;
                    modelPart.z += this.positions[0].getZVal(secTime) * interpolation;
                } else {
                    int id = 1;
                    AnimationValue pos = this.positions[id];
                    while (pos.startTick < actualTick && ++id < this.positions.length)
                        pos = this.positions[id];
                    AnimationValue posPrev = this.positions[id - 1];
                    float prog = Mth.clamp((actualTick - posPrev.startTick) / (pos.startTick - posPrev.startTick), 0F, 1F);
                    modelPart.x += this.interpolate(posPrev.getXVal(secTime), pos.getXVal(secTime), prog) * interpolation * mirrorMult;
                    modelPart.y -= this.interpolate(posPrev.getYVal(secTime), pos.getYVal(secTime), prog) * interpolation;
                    modelPart.z += this.interpolate(posPrev.getZVal(secTime), pos.getZVal(secTime), prog) * interpolation;
                }
            }
            if (this.rotations != null) {
                if (this.rotations.length == 1) {
                    modelPart.xRot += Mth.DEG_TO_RAD * this.rotations[0].getXVal(secTime) * interpolation;
                    modelPart.yRot += Mth.DEG_TO_RAD * this.rotations[0].getYVal(secTime) * interpolation * mirrorMult;
                    modelPart.zRot += Mth.DEG_TO_RAD * this.rotations[0].getZVal(secTime) * interpolation * mirrorMult;
                } else {
                    int id = 1;
                    AnimationValue rot = this.rotations[id];
                    while (rot.startTick < actualTick && ++id < this.rotations.length)
                        rot = this.rotations[id];
                    AnimationValue rotPrev = this.rotations[id - 1];
                    float prog = Mth.clamp((actualTick - rotPrev.startTick) / (rot.startTick - rotPrev.startTick), 0F, 1F);
                    modelPart.xRot += Mth.DEG_TO_RAD * this.interpolate(rotPrev.getXVal(secTime), rot.getXVal(secTime), prog) * interpolation;
                    modelPart.yRot += Mth.DEG_TO_RAD * this.interpolate(rotPrev.getYVal(secTime), rot.getYVal(secTime), prog) * interpolation * mirrorMult;
                    modelPart.zRot += Mth.DEG_TO_RAD * this.interpolate(rotPrev.getZVal(secTime), rot.getZVal(secTime), prog) * interpolation * mirrorMult;
                }
            }
            if (this.scales != null) {
                if (this.scales.length == 1) {
                    modelPart.xScale += (this.scales[0].getXVal(secTime) - 1) * interpolation;
                    modelPart.yScale += (this.scales[0].getYVal(secTime) - 1) * interpolation;
                    modelPart.zScale += (this.scales[0].getZVal(secTime) - 1) * interpolation;
                } else {
                    int id = 1;
                    AnimationValue scale = this.scales[id];
                    while (scale.startTick < actualTick && ++id < this.scales.length)
                        scale = this.scales[id];
                    AnimationValue scalePrev = this.scales[id - 1];
                    float prog = Mth.clamp((actualTick - scalePrev.startTick) / (scale.startTick - scalePrev.startTick), 0F, 1F);
                    modelPart.xScale += this.interpolate(scalePrev.getXVal(secTime) - 1, scale.getXVal(secTime) - 1, prog) * interpolation;
                    modelPart.yScale += this.interpolate(scalePrev.getYVal(secTime) - 1, scale.getYVal(secTime) - 1, prog) * interpolation;
                    modelPart.zScale += this.interpolate(scalePrev.getZVal(secTime) - 1, scale.getZVal(secTime) - 1, prog) * interpolation;
                }
            }
        }

        private float interpolate(float start, float end, float progress) {
            return start + (end - start) * progress;
        }

        @Override
        public String toString() {
            return String.format("%s rot: {%s}; pos: {%s}; scale: {%s}", this.name, ArrayUtils.arrayToString(this.rotations),
                    ArrayUtils.arrayToString(this.positions), ArrayUtils.arrayToString(this.scales));
        }
    }
}
