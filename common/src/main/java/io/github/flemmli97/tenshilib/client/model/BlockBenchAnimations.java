package io.github.flemmli97.tenshilib.client.model;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import io.github.flemmli97.tenshilib.api.entity.AnimatedAction;
import io.github.flemmli97.tenshilib.api.entity.AnimationHandler;
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

    private final SimpleAnimationExpression.VariableMap variables = new SimpleAnimationExpression.VariableMap();

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

    public boolean doAnimation(ExtendedModel model, AnimationHandler<?> handler, float partialTicks) {
        return this.doAnimation(model, handler, partialTicks, AnimationHandler.DEFAULT_ADJUST_TIME, false);
    }

    public boolean doAnimation(ExtendedModel model, AnimationHandler<?> handler, float partialTicks, float adjustTime, boolean mirror) {
        AnimatedAction current = handler.getAnimation();
        AnimatedAction last = handler.getLastAnim();
        float interpolation = handler.getInterpolatedAnimationVal(partialTicks, adjustTime);
        float interpolationRev = 1 - interpolation;
        boolean changed = false;
        if (last != null && interpolationRev > 0) {
            changed = this.doAnimation(model, last.getAnimationClient(), last.getTick(), partialTicks, current != null || handler.getTimeSinceLastChange() <= 1 ? 1 : interpolationRev, mirror, InterpolationCheck.END, false);
        }
        if (current != null) {
            changed = this.doAnimation(model, current.getAnimationClient(), current.getTick(), partialTicks, interpolation, mirror, InterpolationCheck.START, changed);
        }
        return changed;
    }

    public boolean doAnimation(ExtendedModel model, String name, int ticker, float partialTicks, float interpolation, boolean mirror) {
        return this.doAnimation(model, name, ticker, partialTicks, interpolation, mirror, InterpolationCheck.NONE, false);
    }

    /**
     * Run the given animation
     *
     * @param model                  The model to run the animation on
     * @param name                   The name of the animation
     * @param ticker                 Animation ticker
     * @param partialTicks           Partial tick for lerping
     * @param interpolation          An interpolation value between 0-1 indicating
     * @param mirror                 If true mirrors the animation. Components with "left"/"right" in their names will be swapped
     * @param check                  What to do with interpolating the animation:
     *                               NONE: interpolation value is always applied
     *                               START: interpolation value is only applied if the animation starts in a non default pose
     *                               END: interpolation value is only applied if the animation ends in a non default pose
     * @param interpolateFromCurrent If difference between this animation and the current model values should be used as interpolation
     * @return True if the animation is being played
     */
    public boolean doAnimation(ExtendedModel model, String name, int ticker, float partialTicks, float interpolation, boolean mirror, InterpolationCheck check,
                               boolean interpolateFromCurrent) {
        Animation animation = this.animations.get(name);
        if (animation != null && interpolation != 0) {
            if (check == InterpolationCheck.END && animation.endsDefault)
                return false;
            if (check == InterpolationCheck.START && animation.startsDefault)
                interpolation = 1;
            animation.animate(model, ticker, partialTicks, Mth.clamp(interpolation, 0, 1), this.variables, mirror, interpolateFromCurrent);
            return true;
        }
        return false;
    }

    /**
     * Animation length in ticks
     */
    public float animationLength(String name) {
        Animation animation = this.animations.get(name);
        return animation != null ? animation.length : 0;
    }

    public void setVariable(String variable, SimpleAnimationExpression.FloatSupplier value) {
        this.variables.setVariable(variable, value);
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
        /**
         * Whether this animation starts/end with a default pose (all 0) to decide whether to interpolate or not
         */
        private boolean startsDefault = true, endsDefault = true;
        private final List<AnimationComponent> components = new ArrayList<>();

        public Animation(JsonObject json) {
            this.length = (float) (JsonUtils.get(json, "animation_length", 0.0) * 20);
            this.loop = JsonUtils.get(json, "loop", false);
            JsonObject components = JsonUtils.getObj(json, "bones");
            components.entrySet().forEach(e -> {
                AnimationComponent component = new AnimationComponent(e.getKey(), this.length, e.getValue().getAsJsonObject());
                this.components.add(component);
                if (this.startsDefault)
                    this.startsDefault = component.isDefaultPose(true, 0);
                if (this.endsDefault)
                    this.endsDefault = component.isDefaultPose(false, this.length);
            });
        }

        public void animate(ExtendedModel model, int ticker, float partialTicks, float interpolation, SimpleAnimationExpression.VariableMap vars, boolean mirror, boolean interpolateFromCurrent) {
            float actualTick = Math.max(ticker - 1 + partialTicks, 0);
            if (this.loop)
                actualTick = actualTick % this.length;
            for (AnimationComponent comp : this.components)
                comp.animate(model, actualTick, vars, interpolation, mirror, interpolateFromCurrent);
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

        public AnimationComponent(String name, float length, JsonObject obj) {
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

        private boolean isDefaultPose(boolean start, float time) {
            SimpleAnimationExpression.VariableMap vars = new SimpleAnimationExpression.VariableMap();
            vars.setVariable("time", () -> time);
            if (!start)
                return (this.positions == null || this.positions.length == 0 || this.isZero(this.positions[this.positions.length - 1], vars))
                        && (this.rotations == null || this.rotations.length == 0 || this.isZero(this.rotations[this.rotations.length - 1], vars))
                        && (this.scales == null || this.scales.length == 0 || this.isZero(this.scales[this.scales.length - 1], vars));
            return (this.positions == null || this.positions.length == 0 || this.isZero(this.positions[0], vars))
                    && (this.rotations == null || this.rotations.length == 0 || this.isZero(this.rotations[0], vars))
                    && (this.scales == null || this.scales.length == 0 || this.isZero(this.scales[0], vars));
        }

        private boolean isZero(AnimationValue value, SimpleAnimationExpression.VariableMap vars) {
            return value.getXVal(vars) == 0 && value.getYVal(vars) == 0 && value.getZVal(vars) == 0;
        }

        private JsonObject tryGet(JsonObject obj, String name) {
            JsonElement el = obj.get(name);
            if (el.isJsonObject())
                return (JsonObject) el;
            else if (el.isJsonArray()) {
                JsonObject val = new JsonObject();
                val.add("0", el);
                return val;
            } else if (el.isJsonPrimitive()) {
                JsonObject val = new JsonObject();
                JsonArray arr = new JsonArray();
                arr.add(el.getAsDouble());
                arr.add(el.getAsDouble());
                arr.add(el.getAsDouble());
                val.add("0", arr);
                return val;
            }
            return null;
        }

        public void animate(ExtendedModel model, float actualTick, SimpleAnimationExpression.VariableMap vars, float interpolation, boolean mirror, boolean interpolateFromCurrent) {
            ModelPartHandler.ModelPartExtended modelPart = model.getHandler().getPartNullable(this.name);
            if (mirror) {
                //Try getting the mirrored modelpart
                ModelPartHandler.ModelPartExtended mirrored = model.getHandler().getPartNullable(this.mirroredName);
                if (mirrored != null)
                    modelPart = mirrored;
            }
            if (modelPart == null)
                return;
            vars.setVariable("time", () -> actualTick * 0.05f);
            float mirrorMult = (mirror ? -1 : 1);
            if (this.positions != null) {
                if (this.positions.length == 1) {
                    float x = this.positions[0].getXVal(vars) * mirrorMult;
                    float y = this.positions[0].getYVal(vars);
                    float z = this.positions[0].getZVal(vars);
                    float dX = modelPart.x - modelPart.defaultPose.x;
                    modelPart.x += (x - dX) * interpolation;
                    float dY = modelPart.y - modelPart.defaultPose.y;
                    modelPart.y -= (y + dY) * interpolation;
                    float dZ = modelPart.z - modelPart.defaultPose.z;
                    modelPart.z += (z - dZ) * interpolation;
                } else {
                    int id = 1;
                    AnimationValue pos = this.positions[id];
                    while (pos.startTick < actualTick && ++id < this.positions.length)
                        pos = this.positions[id];
                    AnimationValue posPrev = this.positions[id - 1];
                    float prog = Mth.clamp((actualTick - posPrev.startTick) / (pos.startTick - posPrev.startTick), 0F, 1F);
                    float x = this.interpolate(posPrev.getXVal(vars), pos.getXVal(vars), prog) * mirrorMult;
                    float y = this.interpolate(posPrev.getYVal(vars), pos.getYVal(vars), prog);
                    float z = this.interpolate(posPrev.getZVal(vars), pos.getZVal(vars), prog);
                    float dX = modelPart.x - modelPart.defaultPose.x;
                    modelPart.x += (x - dX) * interpolation;
                    float dY = modelPart.y - modelPart.defaultPose.y;
                    modelPart.y -= (y + dY) * interpolation;
                    float dZ = modelPart.z - modelPart.defaultPose.z;
                    modelPart.z += (z - dZ) * interpolation;
                }
            }
            if (this.rotations != null) {
                if (this.rotations.length == 1) {
                    float x = this.rotations[0].getXVal(vars) % 360;
                    float y = (this.rotations[0].getYVal(vars) % 360) * mirrorMult;
                    float z = (this.rotations[0].getZVal(vars) % 360) * mirrorMult;
                    float dX = Mth.RAD_TO_DEG * (modelPart.xRot - modelPart.defaultPose.xRot) % 360;
                    modelPart.xRot += Mth.DEG_TO_RAD * (x - dX) * interpolation;
                    float dY = Mth.RAD_TO_DEG * (modelPart.yRot - modelPart.defaultPose.yRot) % 360;
                    modelPart.yRot += Mth.DEG_TO_RAD * (y - dY) * interpolation;
                    float dZ = Mth.RAD_TO_DEG * (modelPart.zRot - modelPart.defaultPose.zRot) % 360;
                    modelPart.zRot += Mth.DEG_TO_RAD * (z - dZ) * interpolation;
                } else {
                    int id = 1;
                    AnimationValue rot = this.rotations[id];
                    while (rot.startTick < actualTick && ++id < this.rotations.length)
                        rot = this.rotations[id];
                    AnimationValue rotPrev = this.rotations[id - 1];
                    float prog = Mth.clamp((actualTick - rotPrev.startTick) / (rot.startTick - rotPrev.startTick), 0F, 1F);
                    float x = wrapDegrees(this.interpolate(rotPrev.getXVal(vars), rot.getXVal(vars), prog));
                    float y = wrapDegrees(this.interpolate(rotPrev.getYVal(vars), rot.getYVal(vars), prog)) * mirrorMult;
                    float z = wrapDegrees(this.interpolate(rotPrev.getZVal(vars), rot.getZVal(vars), prog)) * mirrorMult;
                    float dX = wrapDegrees(Mth.RAD_TO_DEG * (modelPart.xRot - modelPart.defaultPose.xRot));
                    modelPart.xRot += Mth.DEG_TO_RAD * degreeDiff(dX, x) * interpolation;
                    float dY = wrapDegrees(Mth.RAD_TO_DEG * (modelPart.yRot - modelPart.defaultPose.yRot));
                    modelPart.yRot += Mth.DEG_TO_RAD * degreeDiff(dY, y) * interpolation;
                    float dZ = wrapDegrees(Mth.RAD_TO_DEG * (modelPart.zRot - modelPart.defaultPose.zRot));
                    modelPart.zRot += Mth.DEG_TO_RAD * degreeDiff(dZ, z) * interpolation;
                }
            }
            if (this.scales != null) {
                if (this.scales.length == 1) {
                    float x = this.scales[0].getXVal(vars) - 1;
                    float y = this.scales[0].getYVal(vars) - 1;
                    float z = this.scales[0].getZVal(vars) - 1;
                    float dX = modelPart.xScale - modelPart.defaultPose.xScale;
                    modelPart.xScale += (x - dX) * interpolation;
                    float dY = modelPart.yScale - modelPart.defaultPose.yScale;
                    modelPart.yScale += (y - dY) * interpolation;
                    float dZ = modelPart.zScale - modelPart.defaultPose.zScale;
                    modelPart.zScale += (z - dZ) * interpolation;
                } else {
                    int id = 1;
                    AnimationValue scale = this.scales[id];
                    while (scale.startTick < actualTick && ++id < this.scales.length)
                        scale = this.scales[id];
                    AnimationValue scalePrev = this.scales[id - 1];
                    float prog = Mth.clamp((actualTick - scalePrev.startTick) / (scale.startTick - scalePrev.startTick), 0F, 1F);
                    float x = this.interpolate(scalePrev.getXVal(vars), scale.getXVal(vars), prog) - 1;
                    float y = this.interpolate(scalePrev.getYVal(vars), scale.getYVal(vars), prog) - 1;
                    float z = this.interpolate(scalePrev.getZVal(vars), scale.getZVal(vars), prog) - 1;
                    float dX = modelPart.xScale - modelPart.defaultPose.xScale;
                    modelPart.xScale += (x - dX) * interpolation;
                    float dY = modelPart.yScale - modelPart.defaultPose.yScale;
                    modelPart.yScale += (y - dY) * interpolation;
                    float dZ = modelPart.zScale - modelPart.defaultPose.zScale;
                    modelPart.zScale += (z - dZ) * interpolation;
                }
            }
        }

        public static float wrapDegrees(float value) {
            float f = value % 360.0F;
            if (f < 0) {
                f += 360.0F;
            }
            return f;
        }

        public static float degreeDiff(float current, float target) {
            float diff = target - current;
            if (diff > 180)
                diff -= 360;
            else if (diff < -180)
                diff += 360;
            return diff;
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

    public enum InterpolationCheck {
        NONE,
        START,
        END
    }
}
