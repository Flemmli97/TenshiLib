package io.github.flemmli97.tenshilib.common.entity.ai.brain.behaviour;

import com.mojang.datafixers.util.Pair;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.tslat.smartbrainlib.api.core.behaviour.ExtendedBehaviour;
import net.tslat.smartbrainlib.api.core.behaviour.FirstApplicableBehaviour;

import java.util.List;

public class DummyBehaviour<E extends LivingEntity> extends ExtendedBehaviour<E> {

    private final boolean keepRunning;

    private DummyBehaviour(boolean keepRunning) {
        this.keepRunning = keepRunning;
    }

    public static <E extends LivingEntity> ExtendedBehaviour<E> opt(ExtendedBehaviour<E> wrapped) {
        return opt(wrapped, true);
    }

    /**
     * Wrap the given behaviour making it optional.
     * Makes it useful for {@link net.tslat.smartbrainlib.api.core.behaviour.SequentialBehaviour}
     * as a behaviour failing to start there causes the whole chain to be aborted
     * <p>
     * Notable affected behaviour are e.g. {@link net.tslat.smartbrainlib.api.core.behaviour.custom.move.MoveToWalkTarget}
     * where if the mob is already at target it fails
     */
    @SuppressWarnings("unchecked")
    public static <E extends LivingEntity> ExtendedBehaviour<E> opt(ExtendedBehaviour<E> wrapped, boolean keepRunning) {
        return new FirstApplicableBehaviour<>(wrapped, new DummyBehaviour<>(keepRunning));
    }

    @Override
    protected List<Pair<MemoryModuleType<?>, MemoryStatus>> getMemoryRequirements() {
        return List.of();
    }

    @Override
    protected boolean shouldKeepRunning(E entity) {
        return this.keepRunning;
    }
}