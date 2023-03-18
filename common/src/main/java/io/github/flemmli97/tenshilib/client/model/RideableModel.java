package io.github.flemmli97.tenshilib.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.world.entity.Entity;

public interface RideableModel<T> {

    void transform(T entity, Entity rider, PoseStack stack, int riderNum);
}
