package io.github.flemmli97.tenshilib.client.model;

import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.Nullable;

import java.lang.ref.WeakReference;
import java.util.function.Function;

public abstract class ExtendedEntityModel<T extends Entity> extends EntityModel<T> implements ExtendedModel {

    protected float partialTick;

    protected WeakReference<T> entity;

    protected ExtendedEntityModel() {
        super();
    }

    protected ExtendedEntityModel(Function<ResourceLocation, RenderType> renderType) {
        super(renderType);
    }

    @Override
    public void prepareMobModel(T entity, float limbSwing, float limbSwingAmount, float partialTick) {
        this.entity = new WeakReference<>(entity);
        this.partialTick = partialTick;
    }

    public float getPartialTick() {
        return this.partialTick;
    }

    @Nullable
    public T getCurrentEntity() {
        return this.entity != null ? this.entity.get() : null;
    }
}
