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
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;

/**
 * Add to any entity renderer where the rider should adjust according to the entities model
 */
public class RiderLayerRenderer<T extends LivingEntity, M extends EntityModel<T> & RideableModel<T>> extends RenderLayer<T, M> {

    public RiderLayerRenderer(RenderLayerParent<T, M> renderer) {
        super(renderer);
    }

    @Override
    public void render(PoseStack stack, MultiBufferSource buffer, int light, T entity, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch) {
        for (int i = 0; i < entity.getPassengers().size(); i++) {
            Entity rider = entity.getPassengers().get(i);
            if (rider == null || (Minecraft.getInstance().cameraEntity == rider && Minecraft.getInstance().options.getCameraType() == CameraType.FIRST_PERSON))
                continue;
            ClientHandlers.RIDING_RENDER_BLACKLIST.add(rider.getUUID());
            stack.pushPose();
            this.getParentModel().transform(entity, rider, stack, i);
            this.renderPassenger(entity, rider, partialTicks, stack, buffer, light);
            stack.popPose();
            ClientHandlers.RIDING_RENDER_BLACKLIST.remove(rider.getUUID());
        }
    }

    /**
     * Undo transforms of things in the given EntityRenderer so its not applied twice
     */
    protected <E extends Entity> void undoLivingRendererTransform(EntityRenderer<E> entityRenderer, PoseStack stack, T entity, E rider, float partialTicks) {
        float riderRot = Mth.rotLerp(partialTicks, entity.yBodyRotO, entity.yBodyRot);
        stack.scale(-1.0f, -1.0f, 1.0f);
        stack.mulPose(Vector3f.YP.rotationDegrees(riderRot + 180));
    }

    @SuppressWarnings("unchecked")
    public <E extends Entity> void renderPassenger(T vehicle, E entity, float partialTicks, PoseStack stack, MultiBufferSource buffer, int packedLight) {
        EntityRenderDispatcher dispatcher = Minecraft.getInstance().getEntityRenderDispatcher();
        EntityRenderer<E> entityRenderer = (EntityRenderer<E>) dispatcher.getRenderer(entity);
        try {
            this.undoLivingRendererTransform(entityRenderer, stack, vehicle, entity, partialTicks);
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
