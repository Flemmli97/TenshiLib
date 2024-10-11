package io.github.flemmli97.tenshilib.patreon.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import io.github.flemmli97.tenshilib.TenshiLib;
import io.github.flemmli97.tenshilib.client.AnimationManager;
import io.github.flemmli97.tenshilib.client.model.BlockBenchAnimations;
import io.github.flemmli97.tenshilib.client.model.ExtendedModel;
import io.github.flemmli97.tenshilib.client.model.ModelPartHandler;
import io.github.flemmli97.tenshilib.client.render.RenderUtils;
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

public class ChomusukeModel extends EntityModel<Player> implements ExtendedModel, PatreonModelData<Player> {

    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(TenshiLib.MODID, "chomusuke"), "main");
    public static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(TenshiLib.MODID, "textures/model/chomusuke.png");
    public static final ResourceLocation TEXTURE_SLEEPY = ResourceLocation.fromNamespaceAndPath(TenshiLib.MODID, "textures/model/chomusuke_sleepy.png");

    protected final ModelPartHandler model;
    protected final BlockBenchAnimations anim;

    private RenderLocation location;

    public ChomusukeModel() {
        this.model = new ModelPartHandler(Minecraft.getInstance().getEntityModels().bakeLayer(ChomusukeModel.LAYER_LOCATION));
        this.anim = AnimationManager.getInstance().getAnimation(ResourceLocation.fromNamespaceAndPath(TenshiLib.MODID, "chomusuke"));
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        PartDefinition body = partdefinition.addOrReplaceChild("body", CubeListBuilder.create().texOffs(0, 0).addBox(-3.5F, -3.0F, -6.5F, 7.0F, 6.0F, 13.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 17.0F, -0.5F));

        PartDefinition head = body.addOrReplaceChild("head", CubeListBuilder.create().texOffs(0, 19).addBox(-3.0F, -5.0F, -3.0F, 6.0F, 5.0F, 6.0F, new CubeDeformation(0.0F))
                .texOffs(0, 6).addBox(-2.0F, -2.0F, -4.0F, 4.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -2.0F, -6.5F));

        PartDefinition earLeft = head.addOrReplaceChild("earLeft", CubeListBuilder.create().texOffs(0, 0).addBox(-0.5F, -1.25F, -0.5F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offset(1.5F, -4.75F, -1.5F));

        PartDefinition earRight = head.addOrReplaceChild("earRight", CubeListBuilder.create().texOffs(0, 0).mirror().addBox(-0.5F, -1.25F, -0.5F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offset(-1.5F, -4.75F, -1.5F));

        PartDefinition leftFrontLeg = body.addOrReplaceChild("leftFrontLeg", CubeListBuilder.create().texOffs(8, 30).addBox(-1.0F, -2.0F, -1.0F, 2.0F, 6.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(2.0F, 3.0F, -4.5F));

        PartDefinition rightFrontLeg = body.addOrReplaceChild("rightFrontLeg", CubeListBuilder.create().texOffs(0, 30).addBox(-1.0F, -2.0F, -1.0F, 2.0F, 6.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(-2.0F, 3.0F, -4.5F));

        PartDefinition leftBackLeg = body.addOrReplaceChild("leftBackLeg", CubeListBuilder.create().texOffs(28, 19).addBox(-1.0F, -2.0F, -1.0F, 2.0F, 6.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(2.0F, 3.0F, 3.5F));

        PartDefinition rightBackLeg = body.addOrReplaceChild("rightBackLeg", CubeListBuilder.create().texOffs(27, 0).addBox(-1.0F, -2.0F, -1.0F, 2.0F, 6.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(-2.0F, 3.0F, 3.5F));

        PartDefinition tail = body.addOrReplaceChild("tail", CubeListBuilder.create().texOffs(0, 0).addBox(-1.0F, 0.0F, -0.25F, 2.0F, 2.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -2.0F, 6.5F));

        PartDefinition tail2 = tail.addOrReplaceChild("tail2", CubeListBuilder.create().texOffs(18, 24).addBox(-1.0F, 0.0F, 0.0F, 2.0F, 2.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 3.25F));

        PartDefinition leftWing = body.addOrReplaceChild("leftWing", CubeListBuilder.create().texOffs(0, 38).addBox(-1.0F, -3.0F, 0.0F, 7.0F, 6.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offset(3.5F, -3.0F, -1.0F));

        PartDefinition leftWing2 = body.addOrReplaceChild("leftWing2", CubeListBuilder.create().texOffs(0, 38).mirror().addBox(-6.0F, -3.0F, 0.0F, 7.0F, 6.0F, 0.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offset(-3.5F, -3.0F, -1.0F));

        return LayerDefinition.create(meshdefinition, 64, 64);
    }

    @Override
    public void setupAnim(Player entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        this.model.resetPoses();
        float partialTicks = RenderUtils.getPartialTicks(entity);
        if (RenderLocation.isHead(this.location))
            this.anim.doAnimation(this, "head", entity.tickCount, partialTicks);
        else
            this.anim.doAnimation(this, "idle", entity.tickCount, partialTicks);
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer buffer, int packedLight, int packedOverlay, int color) {
        poseStack.translate(0, 0.45, 0);
        poseStack.scale(0.7f, 0.7f, 0.7f);
        this.model.getMainPart().render(poseStack, buffer, packedLight, packedOverlay, color);
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
        return RenderLocation.isHead(this.location) ? TEXTURE_SLEEPY : TEXTURE;
    }
}
