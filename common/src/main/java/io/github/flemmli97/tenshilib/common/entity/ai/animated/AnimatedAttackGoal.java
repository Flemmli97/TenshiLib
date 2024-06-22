package io.github.flemmli97.tenshilib.common.entity.ai.animated;

import io.github.flemmli97.tenshilib.api.entity.AnimatedAction;
import io.github.flemmli97.tenshilib.api.entity.IAnimated;
import io.github.flemmli97.tenshilib.common.utils.MathUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.util.random.WeightedEntry;
import net.minecraft.util.random.WeightedRandom;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.util.DefaultRandomPos;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.EnumSet;
import java.util.List;

/**
 * Attack goal using a very customizable animated attack system
 */
public class AnimatedAttackGoal<T extends PathfinderMob & IAnimated> extends Goal {

    public final T attacker;
    protected final List<WeightedEntry.Wrapper<GoalAttackAction<T>>> actions;
    protected final List<WeightedEntry.Wrapper<IdleAction<T>>> idleActions;
    protected LivingEntity target;

    @Nullable
    public GoalAttackAction.ActiveAction<T> current;
    protected GoalAttackAction.ActiveAction<T> previous;
    private ActionRun<T> onIdle;
    private boolean reset;

    protected Vec3 lastPathTargetPos;
    protected int idleTime, prepare;
    public double distanceToTargetSq;
    public boolean canSee;

    /**
     * @param actions     A list of weighted goal actions that decide what attack to execute
     * @param idleActions A list of idle actions that runs when the attacks are in cooldown or no matching attack was found.
     */
    public AnimatedAttackGoal(T entity, List<WeightedEntry.Wrapper<GoalAttackAction<T>>> actions, List<WeightedEntry.Wrapper<IdleAction<T>>> idleActions) {
        this.attacker = entity;
        this.actions = actions;
        this.idleActions = idleActions;
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        LivingEntity living = this.attacker.getTarget();
        return !this.actions.isEmpty() && living != null && living.isAlive() && this.attacker.isWithinRestriction(living.blockPosition());
    }

    @Override
    public boolean canContinueToUse() {
        return this.canUse();
    }

    @Override
    public void stop() {
        this.current = null;
        this.previous = null;
        this.target = null;
        this.reset = false;
        this.lastPathTargetPos = null;
        this.idleTime = 0;
        this.prepare = 0;
        this.attacker.getNavigation().stop();
        this.attacker.setZza(0);
        this.attacker.setXxa(0);
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }

    public void setupValues() {
        this.target = this.attacker.getTarget();
        if (this.attacker.tickCount % 10 != 0)
            return;
        this.distanceToTargetSq = this.attacker.distanceToSqr(this.target);
        this.canSee = this.attacker.getSensing().hasLineOfSight(this.target);
    }

    protected void selectNextAction() {
        this.reset = false;
        this.onIdle = null;
        this.lastPathTargetPos = null;
        List<WeightedEntry.Wrapper<IdleAction<T>>> idles = this.idleActions.stream().filter(d -> d.getData().test(this, this.target)).toList();
        IdleAction<T> idle = WeightedRandom.getRandomItem(this.attacker.getRandom(), idles).map(WeightedEntry.Wrapper::getData).orElse(null);
        this.onIdle = idle != null ? idle.runner.create() : null;
        if (idle != null) {
            this.onIdle = idle.runner.create();
            this.idleTime = idle.getDuration().getInt(this.attacker);
        } else {
            this.idleTime = 20;
        }
        List<WeightedEntry.Wrapper<GoalAttackAction<T>>> selectables = this.actions.stream().filter(d -> d.getData().test(this, this.target, this.previous != null ? this.previous.anim().getID() : "")).toList();
        GoalAttackAction<T> action = WeightedRandom.getRandomItem(this.attacker.getRandom(), selectables).map(WeightedEntry.Wrapper::getData).orElse(null);
        this.current = action != null ? action.createActive() : null;
        if (action != null) {
            this.prepare = this.current.start().timeout().getInt(this.attacker);
            this.idleTime = action.getCooldown().getInt(this.attacker);
        }
    }

    @Override
    public void tick() {
        if (this.attacker.getTarget() == null)
            return;
        this.setupValues();
        AnimatedAction anim = this.attacker.getAnimationHandler().getAnimation();
        // This is handled in the entity
        if (anim != null) {
            if (this.current != null && this.current.anim().is(anim))
                this.current.runner().run(this, this.target, anim);
            return;
        } else if (this.reset) {
            this.reset = false;
            this.current = null;
        }
        if (this.current == null && --this.idleTime > 0) {
            if (this.onIdle != null)
                this.onIdle.run(this, this.target, null);
            return;
        }
        if (this.current == null) {
            this.selectNextAction();
            return;
        }

        if (--this.prepare >= 0) {
            boolean done = this.current.start().start(this, this.target);
            if (done)
                this.prepare = -1;
            else if (this.prepare == 0) {
                this.current = null;
                return;
            }
        }
        if (this.prepare == -1) {
            this.attacker.getAnimationHandler().setAnimation(this.current.anim());
            this.reset = true;
            this.previous = this.current;
        }
    }

    public void resetCooldown() {
        this.idleTime = 0;
    }

    public void moveRandomlyAround() {
        this.moveRandomlyAround(81, 7);
    }

    public void moveRandomlyAround(double maxDistSq, int dist) {
        this.attacker.getLookControl().setLookAt(this.target, 30.0f, 30.0f);
        if (this.distanceToTargetSq <= maxDistSq) {
            if (this.attacker.getNavigation().isDone()) {
                for (int i = 0; i < 10; i++) {
                    Vec3 rand = DefaultRandomPos.getPos(this.attacker, dist, 4);
                    if (rand != null && rand.distanceToSqr(this.target.position()) < maxDistSq) {
                        Path path = this.attacker.getNavigation().createPath(rand.x, rand.y, rand.z, 0);
                        if (path != null) {
                            this.attacker.getNavigation().moveTo(path, 1);
                            break;
                        }
                    }
                }
            }
        } else {
            this.moveToTarget(1);
        }
    }

    public void moveToTargetPosition(double x, double y, double z, double speed) {
        if (this.lastPathTargetPos == null || this.attacker.getNavigation().isDone() || this.lastPathTargetPos.distanceToSqr(x, y, z) > 4) {
            this.lastPathTargetPos = new Vec3(x, y, z);
            Path path = this.attacker.getNavigation().createPath(x, y, z, 0);
            if (path != null)
                this.attacker.getNavigation().moveTo(path, speed);
        }
    }

    public void moveToTarget(double speed) {
        if (this.lastPathTargetPos == null || this.attacker.getNavigation().isDone() || this.lastPathTargetPos.distanceToSqr(this.target.position()) > 4) {
            this.lastPathTargetPos = this.target.position();
            Path path = this.attacker.getNavigation().createPath(this.target, 0);
            if (path != null)
                this.attacker.getNavigation().moveTo(path, speed);
        }
    }

    public BlockPos randomPosAwayFrom(LivingEntity away, float minDis) {
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
    public void circleAround(double posX, double posZ, float radius, boolean clockWise, float speed) {
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

    public void circleAroundTargetFacing(float radius, boolean clockWise, float speed) {
        this.attacker.lookAt(this.target, 30, 30);
        double x = this.attacker.getX() - this.target.getX();
        double z = this.attacker.getZ() - this.target.getZ();
        double r = x * x + z * z;
        this.attacker.getMoveControl().strafe(r < (radius - 1.5) * (radius - 1.5) ? -0.5f : r > (radius + 1.5) * (radius + 1.5) ? 0.5f : 0, clockWise ? speed : -speed);
    }

    public void teleportAround(double posX, double posY, double posZ, int range) {
        double x = posX + (this.attacker.getRandom().nextDouble() - 0.5D) * range * 2;
        double y = posY + (this.attacker.getRandom().nextInt(3));
        double z = posZ + (this.attacker.getRandom().nextDouble() - 0.5D) * range * 2;
        this.attacker.randomTeleport(x, y, z, false);
    }
}