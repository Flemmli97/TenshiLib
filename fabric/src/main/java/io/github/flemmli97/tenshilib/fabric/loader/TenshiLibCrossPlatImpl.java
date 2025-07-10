package io.github.flemmli97.tenshilib.fabric.loader;

import io.github.flemmli97.tenshilib.common.entity.BeamEntity;
import io.github.flemmli97.tenshilib.fabric.events.BeamHitEvent;
import io.github.flemmli97.tenshilib.loader.TenshiLibCrossPlat;
import net.fabricmc.api.EnvType;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.phys.HitResult;
import org.jetbrains.annotations.Nullable;

import java.util.function.BiPredicate;

public class TenshiLibCrossPlatImpl implements TenshiLibCrossPlat {

    @Nullable
    public static MinecraftServer CURRENT_SERVER;

    @Override
    public boolean isDatagen() {
        return false;
    }

    @Override
    public boolean isPhysicalClient() {
        return FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT;
    }

    @Override
    public MinecraftServer getCurrentServer() {
        return CURRENT_SERVER;
    }

    @Override
    public boolean projectileImpactEvent(Projectile projectile, HitResult result) {
        return false;
    }

    @Override
    public boolean beamHitEvent(BeamEntity beam, HitResult result) {
        return BeamHitEvent.EVENT.invoker().test(beam, result);
    }

    @Override
    public void listenBeamHitEvent(BiPredicate<BeamEntity, HitResult> test) {
        BeamHitEvent.EVENT.register(test);
    }
}
