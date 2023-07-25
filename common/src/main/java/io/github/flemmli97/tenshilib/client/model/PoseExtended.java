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
}
