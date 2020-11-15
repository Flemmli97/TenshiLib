package com.flemmli97.tenshilib.common.entity.ai;

import com.flemmli97.tenshilib.api.entity.IAnimated;
import com.flemmli97.tenshilib.common.entity.AnimatedAction;
import com.flemmli97.tenshilib.common.utils.MathUtils;
import net.minecraft.entity.CreatureEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.RandomPositionGenerator;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;

import java.util.EnumSet;

public abstract class AnimatedAttackGoal<T extends CreatureEntity & IAnimated> extends Goal {

    protected final T attacker;
    protected LivingEntity target;
    protected AnimatedAction next;
    protected String prevAnim = "";
    protected int iddleTime, pathFindDelay;
    protected double distanceToTargetSq;
    protected boolean movementDone;

    public AnimatedAttackGoal(T entity) {
        this.attacker = entity;
        this.setMutexFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    @Override
    public boolean shouldExecute() {
        LivingEntity entitylivingbase = this.attacker.getAttackTarget();
        return entitylivingbase != null && entitylivingbase.isAlive();
    }

    @Override
    public boolean shouldContinueExecuting() {
        LivingEntity living = this.attacker.getAttackTarget();
        return living != null && living.isAlive() && this.attacker.isWithinHomeDistanceFromPosition(living.getBlockPos());
    }

    @Override
    public void resetTask() {
        this.next = null;
        this.target = null;
        this.iddleTime = 0;
        this.movementDone = false;
        this.attacker.getNavigator().clearPath();
        this.attacker.setMoveForward(0);
        this.attacker.setMoveStrafing(0);
        this.prevAnim = "";
        this.pathFindDelay = 0;
    }

    public abstract AnimatedAction randomAttack();

    public abstract void handlePreAttack();

    public abstract void handleAttack(AnimatedAction anim);

    public abstract void handleIddle();

    public abstract int coolDown(AnimatedAction anim);

    public boolean canChooseAttack(AnimatedAction anim) {
        return true;
    }

    public void setupValues() {
        this.target = this.attacker.getAttackTarget();
        this.distanceToTargetSq = this.attacker.getDistanceSq(this.target);
    }

    @Override
    public void tick() {
        AnimatedAction anim = this.attacker.getAnimation();
        this.setupValues();
        --this.pathFindDelay;
        if (anim != null) {
            this.prevAnim = anim.getID();
            this.handleAttack(anim);
        }
        if (this.next == null && anim == null) {
            AnimatedAction choose;
            if (this.iddleTime <= 0 && this.canChooseAttack(choose = this.randomAttack())) {
                this.next = choose;
                this.iddleTime = this.coolDown(this.next);
                this.movementDone = false;
            } else {
                this.handleIddle();
                this.iddleTime--;
            }
        }
        if (this.next != null) {
            this.handlePreAttack();
            if (this.movementDone) {
                if (anim == null)
                    this.attacker.setAnimation(this.next);
                this.next = null;
            }
        }
    }

    protected void moveRandomlyAround() {
        this.moveRandomlyAround(81);
    }

    protected void moveRandomlyAround(double maxDistSq) {
        this.attacker.getLookController().setLookPositionWithEntity(this.target, 30.0f, 30.0f);
        if (this.distanceToTargetSq <= maxDistSq) {
            if (this.attacker.getNavigator().noPath()) {
                Vector3d rand = RandomPositionGenerator.findRandomTarget(this.attacker, 5, 4);
                if (rand != null)
                    this.attacker.getNavigator().tryMoveToXYZ(rand.x, rand.y, rand.z, 1);
            }
        } else
            this.attacker.getNavigator().tryMoveToEntityLiving(this.target, 1.5);
    }

    protected void moveToWithDelay(double x, double y, double z, double speed) {
        if (this.pathFindDelay <= 0) {
            if (!this.attacker.getNavigator().tryMoveToXYZ(x, y, z, speed))
                this.pathFindDelay += 15;
            this.pathFindDelay += this.attacker.getRNG().nextInt(10) + 5;
        }
    }

    protected void moveToWithDelay(double speed) {
        if (this.pathFindDelay <= 0) {
            if (!this.attacker.getNavigator().tryMoveToEntityLiving(this.target, speed))
                this.pathFindDelay += 15;
            this.pathFindDelay += this.attacker.getRNG().nextInt(10) + 5;
        }
    }

    protected BlockPos randomPosAwayFrom(LivingEntity away, float minDis) {
        double angle = Math.random() * 3.141592653589793 * 2.0;
        double x = Math.cos(angle) * minDis;
        double z = Math.sin(angle) * minDis;
        float min = minDis * minDis;
        BlockPos pos = this.attacker.getBlockPos().add(x, 0.0, z);
        if (away.getDistanceSq(Vector3d.ofCenter(pos)) > min && this.attacker.isWithinHomeDistanceFromPosition(pos)) {
            return pos;
        }
        return this.attacker.getBlockPos();
    }

    /**
     * Circle around given point. y coord not needed
     */
    protected void circleAround(double posX, double posZ, float radius, boolean clockWise, float speed) {
        double x = this.attacker.getX() - posX;
        double z = this.attacker.getZ() - posZ;
        double r = x * x + z * z;
        if (r < (radius - 1.5) * (radius - 1.5) || r > (radius + 1.5) * (radius + 1.5)) {
            double[] c = MathUtils.closestOnCircle(posX, posZ, this.attacker.getX(), this.attacker.getZ(), radius);
            this.attacker.getNavigator().tryMoveToXYZ(c[0], this.attacker.getY(), c[1], speed);
        } else {
            double angle = MathUtils.phiFromPoint(posX, posZ, this.attacker.getX(), this.attacker.getZ()) + (clockWise ? MathUtils.degToRad(15) : -MathUtils.degToRad(15));
            double nPosX = radius * Math.cos(angle);
            double nPosZ = radius * Math.sin(angle);
            this.attacker.getNavigator().tryMoveToXYZ(posX + nPosX, this.attacker.getY(), posZ + nPosZ, speed);
        }
    }

    protected void circleAroundTargetFacing(float radius, boolean clockWise, float speed) {
        this.attacker.faceEntity(this.target, 30, 30);
        double x = this.attacker.getX() - this.target.getX();
        double z = this.attacker.getZ() - this.target.getZ();
        double r = x * x + z * z;
        this.attacker.getMoveHelper().strafe(r < (radius - 1.5) * (radius - 1.5) ? -0.5f : r > (radius + 1.5) * (radius + 1.5) ? 0.5f : 0, clockWise ? speed : -speed);
    }

    protected void teleportAround(double posX, double posY, double posZ, int range) {
        double x = posX + (this.attacker.getRNG().nextDouble() - 0.5D) * range * 2;
        double y = posY + (this.attacker.getRNG().nextInt(3));
        double z = posZ + (this.attacker.getRNG().nextDouble() - 0.5D) * range * 2;
        this.attacker.attemptTeleport(x, y, z, false);
    }
}