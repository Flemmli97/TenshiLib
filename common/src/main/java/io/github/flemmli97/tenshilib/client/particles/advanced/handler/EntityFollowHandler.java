package io.github.flemmli97.tenshilib.client.particles.advanced.handler;

import io.github.flemmli97.tenshilib.client.particles.advanced.AdvancedParticleHandler;
import io.github.flemmli97.tenshilib.common.particle.data.EntityFollowData;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.Particle;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

public class EntityFollowHandler implements AdvancedParticleHandler {

    private final EntityFollowData data;
    private final Entity entity;

    public EntityFollowHandler(EntityFollowData data, Particle particle) {
        this.data = data;
        this.entity = Minecraft.getInstance().level.getEntity(data.entity());
        if (this.entity != null) {
            this.setPositionBasedOnEntity(particle);
        }
    }

    @Override
    public void tick(Particle particle) {
        if (this.entity == null) {
            particle.remove();
            return;
        }
        this.setPositionBasedOnEntity(particle);
    }

    private void setPositionBasedOnEntity(Particle particle) {
        Vec3 pos = this.data.offset().isPresent() ? this.data.offset().get().yRot(-this.entity.getYRot() * Mth.DEG_TO_RAD) : this.entity.position();
        particle.setPos(pos.x(), pos.y(), pos.z());
    }
}
