package io.github.flemmli97.tenshilib.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Vector3f;
import io.github.flemmli97.tenshilib.mixin.ModelPartAccessor;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Random;
import java.util.stream.Collectors;

public class ModelPartHandler {

    private final ModelPartExtended mainPart;
    private final Map<String, ModelPartExtended> childrenToName = new HashMap<>();

    public ModelPartHandler(ModelPart main, String mainID) {
        this.mainPart = new ModelPartExtended(main);
        this.childrenToName.put(mainID, this.mainPart);
        this.mainPart.getMappedParts(this.childrenToName);
    }

    public ModelPartExtended getPart(String name) {
        ModelPartExtended modelPart = this.childrenToName.get(name);
        if (modelPart == null) {
            throw new NoSuchElementException("Can't find part " + name);
        } else {
            return modelPart;
        }
    }

    public void resetPoses() {
        this.mainPart.resetAll();
    }

    public ModelPartExtended getMainPart() {
        return this.mainPart;
    }

    public static class ModelPartExtended {
        public float x, y, z;
        public float xRot, yRot, zRot;
        public float xScale = 1, yScale = 1, zScale = 1;
        public boolean visible = true;
        private final List<ModelPart.Cube> cubes;
        private final Map<String, ModelPartExtended> children;

        private final PoseExtended defaultPose;

        public ModelPartExtended(ModelPart orig) {
            this.cubes = ((ModelPartAccessor) (Object) orig).getCubes();
            this.children = ((ModelPartAccessor) (Object) orig).getChildren()
                    .entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, e -> new ModelPartExtended(e.getValue())));
            this.defaultPose = new PoseExtended(orig.storePose());
        }

        public PartPose storePose() {
            return PartPose.offsetAndRotation(this.x, this.y, this.z, this.xRot, this.yRot, this.zRot);
        }

        public void loadPose(PartPose partPose) {
            this.setPos(partPose.x, partPose.y, partPose.z);
            this.setRotation(partPose.xRot, partPose.yRot, partPose.zRot);
        }

        public void loadPose(PoseExtended pose) {
            this.setPos(pose.x, pose.y, pose.z);
            this.setRotation(pose.xRot, pose.yRot, pose.zRot);
            this.setScale(pose.xScale, pose.yScale, pose.zScale);
        }

        public ModelPartExtended getChild(String string) {
            ModelPartExtended modelPart = this.children.get(string);
            if (modelPart == null) {
                throw new NoSuchElementException("Can't find part " + string);
            } else {
                return modelPart;
            }
        }

        public void setPos(float f, float g, float h) {
            this.x = f;
            this.y = g;
            this.z = h;
        }

        public void setRotation(float f, float g, float h) {
            this.xRot = f;
            this.yRot = g;
            this.zRot = h;
        }

        public void setScale(float x, float y, float z) {
            this.xScale = x;
            this.yScale = y;
            this.zScale = z;
        }

        public void reset() {
            this.loadPose(this.defaultPose);
        }

        public void resetAll() {
            this.reset();
            this.children.values().forEach(ModelPartExtended::resetAll);
        }

        public void render(PoseStack poseStack, VertexConsumer vertexConsumer, int i, int j) {
            this.render(poseStack, vertexConsumer, i, j, 1.0F, 1.0F, 1.0F, 1.0F);
        }

        public void render(PoseStack poseStack, VertexConsumer vertexConsumer, int i, int j, float f, float g, float h, float k) {
            if (this.visible) {
                if (!this.cubes.isEmpty() || !this.children.isEmpty()) {
                    poseStack.pushPose();
                    this.translateAndRotate(poseStack);
                    this.compile(poseStack.last(), vertexConsumer, i, j, f, g, h, k);
                    for (ModelPartExtended modelPart : this.children.values()) {
                        modelPart.render(poseStack, vertexConsumer, i, j, f, g, h, k);
                    }

                    poseStack.popPose();
                }
            }
        }

        public void visit(PoseStack poseStack, ModelPart.Visitor visitor) {
            this.visit(poseStack, visitor, "");
        }

        private void visit(PoseStack poseStack, ModelPart.Visitor visitor, String string) {
            if (!this.cubes.isEmpty() || !this.children.isEmpty()) {
                poseStack.pushPose();
                this.translateAndRotate(poseStack);
                PoseStack.Pose pose = poseStack.last();

                for (int i = 0; i < this.cubes.size(); ++i) {
                    visitor.visit(pose, string, i, this.cubes.get(i));
                }

                String string2 = string + "/";
                this.children.forEach((string2x, modelPart) -> modelPart.visit(poseStack, visitor, string2 + string2x));
                poseStack.popPose();
            }
        }

        public void translateAndRotate(PoseStack poseStack) {
            poseStack.translate(this.x / 16.0F, this.y / 16.0F, this.z / 16.0F);

            if (this.zRot != 0.0F)
                poseStack.mulPose(Vector3f.ZP.rotation(this.zRot));
            if (this.yRot != 0.0F)
                poseStack.mulPose(Vector3f.YP.rotation(this.yRot));
            if (this.xRot != 0.0F)
                poseStack.mulPose(Vector3f.XP.rotation(this.xRot));

            if (this.xScale != 1 || this.yScale != 1 || this.zScale != 1)
                poseStack.scale(this.xScale, this.yScale, this.zScale);
        }

        private void compile(PoseStack.Pose pose, VertexConsumer vertexConsumer, int i, int j, float f, float g, float h, float k) {
            for (ModelPart.Cube cube : this.cubes) {
                cube.compile(pose, vertexConsumer, i, j, f, g, h, k);
            }
        }

        public ModelPart.Cube getRandomCube(Random random) {
            return this.cubes.get(random.nextInt(this.cubes.size()));
        }

        public boolean isEmpty() {
            return this.cubes.isEmpty();
        }

        public void getMappedParts(Map<String, ModelPartExtended> map) {
            this.children.forEach((key, value) -> {
                map.put(key, value);
                value.getMappedParts(map);
            });
        }
    }
}
