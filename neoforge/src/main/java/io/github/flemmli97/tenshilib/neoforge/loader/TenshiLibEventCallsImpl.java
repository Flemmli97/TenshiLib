package io.github.flemmli97.tenshilib.neoforge.loader;

import io.github.flemmli97.tenshilib.common.entity.BeamEntity;
import io.github.flemmli97.tenshilib.loader.TenshiLibEventCalls;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.phys.HitResult;
import net.neoforged.neoforge.event.EventHooks;

public class TenshiLibEventCallsImpl implements TenshiLibEventCalls {

    @Override
    public boolean projectileHitCall(Projectile projectile, HitResult result) {
        return EventHooks.onProjectileImpact(projectile, result);
    }

    @Override
    public boolean beamHitCall(BeamEntity beam, HitResult result) {
        return false;
    }
}
