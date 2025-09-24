package io.github.flemmli97.tenshilib.common.entity.ai.brain.behaviour;

import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.behavior.EntityTracker;
import net.minecraft.world.entity.ai.memory.WalkTarget;
import net.tslat.smartbrainlib.api.core.behaviour.custom.move.WalkOrRunToWalkTarget;

public class MoveToWalkTargetWithSight<E extends PathfinderMob> extends WalkOrRunToWalkTarget<E> {

    protected boolean requireSight = true;

    protected boolean stopIfTouching = true;

    protected boolean accountForBB = true;

    public MoveToWalkTargetWithSight<E> ignoreSight() {
        this.requireSight = false;
        return this;
    }

    public MoveToWalkTargetWithSight<E> ignoreBoundingBox() {
        this.accountForBB = false;
        return this;
    }

    public MoveToWalkTargetWithSight<E> ignoreTouch() {
        this.stopIfTouching = false;
        return this;
    }

    @Override
    protected boolean hasReachedTarget(E entity, WalkTarget target) {
        if (target.getTarget() instanceof EntityTracker tracker) {
            if (this.requireSight && !entity.getSensing().hasLineOfSight(tracker.getEntity())) {
                return false;
            }
            if (this.stopIfTouching && entity.getBoundingBox().inflate(0.5).intersects(tracker.getEntity().getBoundingBox())) {
                return true;
            }
            if (this.accountForBB) {
                double min = target.getCloseEnoughDist() + entity.getBbWidth() * 0.5 + tracker.getEntity().getBbWidth() * 0.5;
                return entity.distanceToSqr(tracker.getEntity()) <= min * min;
            }
        }
        return super.hasReachedTarget(entity, target);
    }
}
