package io.github.flemmli97.tenshilib.common.entity.ai.brain.behaviour;

import com.mojang.datafixers.util.Pair;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.behavior.EntityTracker;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.ai.memory.WalkTarget;
import net.minecraft.world.entity.ai.util.DefaultRandomPos;
import net.minecraft.world.phys.Vec3;
import net.tslat.smartbrainlib.api.core.behaviour.ExtendedBehaviour;
import net.tslat.smartbrainlib.object.MemoryTest;
import net.tslat.smartbrainlib.object.SquareRadius;
import net.tslat.smartbrainlib.util.BrainUtils;

import java.util.List;
import java.util.function.BiFunction;
import java.util.function.ToIntBiFunction;

public class SetWalkTargetAwayFromTarget<E extends PathfinderMob> extends ExtendedBehaviour<E> {

    private static final MemoryTest MEMORIES = MemoryTest.builder(3).hasMemory(MemoryModuleType.ATTACK_TARGET)
            .usesMemories(MemoryModuleType.WALK_TARGET, MemoryModuleType.LOOK_TARGET);

    protected BiFunction<E, LivingEntity, Float> speedMod = (entity, target) -> 1f;
    protected BiFunction<E, LivingEntity, Float> minDist = (entity, target) -> 1f;
    protected SquareRadius radius = new SquareRadius(10, 7);
    protected ToIntBiFunction<E, LivingEntity> closeEnoughWhen = (entity, target) -> 0;

    public SetWalkTargetAwayFromTarget<E> speedMod(float speedModifier) {
        return this.speedMod((entity, target) -> speedModifier);
    }

    public SetWalkTargetAwayFromTarget<E> speedMod(BiFunction<E, LivingEntity, Float> speedModifier) {
        this.speedMod = speedModifier;
        return this;
    }

    public SetWalkTargetAwayFromTarget<E> minDist(float minDist) {
        return this.minDist((entity, target) -> minDist);
    }

    public SetWalkTargetAwayFromTarget<E> minDist(BiFunction<E, LivingEntity, Float> minDist) {
        this.minDist = minDist;
        return this;
    }

    public SetWalkTargetAwayFromTarget<E> radius(double radius) {
        return this.radius(radius, radius);
    }

    public SetWalkTargetAwayFromTarget<E> radius(double xz, double y) {
        this.radius = new SquareRadius(xz, y);
        return this;
    }

    @Override
    protected List<Pair<MemoryModuleType<?>, MemoryStatus>> getMemoryRequirements() {
        return MEMORIES;
    }

    @Override
    protected void start(E entity) {
        Brain<?> brain = entity.getBrain();
        LivingEntity target = BrainUtils.getTargetOfEntity(entity);

        if (!entity.getSensing().hasLineOfSight(target)) {
            BrainUtils.setMemory(brain, MemoryModuleType.LOOK_TARGET, new EntityTracker(target, true));
            BrainUtils.setMemory(brain, MemoryModuleType.WALK_TARGET, new WalkTarget(new EntityTracker(target, false), this.speedMod.apply(entity, target), this.closeEnoughWhen.applyAsInt(entity, target)));
        } else {
            double minDist = this.minDist.apply(entity, target);
            if (entity.distanceToSqr(target) < minDist * minDist) {
                for (int i = 0; i < 10; i++) {
                    Vec3 posAway = DefaultRandomPos.getPosAway(entity, (int) this.radius.xzRadius(), (int) this.radius.yRadius(), target.position());
                    if (posAway != null) {
                        BrainUtils.setMemory(brain, MemoryModuleType.LOOK_TARGET, new EntityTracker(target, true));
                        BrainUtils.setMemory(brain, MemoryModuleType.WALK_TARGET, new WalkTarget(posAway, this.speedMod.apply(entity, target), this.closeEnoughWhen.applyAsInt(entity, target)));
                        break;
                    }
                }
            }
        }
    }
}
