package io.github.flemmli97.tenshilib.common.entity.ai.brain;

import com.mojang.datafixers.util.Pair;
import io.github.flemmli97.tenshilib.common.entity.ai.brain.behaviour.DummyBehaviour;
import net.minecraft.world.entity.LivingEntity;
import net.tslat.smartbrainlib.api.core.behaviour.ExtendedBehaviour;
import net.tslat.smartbrainlib.api.core.behaviour.OneRandomBehaviour;
import net.tslat.smartbrainlib.api.core.behaviour.SequentialBehaviour;
import net.tslat.smartbrainlib.api.core.behaviour.custom.misc.Idle;

import java.util.ArrayList;
import java.util.List;

/**
 * Helper in building a selectable behaviour sequence
 * Using only {@link OneRandomBehaviour} with {@link SequentialBehaviour} will cause a switch to another
 * sequence if the current one fails/stops/aborts
 * Using this will make it so the selected one runs till its stopped from other sources.
 * <pre>
 * {@code
 *  # Stop example
 *  SelectableBehaviourBuilder.builder().build().stopIf(Condition)
 * }
 * </pre>
 */
public class SelectableBehaviourBuilder<E extends LivingEntity> {

    private final List<Pair<ExtendedBehaviour<E>, Integer>> behaviors = new ArrayList<>();

    public static <E extends LivingEntity> SelectableBehaviourBuilder<E> builder() {
        return new SelectableBehaviourBuilder<>();
    }

    @SafeVarargs
    public final SelectableBehaviourBuilder<E> add(int weight, ExtendedBehaviour<E>... behaviours) {
        if (behaviours.length == 0)
            return this;
        if (behaviours.length == 1 && behaviours[0] instanceof Idle<E>) {
            this.behaviors.add(Pair.of(behaviours[0], weight));
        } else {
            this.behaviors.add(Pair.of(DummyBehaviour.opt(new SequentialBehaviour<>(behaviours)), weight));
        }
        return this;
    }

    public SelectableBehaviourBuilder<E> addOpt(int weight, ExtendedBehaviour<E> behaviour) {
        this.behaviors.add(Pair.of(DummyBehaviour.opt(behaviour), weight));
        return this;
    }

    @SuppressWarnings("unchecked")
    public ExtendedBehaviour<E> build() {
        return new OneRandomBehaviour<>(this.behaviors.toArray(Pair[]::new));
    }
}
