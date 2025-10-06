package io.github.flemmli97.tenshilib.client.model;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonObject;
import com.mojang.blaze3d.vertex.PoseStack;
import io.github.flemmli97.tenshilib.client.model.animation.Animation;
import io.github.flemmli97.tenshilib.client.model.animation.AnimationBone;
import io.github.flemmli97.tenshilib.client.model.animation.keyframe.BoneKeyFrame;
import io.github.flemmli97.tenshilib.common.entity.animated.AnimationHandler;
import io.github.flemmli97.tenshilib.common.entity.animated.AnimationState;
import io.github.flemmli97.tenshilib.common.utils.math.parser.VariableMap;
import io.github.flemmli97.tenshilib.mixinhelper.EntityRenderDispatcherAccess;
import net.minecraft.client.Minecraft;
import net.minecraft.core.Holder;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.function.DoublePredicate;
import java.util.function.DoubleSupplier;
import java.util.function.Function;
import java.util.function.Predicate;

public class BedrockAnimations {

    public static final Gson GSON = new GsonBuilder().setLenient()
            .registerTypeAdapter(BedrockAnimations.class, deserializer()).create();

    protected final Map<String, Animation> animations;
    private final VariableMap variables = new VariableMap();

    private BedrockAnimations(Map<String, Animation> animations) {
        this.animations = animations;
    }

    private static JsonDeserializer<BedrockAnimations> deserializer() {
        return (json, type, ctx) -> {
            JsonObject obj = json.getAsJsonObject();
            JsonObject animations = GsonHelper.getAsJsonObject(obj, "animations", new JsonObject());
            Map<String, Animation> map = new HashMap<>();
            animations.asMap().forEach((key, e) -> {
                JsonObject animObj = e.getAsJsonObject();
                double length = GsonHelper.getAsDouble(animObj, "animation_length", 0) * 20;
                boolean loop = GsonHelper.getAsBoolean(animObj, "loop", false);
                map.put(key, new Animation(length, loop,
                        AnimationBone.parseBones(GsonHelper.getAsJsonObject(animObj, "bones", new JsonObject())),
                        Animation.parseParticles(GsonHelper.getAsJsonObject(animObj, "particle_effects", new JsonObject())),
                        Animation.parseSound(GsonHelper.getAsJsonObject(animObj, "sound_effects", new JsonObject())),
                        Animation.parseMarkers(GsonHelper.getAsJsonObject(animObj, "timeline", new JsonObject()))));
            });
            return new BedrockAnimations(Map.copyOf(map));
        };
    }

    public static BedrockAnimations empty() {
        return new BedrockAnimations(Map.of());
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

    public boolean doAnimation(ExtendedModel model, AnimationHandler<?> handler, float partialTicks, @Nullable Predicate<AnimationState> mirror, @Nullable Function<AnimationState, String> animationID) {
        AnimationState current = handler.getAnimation();
        AnimationState last = handler.getLastAnimation();
        float interpolationLast = handler.getLastTransitionProgress(partialTicks);
        float interpolation = handler.getCurrentTransitionProgress(partialTicks);
        boolean changed = false;
        if (last != null && interpolationLast > 0) {
            changed = this.doAnimation(model, animationID != null ? animationID.apply(last) : last.getAnimation(), last.getTick(partialTicks), interpolationLast, mirror != null && mirror.test(last), false);
        }
        if (current != null) {
            if (this.doAnimation(model, animationID != null ? animationID.apply(current) : current.getAnimation(), current.getTick(partialTicks), interpolation, mirror != null && mirror.test(current), false) && !changed) {
                changed = true;
            }
        }
        return changed;
    }

    public boolean doAnimation(ExtendedModel model, String name, float tick, float interpolation, boolean mirror, boolean add) {
        Animation animation = this.animations.get(name);
        if (animation != null && interpolation != 0) {
            this.animate(model, animation, tick, Mth.clamp(interpolation, 0, 1), mirror, add);
            return true;
        }
        return false;
    }

    /**
     * Spawn particles or play a sound if the given keyframe is hit
     */
    public void runSpecialEffects(Entity entity, PoseStack stack, String animationID, DoublePredicate applies, ExtendedModel model) {
        Animation animation = this.animations.get(animationID);
        if (animation == null)
            return;
        animation.particleFrames().forEach(frame -> {
            if (applies.test(frame.startTick)) {
                ParticleOptions particle = frame.getParticle(entity.level().registryAccess());
                if (particle != null) {
                    if (frame.locator.isEmpty()) {
                        Minecraft.getInstance().particleEngine.createParticle(particle,
                                entity.getX(), entity.getY(), entity.getZ(), 0, 0, 0);
                    } else {
                        ModelPartsContainer.ModelPartExtended anchor = model.getModel().getPart(frame.locator);
                        if (anchor != null) {
                            stack.pushPose();
                            anchor.translateAndRotateWithParents(stack);
                            Vec3 pos = EntityRenderDispatcherAccess.toWorldPosition(entity, stack.last().pose());
                            Minecraft.getInstance().particleEngine.createParticle(particle,
                                    pos.x(), pos.y(), pos.z(), 0, 0, 0);
                            stack.popPose();
                        }
                    }
                }
            }
        });
        animation.soundFrames().forEach(frame -> {
            if (applies.test(frame.startTick)) {
                Holder<SoundEvent> sound = frame.getSound(entity.level().registryAccess());
                if (sound != null) {
                    Minecraft.getInstance().level
                            .playLocalSound(entity, sound.value(), entity.getSoundSource(), 1, 1);
                }
            }
        });
    }

    /**
     * Run something when a given timeline is hit
     * Do note that this is all client sided
     */
    public void runTimelineEffects(String animationID, String effect, DoublePredicate applies, Runnable run) {
        Animation animation = this.animations.get(animationID);
        if (animation == null)
            return;
        double[] times = animation.markerFrames().get(effect);
        if (times == null)
            return;
        for (double d : times) {
            if (applies.test(d)) {
                run.run();
            }
        }
    }

    /**
     * Animation length in ticks
     */
    public double animationLength(String name) {
        Animation animation = this.animations.get(name);
        return animation != null ? animation.length() : 0;
    }

    public boolean has(String name) {
        return this.animations.containsKey(name);
    }

    public void setVariable(String variable, double value) {
        this.variables.setVariable(variable, value);
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

    // Actually animate the model

    private void animate(ExtendedModel model, Animation animation, float tick, float interpolation, boolean mirror, boolean add) {
        if (animation.loop() && animation.length() > 0)
            tick = (float) (tick % animation.length());
        for (AnimationBone bone : animation.bones().values()) {
            this.animateBone(model, bone, tick, interpolation, mirror, add);
        }
    }

    private void animateBone(ExtendedModel model, AnimationBone bone, float actualTick, float interpolation, boolean mirror, boolean add) {
        ModelPartsContainer.ModelPartExtended modelPart = model.getModel().getPartNullable(bone.name());
        if (mirror) {
            ModelPartsContainer.ModelPartExtended mirrored = model.getModel().getPartNullable(bone.mirroredName());
            if (mirrored != null)
                modelPart = mirrored;
        }
        if (modelPart == null)
            return;
        this.variables.setVariable("query.anim_time", actualTick * 0.05);
        float mirrorMult = (mirror ? -1 : 1);
        if (!bone.translations().isEmpty()) {
            if (bone.translations().size() == 1) {
                float x = bone.translations().getFirst().getXVal(this.variables) * mirrorMult;
                float y = bone.translations().getFirst().getYVal(this.variables);
                float z = bone.translations().getFirst().getZVal(this.variables);
                float dX = add ? 0 : modelPart.x - modelPart.getDefaultPose().x;
                modelPart.x += (x - dX) * interpolation;
                float dY = add ? 0 : modelPart.y - modelPart.getDefaultPose().y;
                modelPart.y -= (y + dY) * interpolation;
                float dZ = add ? 0 : modelPart.z - modelPart.getDefaultPose().z;
                modelPart.z += (z - dZ) * interpolation;
            } else {
                BoneKeyFrame current = bone.translations().getFirst();
                BoneKeyFrame next = current;
                for (int i = 1; i < bone.translations().size() && actualTick >= next.startTick; i++) {
                    current = next;
                    next = bone.translations().get(i);
                }
                float prog = (float) Mth.clamp((actualTick - current.startTick) / (next.startTick - current.startTick), 0F, 1F);
                float x = this.interpolate(current.getXVal(this.variables), next.getXVal(this.variables), prog) * mirrorMult;
                float y = this.interpolate(current.getYVal(this.variables), next.getYVal(this.variables), prog);
                float z = this.interpolate(current.getZVal(this.variables), next.getZVal(this.variables), prog);
                float dX = add ? 0 : modelPart.x - modelPart.getDefaultPose().x;
                modelPart.x += (x - dX) * interpolation;
                float dY = add ? 0 : modelPart.y - modelPart.getDefaultPose().y;
                modelPart.y -= (y + dY) * interpolation;
                float dZ = add ? 0 : modelPart.z - modelPart.getDefaultPose().z;
                modelPart.z += (z - dZ) * interpolation;
            }
        }
        if (!bone.rotations().isEmpty()) {
            if (bone.rotations().size() == 1) {
                float x = wrapDegrees(bone.rotations().getFirst().getXVal(this.variables));
                float y = wrapDegrees(bone.rotations().getFirst().getYVal(this.variables)) * mirrorMult;
                float z = wrapDegrees(bone.rotations().getFirst().getZVal(this.variables)) * mirrorMult;
                float dX = add ? 0 : wrapDegrees(Mth.RAD_TO_DEG * (modelPart.xRot - modelPart.getDefaultPose().xRot));
                modelPart.xRot += Mth.DEG_TO_RAD * degreeDiff(dX, x) * interpolation;
                float dY = add ? 0 : wrapDegrees(Mth.RAD_TO_DEG * (modelPart.yRot - modelPart.getDefaultPose().yRot));
                modelPart.yRot += Mth.DEG_TO_RAD * degreeDiff(dY, y) * interpolation;
                float dZ = add ? 0 : wrapDegrees(Mth.RAD_TO_DEG * (modelPart.zRot - modelPart.getDefaultPose().zRot));
                modelPart.zRot += Mth.DEG_TO_RAD * degreeDiff(dZ, z) * interpolation;
            } else {
                BoneKeyFrame current = bone.rotations().getFirst();
                BoneKeyFrame next = current;
                for (int i = 1; i < bone.rotations().size() && actualTick >= next.startTick; i++) {
                    current = next;
                    next = bone.rotations().get(i);
                }
                float prog = (float) Mth.clamp((actualTick - current.startTick) / (next.startTick - current.startTick), 0F, 1F);
                float x = wrapDegrees(this.interpolate(current.getXVal(this.variables), next.getXVal(this.variables), prog));
                float y = wrapDegrees(this.interpolate(current.getYVal(this.variables), next.getYVal(this.variables), prog)) * mirrorMult;
                float z = wrapDegrees(this.interpolate(current.getZVal(this.variables), next.getZVal(this.variables), prog)) * mirrorMult;
                float dX = add ? 0 : wrapDegrees(Mth.RAD_TO_DEG * (modelPart.xRot - modelPart.getDefaultPose().xRot));
                modelPart.xRot += Mth.DEG_TO_RAD * degreeDiff(dX, x) * interpolation;
                float dY = add ? 0 : wrapDegrees(Mth.RAD_TO_DEG * (modelPart.yRot - modelPart.getDefaultPose().yRot));
                modelPart.yRot += Mth.DEG_TO_RAD * degreeDiff(dY, y) * interpolation;
                float dZ = add ? 0 : wrapDegrees(Mth.RAD_TO_DEG * (modelPart.zRot - modelPart.getDefaultPose().zRot));
                modelPart.zRot += Mth.DEG_TO_RAD * degreeDiff(dZ, z) * interpolation;
            }
        }
        if (!bone.scales().isEmpty()) {
            if (bone.scales().size() == 1) {
                float x = bone.scales().getFirst().getXVal(this.variables) - modelPart.getDefaultPose().xScale;
                float y = bone.scales().getFirst().getYVal(this.variables) - modelPart.getDefaultPose().yScale;
                float z = bone.scales().getFirst().getZVal(this.variables) - modelPart.getDefaultPose().zScale;
                float dX = add ? 0 : modelPart.xScale - modelPart.getDefaultPose().xScale;
                modelPart.xScale += (x - dX) * interpolation;
                float dY = add ? 0 : modelPart.yScale - modelPart.getDefaultPose().yScale;
                modelPart.yScale += (y - dY) * interpolation;
                float dZ = add ? 0 : modelPart.zScale - modelPart.getDefaultPose().zScale;
                modelPart.zScale += (z - dZ) * interpolation;
            } else {
                BoneKeyFrame current = bone.scales().getFirst();
                BoneKeyFrame next = current;
                for (int i = 1; i < bone.scales().size() && actualTick >= next.startTick; i++) {
                    current = next;
                    next = bone.scales().get(i);
                }
                float prog = (float) Mth.clamp((actualTick - current.startTick) / (next.startTick - current.startTick), 0F, 1F);
                float x = this.interpolate(current.getXVal(this.variables), next.getXVal(this.variables), prog) - modelPart.getDefaultPose().xScale;
                float y = this.interpolate(current.getYVal(this.variables), next.getYVal(this.variables), prog) - modelPart.getDefaultPose().yScale;
                float z = this.interpolate(current.getZVal(this.variables), next.getZVal(this.variables), prog) - modelPart.getDefaultPose().zScale;
                float dX = add ? 0 : modelPart.xScale - modelPart.getDefaultPose().xScale;
                modelPart.xScale += (x - dX) * interpolation;
                float dY = add ? 0 : modelPart.yScale - modelPart.getDefaultPose().yScale;
                modelPart.yScale += (y - dY) * interpolation;
                float dZ = add ? 0 : modelPart.zScale - modelPart.getDefaultPose().zScale;
                modelPart.zScale += (z - dZ) * interpolation;
            }
        }
    }

    private float interpolate(float start, float end, float progress) {
        return start + (end - start) * progress;
    }
}
