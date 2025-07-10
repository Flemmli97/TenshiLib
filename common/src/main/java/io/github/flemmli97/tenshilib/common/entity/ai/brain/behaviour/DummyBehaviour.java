package io.github.flemmli97.tenshilib.common.entity.ai.brain.behaviour;

import net.minecraft.world.entity.LivingEntity;
import net.tslat.smartbrainlib.api.core.behaviour.ExtendedBehaviour;
import net.tslat.smartbrainlib.api.core.behaviour.FirstApplicableBehaviour;
import net.tslat.smartbrainlib.api.core.behaviour.custom.misc.Idle;

import java.util.function.Predicate;

public class DummyBehaviour {

    private static final Predicate<LivingEntity> IMMEDIATE = entity -> true;

    /**
     * Makes the given behaviour optional.
     * Useful for {@link net.tslat.smartbrainlib.api.core.behaviour.SequentialBehaviour}
     * as a behaviour failing to start there causes the whole chain to be aborted
     * <p>
     * Notable affected behaviour are e.g. {@link net.tslat.smartbrainlib.api.core.behaviour.custom.move.MoveToWalkTarget}
     * where if the mob is already at target it wont start
     */
    @SuppressWarnings("unchecked")
    public static <E extends LivingEntity> ExtendedBehaviour<E> opt(ExtendedBehaviour<E> wrapped) {
        return new FirstApplicableBehaviour<>(wrapped, new Idle<>().stopIf(IMMEDIATE));
    }
}