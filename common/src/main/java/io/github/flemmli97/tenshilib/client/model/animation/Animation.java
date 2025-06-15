package io.github.flemmli97.tenshilib.client.model.animation;

import com.google.common.collect.ImmutableMap;
import com.google.gson.JsonObject;
import io.github.flemmli97.tenshilib.client.model.animation.keyframe.ParticleKeyFrame;
import io.github.flemmli97.tenshilib.client.model.animation.keyframe.SoundKeyFrame;
import net.minecraft.util.GsonHelper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public record Animation(double length, boolean loop, Map<String, AnimationBone> bones,
                        List<ParticleKeyFrame> particleFrames, List<SoundKeyFrame> soundFrames,
                        Map<String, double[]> markerFrames) {

    public static List<ParticleKeyFrame> parseParticles(JsonObject obj) {
        List<ParticleKeyFrame> frames = new ArrayList<>();
        obj.asMap().forEach((time, val) -> {
            JsonObject data = val.getAsJsonObject();
            frames.add(new ParticleKeyFrame(Double.parseDouble(time) * 20,
                    GsonHelper.getAsString(data, "effect", ""),
                    GsonHelper.getAsString(data, "locator", "")));
        });
        frames.sort(null);
        return List.copyOf(frames);
    }

    public static List<SoundKeyFrame> parseSound(JsonObject obj) {
        List<SoundKeyFrame> frames = new ArrayList<>();
        obj.asMap().forEach((time, val) -> {
            JsonObject data = val.getAsJsonObject();
            frames.add(new SoundKeyFrame(Double.parseDouble(time) * 20,
                    GsonHelper.getAsString(data, "effect", "")));
        });
        frames.sort(null);
        return List.copyOf(frames);
    }

    public static Map<String, double[]> parseMarkers(JsonObject obj) {
        Map<String, List<Double>> frames = new HashMap<>();
        obj.asMap().forEach((time, val) -> {
            String name = val.getAsString();
            if (name.endsWith(";"))
                name = name.substring(0, name.length() - 1);
            frames.computeIfAbsent(name, ok -> new ArrayList<>())
                    .add(Double.parseDouble(time) * 20);
        });
        ImmutableMap.Builder<String, double[]> map = ImmutableMap.builder();
        frames.forEach((name, vals) -> {
            vals.sort(null);
            map.put(name, vals.stream().mapToDouble(d -> d).toArray());
        });
        return map.build();
    }
}
