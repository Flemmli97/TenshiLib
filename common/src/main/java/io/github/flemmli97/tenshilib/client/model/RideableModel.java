package io.github.flemmli97.tenshilib.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.world.entity.Entity;

public interface RideableModel<T extends Entity> {

    /**
     * @param entity               The ridden entity/vehicle
     * @param entityRenderer       The ridden entity/vehicle's renderer
     * @param rider                The riding entity
     * @param ridingEntityRenderer The riding entities renderer
     * @param stack                PoseStack to transform
     * @param riderNum             The index of the riding entity in {@link Entity#getPassengers()}
     * @return If any of the stack was transformed or not
     */
    boolean transform(T entity, EntityRenderer<T> entityRenderer, Entity rider, EntityRenderer<?> ridingEntityRenderer, PoseStack stack, int riderNum);
}
