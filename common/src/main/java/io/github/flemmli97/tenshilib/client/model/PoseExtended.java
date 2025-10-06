package io.github.flemmli97.tenshilib.client.model;

import net.minecraft.client.model.geom.PartPose;

public class PoseExtended {

    public static final PoseExtended ZERO = new PoseExtended(0, 0, 0, 0, 0, 0, 0, 0, 0);

    public final float x;
    public final float y;
    public final float z;
    public final float xRot;
    public final float yRot;
    public final float zRot;
    public final float xScale, yScale, zScale;

    public PoseExtended(PartPose pose) {
        this(pose.x, pose.y, pose.z, pose.xRot, pose.yRot, pose.zRot, 1, 1, 1);
    }

    public PoseExtended(PartPose pose, float xScale, float yScale, float zScale) {
        this(pose.x, pose.y, pose.z, pose.xRot, pose.yRot, pose.zRot, xScale, yScale, zScale);
    }

    public PoseExtended(float x, float y, float z, float xRot, float yRot, float zRot, float xScale, float yScale, float zScale) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.xRot = xRot;
        this.yRot = yRot;
        this.zRot = zRot;
        this.xScale = xScale;
        this.yScale = yScale;
        this.zScale = zScale;
    }

    public PoseExtended withTranslation(float x, float y, float z) {
        return new PoseExtended(x, y, z, this.xRot, this.yRot, this.zRot, this.xScale, this.yScale, this.zScale);
    }

    public PoseExtended withRotation(float xRot, float yRot, float zRot) {
        return new PoseExtended(this.x, this.y, this.z, xRot, yRot, zRot, this.xScale, this.yScale, this.zScale);
    }

    public PoseExtended withScale(float xScale, float yScale, float zScale) {
        return new PoseExtended(this.x, this.y, this.z, this.xRot, this.yRot, this.zRot, xScale, yScale, zScale);
    }

    public PartPose asPartPose() {
        return PartPose.offsetAndRotation(this.x, this.y, this.z, this.xRot, this.yRot, this.zRot);
    }

    @Override
    public String toString() {
        return String.format("Pose: [x:%s, y:%s z:%s, xRot: %s, yRot: %s, zRot: %s, xScale: %s, yScale: %s, zScale: %s]", this.x, this.y, this.z,
                this.xRot, this.yRot, this.zRot, this.xScale, this.yScale, this.zScale);
    }
}
