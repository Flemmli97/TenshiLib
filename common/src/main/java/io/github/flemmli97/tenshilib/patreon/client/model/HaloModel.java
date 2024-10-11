package io.github.flemmli97.tenshilib.patreon.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import io.github.flemmli97.tenshilib.TenshiLib;
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

public class HaloModel extends EntityModel<Player> implements ExtendedModel, PatreonModelData<Player> {

    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(TenshiLib.MODID, "halo"), "main");
    public static ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(TenshiLib.MODID, "textures/model/halo.png");

    protected final ModelPartHandler model;

    public HaloModel() {
        this.model = new ModelPartHandler(Minecraft.getInstance().getEntityModels().bakeLayer(HaloModel.LAYER_LOCATION));
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        PartDefinition octagon = partdefinition.addOrReplaceChild("octagon", CubeListBuilder.create().texOffs(10, 0).addBox(-1.6569F, -0.5F, -4.0F, 3.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(0, 9).addBox(-1.6569F, -0.5F, 3.0F, 3.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(5, 1).addBox(3.0F, -0.5F, -1.6569F, 1.0F, 1.0F, 3.0F, new CubeDeformation(0.0F))
                .texOffs(0, 0).addBox(-4.0F, -0.5F, -1.6569F, 1.0F, 1.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 21.0F, 0.0F));

        PartDefinition octagon_r1 = octagon.addOrReplaceChild("octagon_r1", CubeListBuilder.create().texOffs(0, 4).addBox(-4.0F, -0.5F, -1.6569F, 1.0F, 1.0F, 3.0F, new CubeDeformation(0.0F))
                .texOffs(5, 5).addBox(3.0F, -0.5F, -1.6569F, 1.0F, 1.0F, 3.0F, new CubeDeformation(0.0F))
                .texOffs(8, 9).addBox(-1.6569F, -0.5F, 3.0F, 3.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(10, 2).addBox(-1.6569F, -0.5F, -4.0F, 3.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, -0.7854F, 0.0F));

        return LayerDefinition.create(meshdefinition, 32, 32);
    }


    @Override
    public void setupAnim(Player entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        this.model.resetPoses();
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer buffer, int packedLight, int packedOverlay, int color) {
        this.model.getMainPart().render(poseStack, buffer, packedLight, packedOverlay, color);
    }

    @Override
    public ModelPartHandler getHandler() {
        return this.model;
    }

    @Override
    public void setRenderLocation(RenderLocation loc) {
    }

    @Override
    public ResourceLocation texture(Player entity) {
        return TEXTURE;
    }
}
