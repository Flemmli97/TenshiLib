package io.github.flemmli97.tenshilib.patreon.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import io.github.flemmli97.tenshilib.TenshiLib;
import io.github.flemmli97.tenshilib.client.model.ExtendedEntityModel;
import io.github.flemmli97.tenshilib.patreon.RenderLocation;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;

public class MeguHatModel extends ExtendedEntityModel<Player> implements PatreonModelData<Player> {

    public static final ResourceLocation MEGU_TEXTURE = ResourceLocation.fromNamespaceAndPath(TenshiLib.MODID, "textures/model/megumin_hat.png");
    public static final ResourceLocation ASSET_LOCATION = ResourceLocation.fromNamespaceAndPath(TenshiLib.MODID, "megumin_hat");

    private RenderLocation location;

    public MeguHatModel() {
        super(ASSET_LOCATION, ASSET_LOCATION);
    }

    @Override
    public void setupAnim(Player entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        this.model.get().resetPoses();
        this.animation.get().doAnimation(this, "idle", entity.tickCount, this.getPartialTick());
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer buffer, int packedLight, int packedOverlay, int color) {
        if (RenderLocation.isHead(this.location))
            poseStack.translate(0, 0.1, 0.0);
        super.renderToBuffer(poseStack, buffer, packedLight, packedOverlay, color);
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
