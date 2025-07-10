package io.github.flemmli97.tenshilib.loader;

import io.github.flemmli97.tenshilib.TenshiLib;
import io.github.flemmli97.tenshilib.common.entity.BeamEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.OwnableEntity;
import net.minecraft.world.entity.TraceableEntity;
import net.minecraft.world.entity.boss.EnderDragonPart;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.phys.HitResult;
import org.jetbrains.annotations.Nullable;

import java.util.function.BiPredicate;

public interface TenshiLibCrossPlat {

    TenshiLibCrossPlat INSTANCE = LoaderInitializer.getImplInstance(TenshiLibCrossPlat.class,
            "io.github.flemmli97.tenshilib.fabric.loader.TenshiLibCrossPlatImpl",
            "io.github.flemmli97.tenshilib.neoforge.loader.TenshiLibCrossPlatImpl");

    /**
     * Returns true if the current run is in a datagen environment
     */
    boolean isDatagen();

    /**
     * Returns true if the current run is on the physical client
     */
    boolean isPhysicalClient();

    /**
     * Returns the current server instance.
     */
    @Nullable
    MinecraftServer getCurrentServer();

    /**
     * Returns the parent of the part entity if present
     */
    @Nullable
    default Entity getMultipartParent(Entity entity) {
        if (entity instanceof OwnableEntity ownable && entity.getType().is(TenshiLib.MULTIPART_ENTITY))
            return ownable.getOwner();
        if (entity instanceof TraceableEntity traceableEntity && entity.getType().is(TenshiLib.MULTIPART_ENTITY))
            return traceableEntity.getOwner();
        if (entity instanceof EnderDragonPart part)
            return part.parentMob;
        return null;
    }

    /**
     * Returns true if the given entity is a multipart entity with the matching parent
     */
    default boolean isSameMultipart(Entity entity, Entity parent) {
        if (parent == null)
            return false;
        if (entity instanceof OwnableEntity ownable && entity.getType().is(TenshiLib.MULTIPART_ENTITY))
            return parent.getUUID().equals(ownable.getOwnerUUID());
        if (entity instanceof TraceableEntity traceableEntity && entity.getType().is(TenshiLib.MULTIPART_ENTITY))
            return parent == traceableEntity.getOwner();
        if (entity instanceof EnderDragonPart part)
            return parent == part.parentMob;
        return false;
    }

    /**
     * Projectile impact event for when a projectile hits something
     *
     * @return true if the event was cancelled
     */
    boolean projectileImpactEvent(Projectile projectile, HitResult result);

    /**
     * Beam hit event for when a beam hits something
     *
     * @return true if the event was cancelled
     */
    boolean beamHitEvent(BeamEntity beam, HitResult result);

    /**
     * Register a listener for a beam hit event
     * Return true to cancel the event preventing the beam to hit the target
     */
    void listenBeamHitEvent(BiPredicate<BeamEntity, HitResult> test);
}
