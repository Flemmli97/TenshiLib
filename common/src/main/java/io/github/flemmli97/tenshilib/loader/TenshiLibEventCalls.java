package io.github.flemmli97.tenshilib.loader;

import io.github.flemmli97.tenshilib.common.entity.BeamEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.phys.HitResult;

public interface TenshiLibEventCalls {

    TenshiLibEventCalls INSTANCE = LoaderInitializer.getImplInstance(TenshiLibEventCalls.class,
            "io.github.flemmli97.tenshilib.fabric.loader.TenshiLibEventCallsImpl",
            "io.github.flemmli97.tenshilib.neoforge.loader.TenshiLibEventCallsImpl");

    boolean projectileHitCall(Projectile projectile, HitResult result);

    boolean beamHitCall(BeamEntity beam, HitResult result);
}
