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

public class CatModel extends EntityModel<Player> implements ExtendedModel, PatreonModelData<Player> {

    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(new ResourceLocation(TenshiLib.MODID, "cat"), "main");
    public static final ResourceLocation TEXTURE = new ResourceLocation(TenshiLib.MODID, "textures/model/cat.png");

    protected final ModelPartHandler model;
    protected final BlockBenchAnimations anim;

    private RenderLocation location;

    public CatModel() {
        this.model = new ModelPartHandler(Minecraft.getInstance().getEntityModels().bakeLayer(CatModel.LAYER_LOCATION));
        this.anim = AnimationManager.getInstance().getAnimation(new ResourceLocation(TenshiLib.MODID, "cat"));
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        PartDefinition body = partdefinition.addOrReplaceChild("body", CubeListBuilder.create().texOffs(0, 0).addBox(-3.0F, -2.0F, -6.5F, 6.0F, 5.0F, 13.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 17.0F, -0.5F));

        PartDefinition head = body.addOrReplaceChild("head", CubeListBuilder.create().texOffs(1, 19).addBox(-2.5F, -4.0F, -2.0F, 5.0F, 4.0F, 5.0F, new CubeDeformation(0.0F))
                .texOffs(0, 6).addBox(-1.5F, -2.0F, -2.5F, 3.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, -7.5F));

        PartDefinition earLeft = head.addOrReplaceChild("earLeft", CubeListBuilder.create().texOffs(1, 10).addBox(-0.5F, -1.25F, -0.5F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offset(1.75F, -3.75F, -0.25F));

        PartDefinition earRight = head.addOrReplaceChild("earRight", CubeListBuilder.create().texOffs(1, 10).mirror().addBox(-0.5F, -1.25F, -0.5F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offset(-1.75F, -3.75F, -0.25F));

        PartDefinition leftFrontLeg = body.addOrReplaceChild("leftFrontLeg", CubeListBuilder.create().texOffs(28, 18).addBox(-1.0F, -2.0F, -1.0F, 2.0F, 6.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(1.75F, 3.0F, -4.5F));

        PartDefinition rightFrontLeg = body.addOrReplaceChild("rightFrontLeg", CubeListBuilder.create().texOffs(8, 28).addBox(-1.0F, -2.0F, -1.0F, 2.0F, 6.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(-1.75F, 3.0F, -4.5F));

        PartDefinition leftBackLeg = body.addOrReplaceChild("leftBackLeg", CubeListBuilder.create().texOffs(0, 28).addBox(-1.0F, -2.0F, -1.0F, 2.0F, 6.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(1.75F, 3.0F, 3.5F));

        PartDefinition rightBackLeg = body.addOrReplaceChild("rightBackLeg", CubeListBuilder.create().texOffs(27, 0).addBox(-1.0F, -2.0F, -1.0F, 2.0F, 6.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(-1.75F, 3.0F, 3.5F));

        PartDefinition tail = body.addOrReplaceChild("tail", CubeListBuilder.create().texOffs(0, 0).addBox(-1.0F, 0.0F, -0.25F, 2.0F, 2.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -1.0F, 6.5F));

        PartDefinition tail2 = tail.addOrReplaceChild("tail2", CubeListBuilder.create().texOffs(18, 22).addBox(-1.0F, 0.0F, 0.0F, 2.0F, 2.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 3.25F));

        return LayerDefinition.create(meshdefinition, 64, 64);
    }

    @Override
    public void setupAnim(Player entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        this.model.resetPoses();
        if (RenderLocation.isHead(this.location))
            this.anim.doAnimation(this, "head", entity.tickCount, Minecraft.getInstance().getFrameTime());
        else
            this.anim.doAnimation(this, "idle", entity.tickCount, Minecraft.getInstance().getFrameTime());
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer buffer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
        poseStack.translate(0, 0.45, 0);
        poseStack.scale(0.7f, 0.7f, 0.7f);
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
        return TEXTURE;
    }
}
