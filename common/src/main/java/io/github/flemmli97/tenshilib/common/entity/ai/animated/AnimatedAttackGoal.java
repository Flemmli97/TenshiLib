package io.github.flemmli97.tenshilib.common.entity.ai.animated;

import io.github.flemmli97.tenshilib.api.entity.AnimatedAction;
import io.github.flemmli97.tenshilib.common.entity.IAnimated;
import io.github.flemmli97.tenshilib.common.utils.MathUtils;
import net.minecraft.util.random.WeightedEntry;
import net.minecraft.util.random.WeightedRandom;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.Goal;
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
    protected final boolean checkRestriction;

    protected LivingEntity target;

    @Nullable
    public GoalAttackAction.ActiveAction<T> current;
    protected GoalAttackAction.ActiveAction<T> previous;
    protected List<GoalAttackAction.ChainedAction> chained;
    private ActionRun<T> onIdle;
    private boolean reset;

    protected Vec3 lastPathTargetPos;
    protected int idleTime, prepare, chainSelect;
    public double distanceToTargetSq;
    public boolean canSee;

    /**
     * @param actions     A list of weighted goal actions that decide what attack to execute
     * @param idleActions A list of idle actions that runs when the attacks are in cooldown or no matching attack was found.
     */
    public AnimatedAttackGoal(T entity, List<WeightedEntry.Wrapper<GoalAttackAction<T>>> actions, List<WeightedEntry.Wrapper<IdleAction<T>>> idleActions) {
        this(entity, actions, idleActions, true);
    }

    public AnimatedAttackGoal(T entity, List<WeightedEntry.Wrapper<GoalAttackAction<T>>> actions, List<WeightedEntry.Wrapper<IdleAction<T>>> idleActions, boolean checkRestriction) {
        this.attacker = entity;
        this.actions = actions;
        this.idleActions = idleActions;
        this.checkRestriction = checkRestriction;
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        LivingEntity living = this.attacker.getTarget();
        return !this.actions.isEmpty() && living != null && living.isAlive()
                && (!this.checkRestriction || this.attacker.isWithinRestriction(living.blockPosition()));
    }

    @Override
    public boolean canContinueToUse() {
        return this.canUse();
    }

    @Override
    public void stop() {
        this.current = null;
        this.previous = null;
        this.chained = null;
        this.target = null;
        this.reset = false;
        this.lastPathTargetPos = null;
        this.idleTime = 0;
        this.prepare = 0;
        this.attacker.getNavigation().stop();
        this.attacker.setZza(0);
        this.attacker.setXxa(0);
    }

    protected void resetAttack() {
        this.current = null;
        this.reset = false;
        this.onIdle = null;
        this.lastPathTargetPos = null;
        this.chained = null;
        this.chainSelect = 0;
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
        this.resetAttack();
        List<WeightedEntry.Wrapper<IdleAction<T>>> idles = this.idleActions.stream().filter(d -> d.data().test(this, this.target)).toList();
        IdleAction<T> idle = WeightedRandom.getRandomItem(this.attacker.getRandom(), idles).map(WeightedEntry.Wrapper::data).orElse(null);
        this.onIdle = idle != null ? idle.runner.create() : null;
        if (idle != null) {
            this.onIdle = idle.runner.create();
            this.idleTime = idle.getDuration().getInt(this.attacker);
        } else {
            this.idleTime = 20;
        }
        List<WeightedEntry.Wrapper<GoalAttackAction<T>>> selectables = this.actions.stream().filter(d -> d.data().test(this, this.target, this.previous != null ? this.previous.anim().getID() : "")).toList();
        GoalAttackAction<T> action = WeightedRandom.getRandomItem(this.attacker.getRandom(), selectables).map(WeightedEntry.Wrapper::data).orElse(null);
        this.current = action != null ? action.createActive() : null;
        if (action != null) {
            this.prepare = this.current.start().timeout().getInt(this.attacker);
            this.idleTime = action.getCooldown().getInt(this.attacker);
            if (action.getChainedAction() != null) {
                this.chained = action.getChainedAction().get(this.attacker);
            }
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
        if (this.current == null) {
            if (this.chained != null) {
                GoalAttackAction.ChainedAction action = this.chained.get(this.chainSelect);
                this.attacker.getAnimationHandler().setAnimation(action.anim(), action.transitionTime(), -1, action.offset());
                ++this.chainSelect;
                if (this.chainSelect >= this.chained.size()) {
                    this.chained = null;
                }
                return;
            }
            if (--this.idleTime > 0) {
                if (this.onIdle != null)
                    this.onIdle.run(this, this.target, null);
                return;
            }
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
                this.resetAttack();
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

    public void moveToTargetPosition(double x, double y, double z, double speed) {
        if (this.lastPathTargetPos == null || this.attacker.getNavigation().isDone() || this.lastPathTargetPos.distanceToSqr(x, y, z) > 4) {
            this.lastPathTargetPos = new Vec3(x, y, z);
            Path path = this.attacker.getNavigation().createPath(x, y, z, 0);
            if (path != null)
                this.attacker.getNavigation().moveTo(path, speed);
        }
    }

    public void moveToTarget(double speed) {
        this.moveToTargetPosition(this.target.getX(), this.target.getY(), this.target.getZ(), speed);
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
}