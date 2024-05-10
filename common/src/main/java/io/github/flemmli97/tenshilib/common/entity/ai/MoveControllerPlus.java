package io.github.flemmli97.tenshilib.common.entity.ai;

import net.minecraft.util.Mth;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.MoveControl;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.level.pathfinder.NodeEvaluator;
import net.minecraft.world.level.pathfinder.PathType;
import net.minecraft.world.level.pathfinder.PathfindingContext;

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
            float f = (float) this.mob.getAttributeValue(Attributes.MOVEMENT_SPEED);
            float f1 = (float) this.speedModifier * f;
            float f2 = this.strafeForwards;
            float f3 = this.strafeRight;
            float f4 = Mth.sqrt(f2 * f2 + f3 * f3);
            if (f4 < 1.0F) {
                f4 = 1.0F;
            }

            f4 = f1 / f4;
            f2 = f2 * f4;
            f3 = f3 * f4;
            float f5 = Mth.sin(this.mob.getYRot() * ((float) Math.PI / 180F));
            float f6 = Mth.cos(this.mob.getYRot() * ((float) Math.PI / 180F));
            float f7 = f2 * f6 - f3 * f5;
            float f8 = f3 * f6 + f2 * f5;
            PathNavigation pathnavigate = this.mob.getNavigation();

            if (pathnavigate != null) {
                PathfindingContext ctx = new PathfindingContext(this.mob.level(), this.mob);
                double len = Math.sqrt(f7 * f7 + f8 * f8);
                NodeEvaluator nodeprocessor = pathnavigate.getNodeEvaluator();
                int x = Mth.floor(this.mob.getX() + (double) f7 / len);
                int y = Mth.floor(this.mob.getY());
                int z = Mth.floor(this.mob.getZ() + (double) f8 / len);
                PathType node = nodeprocessor != null ? nodeprocessor.getPathType(ctx, x, y, z) : PathType.OPEN;
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
                    f1 = f;
                }
            }
            this.mob.setSpeed(f1);
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