package io.github.flemmli97.tenshilib.patreon.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import io.github.flemmli97.tenshilib.TenshiLib;
import io.github.flemmli97.tenshilib.client.AnimationManager;
import io.github.flemmli97.tenshilib.client.model.BlockBenchAnimations;
import io.github.flemmli97.tenshilib.client.model.ExtendedModel;
import io.github.flemmli97.tenshilib.client.model.ModelPartHandler;
import io.github.flemmli97.tenshilib.patreon.RenderLocation;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;

public class MeguHatModel extends EntityModel<Player> implements ExtendedModel, PatreonModelData<Player> {

    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(new ResourceLocation(TenshiLib.MODID, "megu_hat"), "main");
    public static ResourceLocation meguTexture = new ResourceLocation(TenshiLib.MODID, "textures/model/megu_hat.png");

    protected final ModelPartHandler model;
    protected final BlockBenchAnimations anim;

    private RenderLocation location;

    public MeguHatModel() {
        this.model = new ModelPartHandler(Minecraft.getInstance().getEntityModels().bakeLayer(MeguHatModel.LAYER_LOCATION));
        this.anim = AnimationManager.getInstance().getAnimation(new ResourceLocation(TenshiLib.MODID, "megu_hat"));
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        PartDefinition base = partdefinition.addOrReplaceChild("base", CubeListBuilder.create().texOffs(0, 0).addBox(-7.0F, -1.0F, -7.0F, 14.0F, 1.0F, 14.0F, new CubeDeformation(0.0F))
                .texOffs(0, 15).addBox(-5.5F, -2.0F, -5.5F, 11.0F, 1.0F, 11.0F, new CubeDeformation(0.0F))
                .texOffs(0, 27).addBox(-4.5F, -3.0F, -4.5F, 9.0F, 1.0F, 9.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 24.0F, 0.0F));

        PartDefinition layer2 = base.addOrReplaceChild("layer2", CubeListBuilder.create().texOffs(28, 29).addBox(-4.0F, -2.0F, 0.0F, 8.0F, 2.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -3.0F, -4.0F, -0.0873F, 0.0F, 0.0F));

        PartDefinition layer3 = layer2.addOrReplaceChild("layer3", CubeListBuilder.create().texOffs(33, 15).addBox(-3.0F, -4.0F, 0.0F, 6.0F, 4.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -2.0F, 1.0F, -0.1745F, 0.0F, 0.0F));

        PartDefinition layer4 = layer3.addOrReplaceChild("layer4", CubeListBuilder.create().texOffs(0, 37).addBox(-2.0F, -2.0F, 0.0F, 4.0F, 2.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -4.0F, 1.0F, -0.1745F, 0.0F, 0.0F));

        PartDefinition layer5 = layer4.addOrReplaceChild("layer5", CubeListBuilder.create().texOffs(0, 0).addBox(-1.5F, -4.0F, 0.0F, 3.0F, 4.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -2.0F, 0.5F, -0.4363F, 0.0F, 0.0F));

        PartDefinition tip1 = layer5.addOrReplaceChild("tip1", CubeListBuilder.create().texOffs(8, 7).addBox(-0.5F, -4.0F, 0.0F, 1.0F, 4.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -4.0F, 0.5F, -0.5236F, 0.0F, 0.0F));

        PartDefinition tip2 = tip1.addOrReplaceChild("tip2", CubeListBuilder.create().texOffs(0, 18).addBox(-0.5F, -3.0F, 0.0F, 1.0F, 3.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -4.0F, 0.5F, -0.6109F, 0.0F, 0.0F));

        PartDefinition tip3 = tip2.addOrReplaceChild("tip3", CubeListBuilder.create().texOffs(0, 7).addBox(-1.25F, -2.0F, -0.25F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(5, 17).addBox(-1.25F, -3.0F, -0.25F, 2.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.25F, -3.0F, 0.0F, -0.7854F, 0.0F, 0.0F));

        PartDefinition eye = layer3.addOrReplaceChild("eye", CubeListBuilder.create().texOffs(0, 15).addBox(-1.5F, -1.5F, -0.025F, 3.0F, 3.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(3.0F, -1.5F, 0.0F, 0.0F, -0.4363F, 0.0F));

        PartDefinition eye2 = layer3.addOrReplaceChild("eye2", CubeListBuilder.create().texOffs(0, 11).addBox(-1.5F, -1.5F, -0.025F, 3.0F, 3.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-3.0F, -1.5F, 0.0F, 0.0F, 0.4363F, 0.0F));

        return LayerDefinition.create(meshdefinition, 64, 64);
    }

    @Override
    public void setupAnim(Player entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        this.model.resetPoses();
        this.anim.doAnimation(this, "idle", entity.tickCount, Minecraft.getInstance().getFrameTime());
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer buffer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
        if (RenderLocation.isHead(this.location))
            poseStack.translate(0, 0.1, 0.0);
        this.model.getMainPart().render(poseStack, buffer, packedLight, packedOverlay, red, green, blue, alpha);
    }

    @Override
    public ModelPartHandler getHandler() {
        return this.model;
    }

    @Override
    public void setRenderLocation(RenderLocation loc) {
        this.location = loc;
    }

    @Override
    public ResourceLocation texture(Player entity) {
        return meguTexture;
    }
}
