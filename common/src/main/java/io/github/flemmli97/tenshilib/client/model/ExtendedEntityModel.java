package io.github.flemmli97.tenshilib.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import io.github.flemmli97.tenshilib.client.data.GeoAnimationManager;
import io.github.flemmli97.tenshilib.client.data.GeoModelManager;
import io.github.flemmli97.tenshilib.client.data.ReloadableCache;
import io.github.flemmli97.tenshilib.client.model.animation.Animation;
import io.github.flemmli97.tenshilib.common.entity.animated.AnimationState;
import io.github.flemmli97.tenshilib.common.utils.math.parser.VariableMap;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.Nullable;

import java.lang.ref.WeakReference;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public abstract class ExtendedEntityModel<T extends Entity> extends EntityModel<T> implements ExtendedModel {

    protected float partialTick;

    protected WeakReference<T> entity;
    protected final ReloadableCache<ModelPartsContainer> model;
    protected final ReloadableCache<BedrockAnimations> animation;

    @Deprecated(forRemoval = true)
    protected ExtendedEntityModel() {
        this(null, null);
    }

    @Deprecated(forRemoval = true)
    protected ExtendedEntityModel(Function<ResourceLocation, RenderType> renderType) {
        this(renderType, null, null);
    }

    protected ExtendedEntityModel(ResourceLocation model) {
        this(model, null);
    }

    protected ExtendedEntityModel(ResourceLocation model, ResourceLocation animation) {
        this(RenderType::entityCutoutNoCull, model, animation);
    }

    protected ExtendedEntityModel(Function<ResourceLocation, RenderType> renderType,
                                  ResourceLocation model, ResourceLocation animation) {
        super(renderType);
        // Legacy with empty model
        this.model = model == null ? ReloadableCache.of(new ModelPartsContainer(new ModelPart(List.of(), Map.of()))) : GeoModelManager.getInstance().getModel(model, this::onModelReload);
        this.animation = animation == null ? null : GeoAnimationManager.getInstance().getAnimation(animation);
    }

    protected void onModelReload(ModelPartsContainer model) {
    }

    @Override
    public void prepareMobModel(T entity, float limbSwing, float limbSwingAmount, float partialTick) {
        this.entity = new WeakReference<>(entity);
        this.partialTick = partialTick;
    }

    @Override
    public void setupAnim(T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        this.getModel().resetPoses();
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer buffer, int packedLight, int packedOverlay, int color) {
        this.getModel().getRoot().render(poseStack, buffer, packedLight, packedOverlay, color);
    }

    @Override
    public ModelPartsContainer getModel() {
        return this.model.get();
    }

    @Override
    public void onPlayAnimation(AnimationState state, Animation animation, float tick, VariableMap variables) {
        ExtendedModel.super.onPlayAnimation(state, animation, tick, variables);
        MolangQueries.applyEntityQueriesTo(variables, animation.variables(), this.getCurrentEntity(), this.partialTick);
    }

    public float getPartialTick() {
        return this.partialTick;
    }

    @Nullable
    public T getCurrentEntity() {
        return this.entity != null ? this.entity.get() : null;
    }
}
