package io.github.flemmli97.tenshilib.patreon.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import io.github.flemmli97.tenshilib.TenshiLib;
import io.github.flemmli97.tenshilib.client.data.GeoAnimationManager;
import io.github.flemmli97.tenshilib.client.data.GeoModelManager;
import io.github.flemmli97.tenshilib.client.data.ReloadableCache;
import io.github.flemmli97.tenshilib.client.model.BedrockAnimations;
import io.github.flemmli97.tenshilib.client.model.ExtendedEntityModel;
import io.github.flemmli97.tenshilib.client.model.ModelPartsContainer;
import io.github.flemmli97.tenshilib.patreon.RenderLocation;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;

public class CatModel extends ExtendedEntityModel<Player> implements PatreonModelData<Player> {

    public static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(TenshiLib.MODID, "textures/model/cat.png");

    protected final ReloadableCache<ModelPartsContainer> model;
    protected final ReloadableCache<BedrockAnimations> anim;

    private RenderLocation location;

    public CatModel() {
        this.model = GeoModelManager.getInstance().getModel(ResourceLocation.fromNamespaceAndPath(TenshiLib.MODID, "cat"));
        this.anim = GeoAnimationManager.getInstance().getAnimation(ResourceLocation.fromNamespaceAndPath(TenshiLib.MODID, "cat"));
    }

    @Override
    public void setupAnim(Player entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        this.model.get().resetPoses();
        float partialTick = this.getPartialTick();
        if (RenderLocation.isHead(this.location))
            this.anim.get().doAnimation(this, "head", entity.tickCount, partialTick);
        else
            this.anim.get().doAnimation(this, "idle", entity.tickCount, partialTick);
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer buffer, int packedLight, int packedOverlay, int color) {
        poseStack.translate(0, 0.45, 0);
        poseStack.scale(0.7f, 0.7f, 0.7f);
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
        return TEXTURE;
    }
}
