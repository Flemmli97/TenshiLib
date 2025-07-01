package io.github.flemmli97.tenshilib.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import io.github.flemmli97.tenshilib.mixin.ModelPartAccessor;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Random;
import java.util.stream.Collectors;

public class ModelPartsContainer {

    private final ModelPartExtended root;
    private final Map<String, ModelPartExtended> childrenToName = new HashMap<>();

    public ModelPartsContainer(ModelPart root) {
        this.root = new ModelPartExtended("model_root", null, root);
        this.childrenToName.put("model_root", this.root);
        this.root.getMappedParts(this.childrenToName);
    }

    public ModelPartExtended getPart(String name) {
        ModelPartExtended modelPart = this.childrenToName.get(name);
        if (modelPart == null) {
            throw new NoSuchElementException("Can't find part " + name);
        } else {
            return modelPart;
        }
    }

    public Optional<ModelPartExtended> getOptionalPart(String name) {
        return Optional.ofNullable(this.childrenToName.get(name));
    }

    /**
     * Used only in animation
     */
    protected ModelPartExtended getPartNullable(String name) {
        return this.childrenToName.get(name);
    }

    public void resetPoses() {
        this.root.resetAll();
    }

    /**
     * @return The root part containing all other parts. This part usually does not have any transformations applied!
     */
    public ModelPartExtended getRoot() {
        return this.root;
    }

    public static class ModelPartExtended {

        private final String name;
        private final ModelPartExtended parent;
        public float x, y, z;
        public float xRot, yRot, zRot;
        public float xScale = 1, yScale = 1, zScale = 1;
        public boolean visible = true;
        private final List<ModelPart.Cube> cubes;
        private final Map<String, ModelPartExtended> children;

        private PoseExtended defaultPose;

        public ModelPartExtended(String name, ModelPartExtended parent, ModelPart orig) {
            this.name = name;
            this.parent = parent;
            this.cubes = ((ModelPartAccessor) (Object) orig).getCubes();
            this.children = ((ModelPartAccessor) (Object) orig).getChildren()
                    .entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, e -> new ModelPartExtended(e.getKey(), this, e.getValue())));
            this.updateDefaultPose(new PoseExtended(orig.storePose()));
        }

        public PartPose storePose() {
            return PartPose.offsetAndRotation(this.x, this.y, this.z, this.xRot, this.yRot, this.zRot);
        }

        public PoseExtended extendedPose() {
            return new PoseExtended(this.x, this.y, this.z, this.xRot, this.yRot, this.zRot, this.xScale, this.yScale, this.zScale);
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

        public void loadPoseRecursive(PoseExtended pose) {
            this.setPos(pose.x, pose.y, pose.z);
            this.setRotation(pose.xRot, pose.yRot, pose.zRot);
            this.setScale(pose.xScale, pose.yScale, pose.zScale);
            this.children.values().forEach(m -> m.loadPoseRecursive(pose));
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
            this.loadPose(this.getDefaultPose());
        }

        public void resetAll() {
            this.reset();
            this.children.values().forEach(ModelPartExtended::resetAll);
        }

        public void setAllVisible(boolean visible) {
            this.visible = visible;
            this.children.values().forEach(m -> m.setAllVisible(visible));
        }

        public void render(PoseStack poseStack, VertexConsumer vertexConsumer, int i, int j) {
            this.render(poseStack, vertexConsumer, i, j, 0xFFFFFFFF);
        }

        public void render(PoseStack poseStack, VertexConsumer vertexConsumer, int i, int j, int color) {
            if (this.visible) {
                if (!this.cubes.isEmpty() || !this.children.isEmpty()) {
                    poseStack.pushPose();
                    this.translateAndRotate(poseStack);
                    this.compile(poseStack.last(), vertexConsumer, i, j, color);
                    for (ModelPartExtended modelPart : this.children.values()) {
                        modelPart.render(poseStack, vertexConsumer, i, j, color);
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

        public void translateAndRotateWithParents(PoseStack poseStack) {
            this.translateAndRotateWithParents(poseStack, false);
        }

        public void translateAndRotateWithParents(PoseStack poseStack, boolean excludeSelf) {
            ModelPartExtended part = excludeSelf ? this.parent : this;
            if (part == null)
                return;
            List<ModelPartExtended> parts = new ArrayList<>();
            parts.add(part);
            while (part.parent != null) {
                part = part.parent;
                parts.add(part);
            }
            for (int i = parts.size() - 1; i >= 0; i--) {
                parts.get(i).translateAndRotate(poseStack);
            }
        }

        public void translateAndRotate(PoseStack poseStack) {
            poseStack.translate(this.x / 16.0F, this.y / 16.0F, this.z / 16.0F);

            if (this.zRot != 0.0F)
                poseStack.mulPose(Axis.ZP.rotation(this.zRot));
            if (this.yRot != 0.0F)
                poseStack.mulPose(Axis.YP.rotation(this.yRot));
            if (this.xRot != 0.0F)
                poseStack.mulPose(Axis.XP.rotation(this.xRot));

            if (this.xScale != 1 || this.yScale != 1 || this.zScale != 1)
                poseStack.scale(this.xScale, this.yScale, this.zScale);
        }

        private void compile(PoseStack.Pose pose, VertexConsumer vertexConsumer, int i, int j, int color) {
            for (ModelPart.Cube cube : this.cubes) {
                cube.compile(pose, vertexConsumer, i, j, color);
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
                if (map.containsKey(key))
                    throw new IllegalStateException("Part with name " + key + " already exists!");
                map.put(key, value);
                value.getMappedParts(map);
            });
        }

        public PoseExtended getDefaultPose() {
            return this.defaultPose;
        }

        public void updateDefaultPose(PoseExtended defaultPose) {
            this.defaultPose = defaultPose;
        }

        @Override
        public String toString() {
            return String.format("Part: %s, Pos:[%s,%s,%s] - Rot:[%s,%s,%s] - Scale[%s,%s,%s]", this.name,
                    this.x, this.y, this.z, this.xRot, this.yRot, this.zRot, this.xScale, this.yScale, this.zScale);
        }
    }
}
