package io.github.flemmli97.tenshilib.fabric.events;

import io.github.flemmli97.tenshilib.common.entity.BeamEntity;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.world.phys.HitResult;

import java.util.function.BiPredicate;

public class BeamHitEvent {

    public static final Event<BiPredicate<BeamEntity, HitResult>> EVENT = EventFactory.createArrayBacked(BiPredicate.class,
            (listeners) -> (beam, hitResult) -> {
                for (BiPredicate<BeamEntity, HitResult> event : listeners) {
                    if (event.test(beam, hitResult))
                        return true;
                }
                return false;
            }
    );
}