package io.github.flemmli97.tenshilib.common.entity.ai.brain.behaviour;

import com.mojang.datafixers.util.Pair;
import io.github.flemmli97.tenshilib.common.entity.AOEAttackEntity;
import io.github.flemmli97.tenshilib.common.entity.ai.brain.data.AnimationPlayHolder;
import io.github.flemmli97.tenshilib.common.entity.ai.brain.memory.MoreMemoryModules;
import io.github.flemmli97.tenshilib.common.entity.animated.AnimatedEntity;
import io.github.flemmli97.tenshilib.common.utils.math.OrientedBoundingBox;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.behavior.EntityTracker;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.ai.memory.WalkTarget;
import net.tslat.smartbrainlib.api.core.behaviour.custom.move.WalkOrRunToWalkTarget;
import net.tslat.smartbrainlib.object.MemoryTest;
import net.tslat.smartbrainlib.util.BrainUtils;

import java.util.List;

public class MoveToAttackTarget<E extends PathfinderMob & AOEAttackEntity & AnimatedEntity> extends WalkOrRunToWalkTarget<E> {

    private static final MemoryTest MEMORIES = MemoryTest.builder(4).hasMemories(MoreMemoryModules.ANIMATION_TO_PLAY.get())
            .hasMemory(MemoryModuleType.WALK_TARGET).noMemory(MemoryModuleType.PATH).usesMemory(MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE);

    @Override
    protected List<Pair<MemoryModuleType<?>, MemoryStatus>> getMemoryRequirements() {
        return MEMORIES;
    }

    @Override
    protected boolean doStartCheck(ServerLevel level, E entity, long gameTime) {
        AnimationPlayHolder<?> animation = BrainUtils.getMemory(entity, MoreMemoryModules.ANIMATION_TO_PLAY.get());
        return super.doStartCheck(level, entity, gameTime) && animation != null;
    }

    @Override
    protected boolean canStillUse(ServerLevel level, E entity, long gameTime) {
        AnimationPlayHolder<?> animation = BrainUtils.getMemory(entity, MoreMemoryModules.ANIMATION_TO_PLAY.get());
        return super.canStillUse(level, entity, gameTime) && animation != null;
    }

    @Override
    protected boolean hasReachedTarget(E entity, WalkTarget target) {
        AnimationPlayHolder<?> animation = BrainUtils.getMemory(entity, MoreMemoryModules.ANIMATION_TO_PLAY.get());
        if (animation == null)
            return true;
        if (target.getTarget() instanceof EntityTracker entityTracker) {
            Entity targetEntity = entityTracker.getEntity();
            OrientedBoundingBox aabb = entity.prepareAttackBox(animation.animation(), targetEntity, -0.15, true);
            if (aabb.intersects(targetEntity.getBoundingBox())) {
                return true;
            }
        }
        return super.hasReachedTarget(entity, target);
    }
}
