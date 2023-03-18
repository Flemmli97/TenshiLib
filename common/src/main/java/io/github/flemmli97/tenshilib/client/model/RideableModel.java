package io.github.flemmli97.tenshilib.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.world.entity.Entity;

public interface RideableModel<T> {

    /**
     * @param entity         The ridden entity/vehicle
     * @param rider          The riding entity
     * @param entityRenderer The riding entities renderer
     * @param stack
     * @param riderNum       The index of the riding entity in {@link Entity#getPassengers()}
     */
    void transform(T entity, Entity rider, EntityRenderer<?> entityRenderer, PoseStack stack, int riderNum);
}
