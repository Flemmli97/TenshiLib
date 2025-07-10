package io.github.flemmli97.tenshilib.neoforge.events;

import io.github.flemmli97.tenshilib.common.entity.BeamEntity;
import net.minecraft.world.phys.HitResult;
import net.neoforged.bus.api.ICancellableEvent;
import net.neoforged.neoforge.event.entity.EntityEvent;

public class BeamHitEvent extends EntityEvent implements ICancellableEvent {

    private final HitResult hitResult;
    private final BeamEntity beam;

    public BeamHitEvent(BeamEntity beam, HitResult hitResult) {
        super(beam);
        this.hitResult = hitResult;
        this.beam = beam;
    }

    public HitResult getHitResult() {
        return this.hitResult;
    }

    public BeamEntity getBeam() {
        return this.beam;
    }
}