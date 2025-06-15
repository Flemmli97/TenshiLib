package io.github.flemmli97.tenshilib.common.entity.ai.brain.behaviour;

import com.mojang.datafixers.util.Pair;
import io.github.flemmli97.tenshilib.common.entity.ai.brain.data.AnimationPlayHolder;
import io.github.flemmli97.tenshilib.common.entity.ai.brain.memory.MoreMemoryModules;
import io.github.flemmli97.tenshilib.common.entity.animated.AnimatedEntity;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.tslat.smartbrainlib.api.core.behaviour.ExtendedBehaviour;
import net.tslat.smartbrainlib.object.MemoryTest;
import net.tslat.smartbrainlib.util.BrainUtils;

import java.util.Arrays;
import java.util.List;

public class SetAnimationToPlay<E extends Mob & AnimatedEntity> extends ExtendedBehaviour<E> {

    private static final MemoryTest MEMORIES = MemoryTest.builder(1).usesMemory(MoreMemoryModules.ANIMATION_TO_PLAY.get());

    private final List<AnimationPlayHolder<E>> animations;

    public SetAnimationToPlay(String... animations) {
        this.animations = Arrays.stream(animations).map(s -> new AnimationPlayHolder<E>(s)).toList();
    }

    @SafeVarargs
    public SetAnimationToPlay(AnimationPlayHolder<E>... animations) {
        this.animations = List.of(animations);
    }

    @Override
    protected List<Pair<MemoryModuleType<?>, MemoryStatus>> getMemoryRequirements() {
        return MEMORIES;
    }

    @Override
    protected boolean checkExtraStartConditions(ServerLevel level, E entity) {
        return !this.animations.isEmpty() && !entity.getAnimationHandler().hasAnimation();
    }

    @Override
    protected void start(E entity) {
        AnimationPlayHolder<E> selected = this.animations.get(entity.getRandom().nextInt(this.animations.size()));
        BrainUtils.setMemory(entity, MoreMemoryModules.ANIMATION_TO_PLAY.get(), selected);
    }
}
