package io.github.flemmli97.tenshilib.client.particles.advanced.handler;

import io.github.flemmli97.tenshilib.client.particles.advanced.AdvancedParticleHandler;
import io.github.flemmli97.tenshilib.common.particle.data.EntityFollowData;
import io.github.flemmli97.tenshilib.mixin.ParticleAccessor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.Particle;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

public class EntityFollowHandler implements AdvancedParticleHandler {

    private final EntityFollowData data;
    private final Entity entity;

    private Vec3 lastPos;

    public EntityFollowHandler(EntityFollowData data, Particle particle) {
        this.data = data;
        this.entity = Minecraft.getInstance().level.getEntity(data.entity());
        if (this.entity != null) {
            this.lastPos = this.entity.position();
            this.setPositionBasedOnEntity(particle);
        }
    }

    @Override
    public void tick(Particle particle) {
        if (this.entity == null || this.entity.isRemoved()) {
            particle.remove();
            return;
        }
        this.setPositionBasedOnEntity(particle);
    }

    private void setPositionBasedOnEntity(Particle particle) {
        Vec3 pos = this.entity.position();
        if (this.data.differenceOnly()) {
            pos = pos.subtract(this.lastPos)
                    .add(((ParticleAccessor) particle).getX(), ((ParticleAccessor) particle).getY(), ((ParticleAccessor) particle).getZ());
        } else if (this.data.offset().isPresent()) {
            pos = pos.add(this.data.offset().get().yRot(-this.entity.getYRot() * Mth.DEG_TO_RAD));
        }
        particle.setPos(pos.x(), pos.y(), pos.z());
        this.lastPos = this.entity.position();
    }
}
