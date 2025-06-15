package io.github.flemmli97.tenshilib.common.entity.ai;

import net.minecraft.util.Mth;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.MoveControl;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.level.pathfinder.NodeEvaluator;
import net.minecraft.world.level.pathfinder.PathType;
import net.minecraft.world.level.pathfinder.PathfindingContext;
import net.minecraft.world.phys.Vec3;

/**
 * Made it so entities jump up blocks during strafing.
 * Also stops strafing when the action is not Action#STRAFE
 * which makes strafing entitys look weird when they stop to attack
 */
public class MoveControllerPlus extends MoveControl {

    public MoveControllerPlus(Mob entity) {
        super(entity);
    }

    @Override
    public void tick() {
        if (this.operation == Operation.STRAFE) {
            float speed = (float) (this.mob.getAttributeValue(Attributes.MOVEMENT_SPEED) * this.speedModifier);
            float forward = this.strafeForwards;
            float right = this.strafeRight;
            float len = Mth.sqrt(forward * forward + right * right);
            if (len < 1.0F) {
                len = 1.0F;
            }
            len = speed / len;
            forward *= len;
            right *= len;

            Vec3 target = new Vec3(right, 0, forward).normalize().scale(this.mob.getBbWidth() + 0.3)
                    .yRot(-this.mob.getYRot() * Mth.DEG_TO_RAD);
            PathNavigation pathnavigate = this.mob.getNavigation();
            PathfindingContext ctx = new PathfindingContext(this.mob.level(), this.mob);
            NodeEvaluator nodeprocessor = pathnavigate.getNodeEvaluator();
            int x = Mth.floor(this.mob.getX() + target.x());
            int y = Mth.floor(this.mob.getY());
            int z = Mth.floor(this.mob.getZ() + target.z());
            PathType node = nodeprocessor.getPathType(ctx, x, y, z);
            if (node == PathType.BLOCKED) {
                int yAdd = 0;
                while (yAdd < this.mob.maxUpStep()) {
                    yAdd++;
                    node = nodeprocessor.getPathType(ctx, x, y + yAdd, z);
                    if (node == PathType.WALKABLE) {
                        this.mob.getJumpControl().jump();
                        break;
                    }
                }
            } else if (node != PathType.WALKABLE) {
                this.strafeForwards = 1.0F;
                this.strafeRight = 0.0F;
            }
            this.mob.setSpeed(speed);
            this.mob.setZza(this.strafeForwards);
            this.mob.setXxa(this.strafeRight);
            this.operation = Operation.WAIT;
        } else {
            super.tick();
            this.mob.setXxa(0);
        }
    }

    public Operation currentAction() {
        return this.operation;
    }
}