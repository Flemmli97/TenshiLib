package io.github.flemmli97.tenshilib.patreon.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import io.github.flemmli97.tenshilib.TenshiLib;
import io.github.flemmli97.tenshilib.client.data.GeoAnimationManager;
import io.github.flemmli97.tenshilib.client.data.GeoModelManager;
import io.github.flemmli97.tenshilib.client.data.ReloadableCache;
import io.github.flemmli97.tenshilib.client.model.BedrockAnimations;
import io.github.flemmli97.tenshilib.client.model.ExtendedEntityModel;
import io.github.flemmli97.tenshilib.client.model.ExtendedModel;
import io.github.flemmli97.tenshilib.client.model.ModelPartsContainer;
import io.github.flemmli97.tenshilib.patreon.RenderLocation;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;

public class MeguHatModel extends ExtendedEntityModel<Player> implements ExtendedModel, PatreonModelData<Player> {

    public static final ResourceLocation MEGU_TEXTURE = ResourceLocation.fromNamespaceAndPath(TenshiLib.MODID, "textures/model/megumin_hat.png");

    protected final ReloadableCache<ModelPartsContainer> model;
    protected final ReloadableCache<BedrockAnimations> anim;

    private RenderLocation location;

    public MeguHatModel() {
        this.model = GeoModelManager.getInstance().getModel(ResourceLocation.fromNamespaceAndPath(TenshiLib.MODID, "megumin_hat"));
        this.anim = GeoAnimationManager.getInstance().getAnimation(ResourceLocation.fromNamespaceAndPath(TenshiLib.MODID, "megumin_hat"));
    }

    @Override
    public void setupAnim(Player entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        this.model.get().resetPoses();
        this.anim.get().doAnimation(this, "idle", entity.tickCount, this.getPartialTick());
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer buffer, int packedLight, int packedOverlay, int color) {
        if (RenderLocation.isHead(this.location))
            poseStack.translate(0, 0.1, 0.0);
        this.model.get().getRoot().render(poseStack, buffer, packedLight, packedOverlay, color);
    }

    @Override
    public ModelPartsContainer getModel() {
        return this.model.get();
    }

    @Override
    public void setRenderLocation(RenderLocation loc) {
        this.location = loc;
    }

    @Override
    public ResourceLocation texture(Player entity) {
        return MEGU_TEXTURE;
    }
}
