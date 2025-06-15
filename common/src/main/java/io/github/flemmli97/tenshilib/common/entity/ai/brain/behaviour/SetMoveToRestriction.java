package io.github.flemmli97.tenshilib.common.entity.ai.brain.behaviour;

import com.mojang.datafixers.util.Pair;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.ai.memory.WalkTarget;
import net.minecraft.world.entity.ai.util.DefaultRandomPos;
import net.minecraft.world.phys.Vec3;
import net.tslat.smartbrainlib.api.core.behaviour.ExtendedBehaviour;
import net.tslat.smartbrainlib.object.MemoryTest;
import net.tslat.smartbrainlib.util.BrainUtils;

import java.util.List;
import java.util.function.BiFunction;

public class SetMoveToRestriction<E extends PathfinderMob> extends ExtendedBehaviour<E> {

    private static final MemoryTest MEMORIES = MemoryTest.builder(1).noMemory(MemoryModuleType.WALK_TARGET);

    protected BiFunction<E, Vec3, Float> speedModifier = (entity, targetPos) -> 1f;

    public SetMoveToRestriction<E> speedModifier(BiFunction<E, Vec3, Float> function) {
        this.speedModifier = function;
        return this;
    }

    @Override
    protected List<Pair<MemoryModuleType<?>, MemoryStatus>> getMemoryRequirements() {
        return MEMORIES;
    }

    @Override
    protected boolean checkExtraStartConditions(ServerLevel level, E entity) {
        return !entity.isWithinRestriction();
    }

    @Override
    protected void start(E entity) {
        Vec3 vec3 = DefaultRandomPos.getPosTowards(entity, 16, 7, Vec3.atBottomCenterOf(entity.getRestrictCenter()), Math.PI / 2);
        if (vec3 == null) {
            BrainUtils.clearMemory(entity, MemoryModuleType.WALK_TARGET);
        } else {
            BrainUtils.setMemory(entity, MemoryModuleType.WALK_TARGET, new WalkTarget(vec3, this.speedModifier.apply(entity, vec3), 0));
        }
    }
}
