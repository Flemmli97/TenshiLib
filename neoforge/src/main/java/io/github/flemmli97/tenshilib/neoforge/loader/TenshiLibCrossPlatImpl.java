package io.github.flemmli97.tenshilib.neoforge.loader;

import io.github.flemmli97.tenshilib.common.entity.BeamEntity;
import io.github.flemmli97.tenshilib.loader.TenshiLibCrossPlat;
import io.github.flemmli97.tenshilib.neoforge.events.BeamHitEvent;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.phys.HitResult;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.data.loading.DatagenModLoader;
import net.neoforged.neoforge.entity.PartEntity;
import net.neoforged.neoforge.event.EventHooks;
import net.neoforged.neoforge.server.ServerLifecycleHooks;

import java.util.function.BiPredicate;
import java.util.function.Consumer;

public class TenshiLibCrossPlatImpl implements TenshiLibCrossPlat {

    @Override
    public boolean isDatagen() {
        return DatagenModLoader.isRunningDataGen();
    }

    @Override
    public boolean isPhysicalClient() {
        return FMLEnvironment.dist == Dist.CLIENT;
    }

    @Override
    public MinecraftServer getCurrentServer() {
        return ServerLifecycleHooks.getCurrentServer();
    }

    @Override
    public Entity getMultipartParent(Entity entity) {
        if (entity instanceof PartEntity<?> part)
            return part.getParent();
        return TenshiLibCrossPlat.super.getMultipartParent(entity);
    }

    @Override
    public boolean isSameMultipart(Entity entity, Entity parent) {
        if (entity instanceof PartEntity<?> part)
            return part.getParent() == parent;
        return TenshiLibCrossPlat.super.isSameMultipart(entity, parent);
    }

    @Override
    public boolean projectileImpactEvent(Projectile projectile, HitResult result) {
        return EventHooks.onProjectileImpact(projectile, result);
    }

    @Override
    public boolean beamHitEvent(BeamEntity beam, HitResult result) {
        return NeoForge.EVENT_BUS.post(new BeamHitEvent(beam, result)).isCanceled();
    }

    @Override
    public void listenBeamHitEvent(BiPredicate<BeamEntity, HitResult> test) {
        Consumer<BeamHitEvent> cons = event -> {
            if (test.test(event.getBeam(), event.getHitResult()))
                event.setCanceled(true);
        };
        NeoForge.EVENT_BUS.addListener(cons);
    }
}
