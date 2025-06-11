package io.github.flemmli97.tenshilib.client.model;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDefinition;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.Mth;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

public class BedrockGeometryParser {

    public static final Gson GSON = new GsonBuilder().setLenient()
            .registerTypeAdapter(ModelPartsContainer.class, deserializer())
            .registerTypeAdapter(BedrockGeometry.class, BedrockGeometry.deserializer())
            .registerTypeAdapter(Bone.class, Bone.deserializer())
            .registerTypeAdapter(Cube.class, Cube.deserializer()).create();

    private static JsonDeserializer<ModelPartsContainer> deserializer() {
        return (json, type, ctx) -> {
            BedrockGeometry geometry = ctx.deserialize(json, BedrockGeometry.class);
            return geometry.bake();
        };
    }

    private static Vector3f calculateOrigin(Bone bone, Map<String, Bone> boneMap) {
        Vector3f origin = new Vector3f(bone.pivot());
        boolean root = bone.parent().isEmpty();
        if ((bone = boneMap.get(bone.parent())) != null) {
            origin.sub(bone.pivot());
        }
        return new Vector3f(origin.x(), -origin.y() + (root ? 24 : 0), origin.z());
    }

    private static Vector3f parseVec(JsonElement element) {
        if (element == null || element.isJsonNull())
            return new Vector3f();
        if (element.isJsonPrimitive()) {
            return new Vector3f(element.getAsFloat());
        } else if (element instanceof JsonArray arr) {
            return new Vector3f(arr.get(0).getAsFloat(), arr.get(1).getAsFloat(), arr.get(2).getAsFloat());
        }
        return new Vector3f();
    }

    public record BedrockGeometry(int textureWidth, int textureHeight, List<Bone> bones) {

        private static JsonDeserializer<BedrockGeometry> deserializer() {
            return (json, type, ctx) -> {
                JsonArray geom = GsonHelper.getAsJsonArray(json.getAsJsonObject(), "minecraft:geometry");
                JsonObject obj = geom.get(0).getAsJsonObject();
                JsonObject description = GsonHelper.getAsJsonObject(obj, "description");
                int textureWidth = GsonHelper.getAsInt(description, "texture_width");
                int textureHeight = GsonHelper.getAsInt(description, "texture_height");
                return new BedrockGeometry(textureWidth, textureHeight,
                        Arrays.stream(ctx.<Bone[]>deserialize(GsonHelper.getAsJsonArray(obj, "bones", new JsonArray()), Bone[].class)).toList());
            };
        }

        public ModelPartsContainer bake() {
            Map<String, Bone> boneMap = new HashMap<>();
            this.bones().forEach(b -> boneMap.put(b.name(), b));

            ModelPartBuilder root = new ModelPartBuilder();
            Map<String, ModelPartBuilder> parts = new HashMap<>();
            this.bones().forEach(bone -> this.bakeBone(bone, parts, boneMap, root));
            return new ModelPartsContainer(root.bake(this.textureWidth, this.textureHeight));
        }

        private void bakeBone(Bone bone, Map<String, ModelPartBuilder> map, Map<String, Bone> boneMap, ModelPartBuilder root) {
            CubeListBuilder cubes = CubeListBuilder.create();
            Set<Bone> cubeRotations = new HashSet<>();
            bone.cubes().forEach(cube -> {
                if (!cube.rotation().equals(new Vector3f())) {
                    Bone newBone = cubeRotations.stream().filter(b -> b.rotation().equals(cube.rotation()) && b.pivot().equals(cube.pivot()))
                            .findFirst().orElse(null);
                    if (newBone == null) {
                        newBone = new Bone(bone.name() + "_generated_" + UUID.randomUUID(), bone.name(),
                                bone.mirror(), bone.inflate(), cube.pivot(), cube.rotation(), new ArrayList<>());
                        cubeRotations.add(newBone);
                    }
                    newBone.cubes().add(new Cube(cube.origin(), cube.size(), cube.pivot(), new Vector3f(),
                            cube.mirror(), cube.inflate(), cube.uv()));
                } else {
                    if (cube.uv != null)
                        cubes.texOffs(cube.uv[0], cube.uv[1]);
                    cubes.mirror(cube.mirror());
                    cubes.addBox(cube.origin().x() - bone.pivot().x(),
                            bone.pivot().y() - (cube.origin().y() + cube.size().y()),
                            cube.origin().z() - bone.pivot().z(),
                            cube.size().x(), cube.size().y(), cube.size().z(), new CubeDeformation(cube.inflate));
                }
            });
            ModelPartBuilder build = map.compute(bone.name(), (name, old) -> {
                Vector3f origin = calculateOrigin(bone, boneMap);
                ModelPartBuilder builder = old == null ? new ModelPartBuilder() : old;
                return builder.update(cubes.getCubes(), PartPose.offsetAndRotation(origin.x(), origin.y(), origin.z(),
                        bone.rotation().x() * Mth.DEG_TO_RAD, bone.rotation().y() * Mth.DEG_TO_RAD, bone.rotation().z() * Mth.DEG_TO_RAD));
            });
            if (bone.parent().isEmpty())
                root.addChild(bone.name(), build);
            else
                map.computeIfAbsent(bone.parent(), k -> new ModelPartBuilder())
                        .addChild(bone.name(), build);
            cubeRotations.forEach(b -> this.bakeBone(b, map, boneMap, root));
        }
    }

    public record Bone(String name, String parent, boolean mirror, float inflate, Vector3f pivot, Vector3f rotation,
                       List<Cube> cubes) {

        private static JsonDeserializer<Bone> deserializer() {
            return (json, type, ctx) -> {
                JsonObject obj = json.getAsJsonObject();
                return new Bone(
                        GsonHelper.getAsString(obj, "name"),
                        GsonHelper.getAsString(obj, "parent", ""),
                        GsonHelper.getAsBoolean(obj, "mirror", false),
                        GsonHelper.getAsFloat(obj, "inflate", 0),
                        parseVec(obj.get("pivot")),
                        parseVec(obj.get("rotation")),
                        Arrays.stream(ctx.<Cube[]>deserialize(GsonHelper.getAsJsonArray(obj, "cubes", new JsonArray()), Cube[].class)).toList()
                );
            };
        }
    }

    public record Cube(Vector3f origin, Vector3f size, Vector3f pivot, Vector3f rotation, boolean mirror, float inflate,
                       int[] uv) {

        private static JsonDeserializer<Cube> deserializer() {
            return (json, type, ctx) -> {
                JsonObject obj = json.getAsJsonObject();
                return new Cube(
                        parseVec(obj.get("origin")),
                        parseVec(obj.get("size")),
                        parseVec(obj.get("pivot")),
                        parseVec(obj.get("rotation")),
                        GsonHelper.getAsBoolean(obj, "mirror", false),
                        GsonHelper.getAsFloat(obj, "inflate", 0),
                        parseUV(obj.get("uv"))
                );
            };
        }

        private static int[] parseUV(JsonElement element) {
            if (element == null || element.isJsonNull())
                return null;
            if (element instanceof JsonArray arr) {
                return new int[]{arr.get(0).getAsInt(), arr.get(1).getAsInt()};
            }
            throw new JsonParseException("Only box uv's are currently supported!");
        }
    }

    private static class ModelPartBuilder {

        private List<CubeDefinition> cubes = List.of();
        private PartPose partPose = PartPose.ZERO;
        private final Map<String, ModelPartBuilder> children = Maps.newHashMap();

        public ModelPartBuilder() {
        }

        public ModelPartBuilder update(List<CubeDefinition> cubes, PartPose partPose) {
            this.cubes = cubes;
            this.partPose = partPose;
            return this;
        }

        public void addChild(String name, ModelPartBuilder child) {
            this.children.put(name, child);
        }

        public ModelPart bake(int texWidth, int texHeight) {
            Object2ObjectArrayMap<String, ModelPart> map = this.children.entrySet().stream()
                    .collect(Collectors.toMap(Map.Entry::getKey, entry -> entry.getValue().bake(texWidth, texHeight),
                            (modelPart, modelPart2) -> modelPart, Object2ObjectArrayMap::new));
            List<ModelPart.Cube> cubes = this.cubes.stream().map(cubeDefinition -> cubeDefinition.bake(texWidth, texHeight)).collect(ImmutableList.toImmutableList());
            ModelPart modelPart3 = new ModelPart(cubes, map);
            modelPart3.setInitialPose(this.partPose);
            modelPart3.loadPose(this.partPose);
            return modelPart3;
        }
    }
}
