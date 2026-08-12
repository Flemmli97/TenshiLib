package io.github.flemmli97.tenshilib.patreon.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import io.github.flemmli97.tenshilib.TenshiLib;
import io.github.flemmli97.tenshilib.client.model.ExtendedEntityModel;
import io.github.flemmli97.tenshilib.patreon.RenderLocation;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;

public class ChomusukeModel extends ExtendedEntityModel<Player> implements PatreonModelData<Player> {

    public static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(TenshiLib.MODID, "textures/model/chomusuke.png");
    public static final ResourceLocation TEXTURE_SLEEPY = ResourceLocation.fromNamespaceAndPath(TenshiLib.MODID, "textures/model/chomusuke_sleepy.png");
    public static final ResourceLocation ASSET_LOCATION = ResourceLocation.fromNamespaceAndPath(TenshiLib.MODID, "chomusuke");

    private RenderLocation location;

    public ChomusukeModel() {
        super(ASSET_LOCATION, ASSET_LOCATION);
    }

    @Override
    public void setupAnim(Player entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        this.model.get().resetPoses();
        float partialTick = this.getPartialTick();
        if (RenderLocation.isHead(this.location))
            this.animation.get().doAnimation(this, "head", entity.tickCount, partialTick);
        else
            this.animation.get().doAnimation(this, "idle", entity.tickCount, partialTick);
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer buffer, int packedLight, int packedOverlay, int color) {
        poseStack.translate(0, 0.45, 0);
        poseStack.scale(0.7f, 0.7f, 0.7f);
        super.renderToBuffer(poseStack, buffer, packedLight, packedOverlay, color);
    }

    @Override
    public void setRenderLocation(RenderLocation loc) {
        this.location = loc;
    }

    @Override
    public ResourceLocation texture(Player entity) {
        return RenderLocation.isHead(this.location) ? TEXTURE_SLEEPY : TEXTURE;
    }
}
