package io.github.flemmli97.tenshilib.common.entity.ai.brain.behaviour;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.behavior.BehaviorUtils;
import net.tslat.smartbrainlib.api.core.behaviour.custom.move.StrafeTarget;
import net.tslat.smartbrainlib.util.BrainUtils;

import java.util.function.BiPredicate;

public class StrafeWithCheck<E extends PathfinderMob> extends StrafeTarget<E> {

    protected BiPredicate<E, LivingEntity> moveTowards = (entity, target) -> !BehaviorUtils.canSee(entity, target);

    public StrafeWithCheck<E> shouldMoveTowards(BiPredicate<E, LivingEntity> moveTowards) {
        this.moveTowards = moveTowards;
        return this;
    }

    @Override
    protected void tick(E entity) {
        LivingEntity target = BrainUtils.getTargetOfEntity(entity);
        if (this.moveTowards.test(entity, target)) {
            entity.getNavigation().moveTo(target, this.speedMod);
            this.strafeCounter = -1;
            return;
        }
        super.tick(entity);
    }
}
