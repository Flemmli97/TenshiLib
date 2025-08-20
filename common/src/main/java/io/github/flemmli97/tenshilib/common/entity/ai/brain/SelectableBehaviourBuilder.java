package io.github.flemmli97.tenshilib.common.entity.ai.brain;

import com.mojang.datafixers.util.Pair;
import net.minecraft.world.entity.LivingEntity;
import net.tslat.smartbrainlib.api.core.behaviour.ExtendedBehaviour;
import net.tslat.smartbrainlib.api.core.behaviour.OneRandomBehaviour;
import net.tslat.smartbrainlib.api.core.behaviour.RepeatingBehaviour;
import net.tslat.smartbrainlib.api.core.behaviour.SequentialBehaviour;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

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
        return this.add(weight, null, behaviours);
    }

    @SafeVarargs
    public final SelectableBehaviourBuilder<E> add(int weight, Predicate<E> condition, ExtendedBehaviour<E>... behaviours) {
        if (behaviours.length == 0)
            return this;
        ExtendedBehaviour<E> behaviour;
        if (behaviours.length == 1) {
            behaviour = new RepeatingBehaviour<>(behaviours[0]);
        } else {
            behaviour = new RepeatingBehaviour<>(new SequentialBehaviour<>(behaviours));
        }
        if (condition != null) {
            behaviour.startCondition(condition);
        }
        this.behaviors.add(Pair.of(behaviour, weight));
        return this;
    }

    @SuppressWarnings("unchecked")
    public ExtendedBehaviour<E> build() {
        return new OneRandomBehaviour<>(this.behaviors.toArray(Pair[]::new));
    }
}
