package io.github.flemmli97.tenshilib.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Vector3f;
import io.github.flemmli97.tenshilib.client.ClientHandlers;
import io.github.flemmli97.tenshilib.client.model.RideableModel;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.ReportedException;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

/**
 * Add to any entity renderer where the rider should adjust according to the entities model
 */
public class RiderLayerRenderer<T extends LivingEntity, M extends EntityModel<T> & RideableModel<T>> extends RenderLayer<T, M> {

    private final EntityRenderDispatcher dispatcher;

    private final LivingEntityRenderer<T, M> renderer;

    public RiderLayerRenderer(LivingEntityRenderer<T, M> renderer) {
        super(renderer);
        this.renderer = renderer;
        this.dispatcher = Minecraft.getInstance().getEntityRenderDispatcher();
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    @Override
    public void render(PoseStack stack, MultiBufferSource buffer, int light, T entity, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch) {
        for (int i = 0; i < entity.getPassengers().size(); i++) {
            Entity rider = entity.getPassengers().get(i);
            if (rider == null || (Minecraft.getInstance().cameraEntity == rider && Minecraft.getInstance().options.getCameraType() == CameraType.FIRST_PERSON))
                continue;
            ClientHandlers.RIDING_RENDER_BLACKLIST.add(rider.getUUID());
            stack.pushPose();
            EntityRenderer<?> entityRenderer = this.dispatcher.getRenderer(rider);
            boolean transformed = this.getParentModel().transform(entity, this.renderer, rider, entityRenderer, stack, i);
            this.renderPassenger(entity, (EntityRenderer) entityRenderer, rider, partialTicks, stack, buffer, light, transformed);
            stack.popPose();
            ClientHandlers.RIDING_RENDER_BLACKLIST.remove(rider.getUUID());
        }
    }

    /**
     * Undo transforms of the stacks from {@link LivingEntityRenderer} for the entity T
     * For renderer with different transforms than vanilla this also needs to be adjusted
     */
    protected void undoLivingRendererTransform(EntityRenderer<?> entityRenderer, PoseStack stack, T entity, Entity rider, float partialTicks, boolean transformed) {
        float yaw = Mth.rotLerp(partialTicks, entity.yBodyRotO, entity.yBodyRot);
        if (rider instanceof LivingEntity livingRider) {
            float headRot = Mth.rotLerp(partialTicks, livingRider.yHeadRotO, livingRider.yHeadRot);
            float diff = Mth.wrapDegrees(headRot - yaw);
            if (diff < -85.0f) {
                diff = -85.0f;
            }
            if (diff >= 85.0f) {
                diff = 85.0f;
            }
            yaw = headRot - diff;
            if (diff * diff > 2500.0f) {
                yaw += diff * 0.2f;
            }
        }
        if (!transformed)
            stack.translate(0.0D, 1.501f, 0.0D);
        stack.scale(-1.0F, -1.0F, 1.0F);
        stack.mulPose(Vector3f.YP.rotationDegrees(yaw + 180.0F));
    }

    public <E extends Entity> void renderPassenger(T vehicle, EntityRenderer<E> entityRenderer, E entity, float partialTicks, PoseStack stack, MultiBufferSource buffer, int packedLight, boolean transformed) {
        try {
            this.undoLivingRendererTransform(entityRenderer, stack, vehicle, entity, partialTicks, transformed);
            if (!transformed) {
                Vec3 diff = entity.position().subtract(vehicle.position());
                stack.translate(diff.x, diff.y, diff.z);
            }
            entityRenderer.render(entity, 0, partialTicks, stack, buffer, packedLight);
        } catch (Throwable throwable) {
            CrashReport crashReport = CrashReport.forThrowable(throwable, "Rendering entity in world from " + this.getClass());
            CrashReportCategory crashReportCategory = crashReport.addCategory("Entity being rendered");
            entity.fillCrashReportCategory(crashReportCategory);
            CrashReportCategory crashReportCategory2 = crashReport.addCategory("Renderer details");
            crashReportCategory2.setDetail("Assigned renderer", entityRenderer);
            crashReportCategory2.setDetail("Location", CrashReportCategory.formatLocation(entity.level, entity.position().x, entity.position().y, entity.position().z));
            crashReportCategory2.setDetail("Delta", partialTicks);
            throw new ReportedException(crashReport);
        }
    }
}
