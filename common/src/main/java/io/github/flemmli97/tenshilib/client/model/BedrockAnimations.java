package io.github.flemmli97.tenshilib.client.model;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import io.github.flemmli97.tenshilib.common.entity.AnimatedAction;
import io.github.flemmli97.tenshilib.common.entity.AnimationHandler;
import io.github.flemmli97.tenshilib.common.utils.ArrayUtils;
import io.github.flemmli97.tenshilib.common.utils.math.parser.Expression;
import io.github.flemmli97.tenshilib.common.utils.math.parser.VariableMap;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.DoubleSupplier;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * A Blockbench animation using the free model animation from Blockbench.
 */
public class BedrockAnimations {

    private final Map<String, Animation> animations = new HashMap<>();

    private final VariableMap variables = new VariableMap();

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
        this.doAnimation(model, name, ticker, partialTicks, interpolation, false, false);
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
     * @param add           By default animations overwrite eachother. Setting this to true will instead add the poses ontop
     * @return True if the animation is being played
     */
    public boolean doAnimation(ExtendedModel model, String name, int ticker, float partialTicks, float interpolation, boolean mirror, boolean add) {
        return this.doAnimation(model, name, Math.max(ticker - 1 + partialTicks, 0), interpolation, mirror, add);
    }

    public boolean doAnimation(ExtendedModel model, AnimationHandler<?> handler, float partialTicks) {
        return this.doAnimation(model, handler, partialTicks, false);
    }

    public boolean doAnimation(ExtendedModel model, AnimationHandler<?> handler, float partialTicks, boolean mirror) {
        return this.doAnimation(model, handler, partialTicks, a -> mirror, null);
    }

    public boolean doAnimation(ExtendedModel model, AnimationHandler<?> handler, float partialTicks, @Nullable Predicate<AnimatedAction> mirror, @Nullable Function<AnimatedAction, String> animationID) {
        AnimatedAction current = handler.getAnimation();
        AnimatedAction last = handler.getLastAnimation();
        float interpolationLast = handler.getLastTransitionProgress(partialTicks);
        float interpolation = handler.getCurrentTransitionProgress(partialTicks);
        boolean changed = false;
        if (last != null && interpolationLast > 0) {
            changed = this.doAnimation(model, animationID != null ? animationID.apply(last) : last.getClientIdentifier(), last.getTick(partialTicks), interpolationLast, mirror != null && mirror.test(last), false);
        }
        if (current != null) {
            if (this.doAnimation(model, animationID != null ? animationID.apply(current) : current.getClientIdentifier(), current.getTick(partialTicks), interpolation, mirror != null && mirror.test(last), false) && !changed) {
                changed = true;
            }
        }
        return changed;
    }

    public boolean doAnimation(ExtendedModel model, String name, float tick, float interpolation, boolean mirror, boolean add) {
        Animation animation = this.animations.get(name);
        if (animation != null && interpolation != 0) {
            animation.animate(model, tick, Mth.clamp(interpolation, 0, 1), this.variables, mirror, add);
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

    public void setVariable(String variable, DoubleSupplier value) {
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

        private final List<AnimationComponent> components = new ArrayList<>();

        public Animation(JsonObject json) {
            this.length = GsonHelper.getAsFloat(json, "animation_length", 0.0f) * 20;
            this.loop = GsonHelper.getAsBoolean(json, "loop", false);
            JsonObject components = GsonHelper.getAsJsonObject(json, "bones", new JsonObject());
            components.entrySet().forEach(e -> this.components.add(new AnimationComponent(e.getKey(), e.getValue().getAsJsonObject())));
        }

        public void animate(ExtendedModel model, float tick, float interpolation, VariableMap vars, boolean mirror, boolean add) {
            if (this.loop && this.length > 0)
                tick = tick % this.length;
            for (AnimationComponent comp : this.components)
                comp.animate(model, tick, vars, interpolation, mirror, add);
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
                                Expression.of(arr.get(0).getAsString()),
                                Expression.of(arr.get(1).getAsString()),
                                Expression.of(arr.get(2).getAsString()));
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
                                Expression.of(arr.get(0).getAsString()),
                                Expression.of(arr.get(1).getAsString()),
                                Expression.of(arr.get(2).getAsString()));
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
                                Expression.of(arr.get(0).getAsString()),
                                Expression.of(arr.get(1).getAsString()),
                                Expression.of(arr.get(2).getAsString()));
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

        public void animate(ExtendedModel model, float actualTick, VariableMap vars, float interpolation, boolean mirror, boolean add) {
            ModelPartsHolder.ModelPartExtended modelPart = model.getHandler().getPartNullable(this.name);
            if (mirror) {
                ModelPartsHolder.ModelPartExtended mirrored = model.getHandler().getPartNullable(this.mirroredName);
                if (mirrored != null)
                    modelPart = mirrored;
            }
            if (modelPart == null)
                return;
            vars.setVariable("query.anim_time", () -> actualTick * 0.05);
            float mirrorMult = (mirror ? -1 : 1);
            if (this.positions != null) {
                if (this.positions.length == 1) {
                    float x = this.positions[0].getXVal(vars) * mirrorMult;
                    float y = this.positions[0].getYVal(vars);
                    float z = this.positions[0].getZVal(vars);
                    float dX = add ? 0 : modelPart.x - modelPart.getDefaultPose().x;
                    modelPart.x += (x - dX) * interpolation;
                    float dY = add ? 0 : modelPart.y - modelPart.getDefaultPose().y;
                    modelPart.y -= (y + dY) * interpolation;
                    float dZ = add ? 0 : modelPart.z - modelPart.getDefaultPose().z;
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
                    float dX = add ? 0 : modelPart.x - modelPart.getDefaultPose().x;
                    modelPart.x += (x - dX) * interpolation;
                    float dY = add ? 0 : modelPart.y - modelPart.getDefaultPose().y;
                    modelPart.y -= (y + dY) * interpolation;
                    float dZ = add ? 0 : modelPart.z - modelPart.getDefaultPose().z;
                    modelPart.z += (z - dZ) * interpolation;
                }
            }
            if (this.rotations != null) {
                if (this.rotations.length == 1) {
                    float x = wrapDegrees(this.rotations[0].getXVal(vars));
                    float y = wrapDegrees(this.rotations[0].getYVal(vars)) * mirrorMult;
                    float z = wrapDegrees(this.rotations[0].getZVal(vars)) * mirrorMult;
                    float dX = add ? 0 : wrapDegrees(Mth.RAD_TO_DEG * (modelPart.xRot - modelPart.getDefaultPose().xRot));
                    modelPart.xRot += Mth.DEG_TO_RAD * degreeDiff(dX, x) * interpolation;
                    float dY = add ? 0 : wrapDegrees(Mth.RAD_TO_DEG * (modelPart.yRot - modelPart.getDefaultPose().yRot));
                    modelPart.yRot += Mth.DEG_TO_RAD * degreeDiff(dY, y) * interpolation;
                    float dZ = add ? 0 : wrapDegrees(Mth.RAD_TO_DEG * (modelPart.zRot - modelPart.getDefaultPose().zRot));
                    modelPart.zRot += Mth.DEG_TO_RAD * degreeDiff(dZ, z) * interpolation;
                } else {
                    int id = 1;
                    AnimationValue rot = this.rotations[id];
                    while (rot.startTick < actualTick && ++id < this.rotations.length)
                        rot = this.rotations[id];
                    AnimationValue rotPrev = this.rotations[id - 1];
                    float prog = Mth.clamp((actualTick - rotPrev.startTick) / (rot.startTick - rotPrev.startTick), 0F, 1F);
                    float x = add ? 0 : wrapDegrees(this.interpolate(rotPrev.getXVal(vars), rot.getXVal(vars), prog));
                    float y = add ? 0 : wrapDegrees(this.interpolate(rotPrev.getYVal(vars), rot.getYVal(vars), prog)) * mirrorMult;
                    float z = add ? 0 : wrapDegrees(this.interpolate(rotPrev.getZVal(vars), rot.getZVal(vars), prog)) * mirrorMult;
                    float dX = wrapDegrees(Mth.RAD_TO_DEG * (modelPart.xRot - modelPart.getDefaultPose().xRot));
                    modelPart.xRot += Mth.DEG_TO_RAD * degreeDiff(dX, x) * interpolation;
                    float dY = wrapDegrees(Mth.RAD_TO_DEG * (modelPart.yRot - modelPart.getDefaultPose().yRot));
                    modelPart.yRot += Mth.DEG_TO_RAD * degreeDiff(dY, y) * interpolation;
                    float dZ = wrapDegrees(Mth.RAD_TO_DEG * (modelPart.zRot - modelPart.getDefaultPose().zRot));
                    modelPart.zRot += Mth.DEG_TO_RAD * degreeDiff(dZ, z) * interpolation;
                }
            }
            if (this.scales != null) {
                if (this.scales.length == 1) {
                    float x = this.scales[0].getXVal(vars) - modelPart.getDefaultPose().xScale;
                    float y = this.scales[0].getYVal(vars) - modelPart.getDefaultPose().yScale;
                    float z = this.scales[0].getZVal(vars) - modelPart.getDefaultPose().zScale;
                    float dX = add ? 0 : modelPart.xScale - modelPart.getDefaultPose().xScale;
                    modelPart.xScale += (x - dX) * interpolation;
                    float dY = add ? 0 : modelPart.yScale - modelPart.getDefaultPose().yScale;
                    modelPart.yScale += (y - dY) * interpolation;
                    float dZ = add ? 0 : modelPart.zScale - modelPart.getDefaultPose().zScale;
                    modelPart.zScale += (z - dZ) * interpolation;
                } else {
                    int id = 1;
                    AnimationValue scale = this.scales[id];
                    while (scale.startTick < actualTick && ++id < this.scales.length)
                        scale = this.scales[id];
                    AnimationValue scalePrev = this.scales[id - 1];
                    float prog = Mth.clamp((actualTick - scalePrev.startTick) / (scale.startTick - scalePrev.startTick), 0F, 1F);
                    float x = this.interpolate(scalePrev.getXVal(vars), scale.getXVal(vars), prog) - modelPart.getDefaultPose().xScale;
                    float y = this.interpolate(scalePrev.getYVal(vars), scale.getYVal(vars), prog) - modelPart.getDefaultPose().yScale;
                    float z = this.interpolate(scalePrev.getZVal(vars), scale.getZVal(vars), prog) - modelPart.getDefaultPose().zScale;
                    float dX = add ? 0 : modelPart.xScale - modelPart.getDefaultPose().xScale;
                    modelPart.xScale += (x - dX) * interpolation;
                    float dY = add ? 0 : modelPart.yScale - modelPart.getDefaultPose().yScale;
                    modelPart.yScale += (y - dY) * interpolation;
                    float dZ = add ? 0 : modelPart.zScale - modelPart.getDefaultPose().zScale;
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
}
