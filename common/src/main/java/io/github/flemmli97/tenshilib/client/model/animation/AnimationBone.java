package io.github.flemmli97.tenshilib.client.model.animation;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import io.github.flemmli97.tenshilib.client.model.animation.keyframe.BoneKeyFrame;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public record AnimationBone(String name, String mirroredName, List<BoneKeyFrame> translations,
                            List<BoneKeyFrame> rotations,
                            List<BoneKeyFrame> scales) {

    public AnimationBone(String name, String mirroredName, List<BoneKeyFrame> translations, List<BoneKeyFrame> rotations, List<BoneKeyFrame> scales) {
        this.name = name;
        this.mirroredName = mirroredName;
        this.translations = List.copyOf(translations);
        this.rotations = List.copyOf(rotations);
        this.scales = List.copyOf(scales);
    }

    public static Map<String, AnimationBone> parseBones(JsonObject obj) {
        Map<String, AnimationBone> bones = new HashMap<>();
        obj.asMap().forEach((name, val) -> {
            JsonObject boneObj = val.getAsJsonObject();
            String mirrored = name.toLowerCase(Locale.ROOT).contains("right") ? name.replace("Right", "Left").replace("right", "left")
                    : name.replace("Left", "Right").replace("left", "right");
            bones.put(name, new AnimationBone(name, mirrored,
                    AnimationBone.parseBoneFrames(boneObj.get("position")),
                    AnimationBone.parseBoneFrames(boneObj.get("rotation")),
                    AnimationBone.parseBoneFrames(boneObj.get("scale"))));
        });
        return Map.copyOf(bones);
    }

    private static List<BoneKeyFrame> parseBoneFrames(JsonElement element) {
        if (element == null)
            return List.of();
        List<BoneKeyFrame> frames = new ArrayList<>();
        String[] simple = tryParseFrameData(element);
        if (simple != null) {
            frames.add(new BoneKeyFrame(0, simple[0], simple[1], simple[2]));
        } else if (element instanceof JsonObject obj) {
            obj.asMap().forEach((timeString, val) -> {
                double time = Double.parseDouble(timeString) * 20;
                String[] vals = tryParseFrameData(val);
                if (vals != null) {
                    frames.add(new BoneKeyFrame(time, vals[0], vals[1], vals[2]));
                } else if (val instanceof JsonObject data) {
                    String[] pre = tryParseFrameData(data.get("pre"));
                    String[] post = tryParseFrameData(data.get("post"));
//                  String lerp = GsonHelper.getAsString(data, "lerp_mode"); Sometimes in future maybe...
                    if (pre != null) {
                        frames.add(new BoneKeyFrame(time - 0.001, pre[0], pre[1], pre[2]));
                    }
                    if (post != null) {
                        frames.add(new BoneKeyFrame(time, post[0], post[1], post[2]));
                    }
                }
            });
        }
        frames.sort(null);
        return frames;
    }

    private static String[] tryParseFrameData(JsonElement element) {
        if (element.isJsonPrimitive()) {
            return new String[]{element.getAsString(), element.getAsString(), element.getAsString()};
        } else if (element instanceof JsonArray arr) {
            return new String[]{arr.get(0).getAsString(), arr.get(1).getAsString(), arr.get(2).getAsString()};
        }
        return null;
    }
}
