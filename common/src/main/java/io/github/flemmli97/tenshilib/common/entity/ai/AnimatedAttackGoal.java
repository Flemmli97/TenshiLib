package io.github.flemmli97.tenshilib.common.entity.ai;

import io.github.flemmli97.tenshilib.api.entity.AnimatedAction;
import io.github.flemmli97.tenshilib.api.entity.IAnimated;
import io.github.flemmli97.tenshilib.common.utils.MathUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.util.DefaultRandomPos;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;

public abstract class AnimatedAttackGoal<T extends PathfinderMob & IAnimated> extends Goal {

    protected final T attacker;
    protected LivingEntity target;
    protected AnimatedAction next;
    protected String prevAnim = "";
    protected int iddleTime, pathFindDelay;
    protected double distanceToTargetSq;
    protected boolean movementDone;

    public AnimatedAttackGoal(T entity) {
        this.attacker = entity;
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        LivingEntity living = this.attacker.getTarget();
        return living != null && living.isAlive() && this.attacker.isWithinRestriction(living.blockPosition());
    }

    @Override
    public boolean canContinueToUse() {
        return this.canUse();
    }

    @Override
    public void stop() {
        this.next = null;
        this.target = null;
        this.iddleTime = 0;
        this.movementDone = false;
        this.attacker.getNavigation().stop();
        this.attacker.setZza(0);
        this.attacker.setXxa(0);
        this.prevAnim = "";
        this.pathFindDelay = 0;
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }

    public abstract AnimatedAction randomAttack();

    public abstract void handlePreAttack();

    public abstract void handleAttack(AnimatedAction anim);

    public abstract void handleIddle();

    public abstract int coolDown(AnimatedAction anim);

    public boolean canChooseAttack(AnimatedAction anim) {
        return anim != null;
    }

    public void setupValues() {
        this.target = this.attacker.getTarget();
        this.distanceToTargetSq = this.attacker.distanceToSqr(this.target);
    }

    @Override
    public void tick() {
        if (this.attacker.getTarget() == null)
            return;
        AnimatedAction anim = this.attacker.getAnimationHandler().getAnimation();
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
                    this.attacker.getAnimationHandler().setAnimation(this.next);
                this.next = null;
            }
        }
    }

    protected void moveRandomlyAround() {
        this.moveRandomlyAround(81);
    }

    protected void moveRandomlyAround(double maxDistSq) {
        this.attacker.getLookControl().setLookAt(this.target, 30.0f, 30.0f);
        if (this.distanceToTargetSq <= maxDistSq) {
            if (this.attacker.getNavigation().isDone()) {
                Vec3 rand = DefaultRandomPos.getPos(this.attacker, 5, 4);
                if (rand != null) {
                    this.moveTo(rand.x, rand.y, rand.z, 1);
                }
            }
        } else
            this.moveTo(this.target, 1);
    }

    protected void moveToWithDelay(double x, double y, double z, double speed) {
        if (this.pathFindDelay <= 0) {
            if (!this.moveTo(x, y, z, speed))
                this.pathFindDelay += 15;
            this.pathFindDelay += this.attacker.getRandom().nextInt(10) + 5;
        }
    }

    protected void moveToWithDelay(double speed) {
        if (this.pathFindDelay <= 0) {
            if (!this.moveTo(this.target, speed))
                this.pathFindDelay += 15;
            this.pathFindDelay += this.attacker.getRandom().nextInt(10) + 5;
        }
    }

    private boolean moveTo(double x, double y, double z, double speed) {
        Path path = this.attacker.getNavigation().createPath(x, y, z, 0);
        return path != null && this.attacker.getNavigation().moveTo(path, speed);
    }

    private boolean moveTo(Entity target, double speed) {
        Path path = this.attacker.getNavigation().createPath(target, 0);
        return path != null && this.attacker.getNavigation().moveTo(path, speed);
    }

    protected BlockPos randomPosAwayFrom(LivingEntity away, float minDis) {
        double angle = Math.random() * 3.141592653589793 * 2.0;
        double x = Math.cos(angle) * minDis;
        double z = Math.sin(angle) * minDis;
        float min = minDis * minDis;
        BlockPos pos = this.attacker.blockPosition().offset(x, 0.0, z);
        if (away.distanceToSqr(Vec3.atCenterOf(pos)) > min && this.attacker.isWithinRestriction(pos)) {
            return pos;
        }
        return this.attacker.blockPosition();
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
            this.attacker.getNavigation().moveTo(c[0], this.attacker.getY(), c[1], speed);
        } else {
            double angle = MathUtils.phiFromPoint(posX, posZ, this.attacker.getX(), this.attacker.getZ()) + (clockWise ? MathUtils.degToRad(15) : -MathUtils.degToRad(15));
            double nPosX = radius * Math.cos(angle);
            double nPosZ = radius * Math.sin(angle);
            this.attacker.getNavigation().moveTo(posX + nPosX, this.attacker.getY(), posZ + nPosZ, speed);
        }
    }

    protected void circleAroundTargetFacing(float radius, boolean clockWise, float speed) {
        this.attacker.lookAt(this.target, 30, 30);
        double x = this.attacker.getX() - this.target.getX();
        double z = this.attacker.getZ() - this.target.getZ();
        double r = x * x + z * z;
        this.attacker.getMoveControl().strafe(r < (radius - 1.5) * (radius - 1.5) ? -0.5f : r > (radius + 1.5) * (radius + 1.5) ? 0.5f : 0, clockWise ? speed : -speed);
    }

    protected void teleportAround(double posX, double posY, double posZ, int range) {
        double x = posX + (this.attacker.getRandom().nextDouble() - 0.5D) * range * 2;
        double y = posY + (this.attacker.getRandom().nextInt(3));
        double z = posZ + (this.attacker.getRandom().nextDouble() - 0.5D) * range * 2;
        this.attacker.randomTeleport(x, y, z, false);
    }
}