package io.github.flemmli97.tenshilib.common.entity.ai.brain.behaviour;

import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.behavior.EntityTracker;
import net.minecraft.world.entity.ai.memory.WalkTarget;
import net.tslat.smartbrainlib.api.core.behaviour.custom.move.WalkOrRunToWalkTarget;

public class MoveToWalkTargetWithSight<E extends PathfinderMob> extends WalkOrRunToWalkTarget<E> {

    protected boolean requireSight = true;

    public MoveToWalkTargetWithSight<E> ignoreSight() {
        this.requireSight = false;
        return this;
    }

    @Override
    protected boolean hasReachedTarget(E entity, WalkTarget target) {
        if (this.requireSight && target.getTarget() instanceof EntityTracker entityTracker && !entity.getSensing().hasLineOfSight(entityTracker.getEntity())) {
            return false;
        }
        return super.hasReachedTarget(entity, target);
    }
}
