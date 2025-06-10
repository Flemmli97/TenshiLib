package io.github.flemmli97.tenshilib.patreon.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import io.github.flemmli97.tenshilib.TenshiLib;
import io.github.flemmli97.tenshilib.client.data.AnimationManager;
import io.github.flemmli97.tenshilib.client.data.ModelManager;
import io.github.flemmli97.tenshilib.client.data.ReloadableCache;
import io.github.flemmli97.tenshilib.client.model.BedrockAnimations;
import io.github.flemmli97.tenshilib.client.model.ExtendedModel;
import io.github.flemmli97.tenshilib.client.model.ModelPartsHolder;
import io.github.flemmli97.tenshilib.client.render.RenderUtils;
import io.github.flemmli97.tenshilib.patreon.RenderLocation;
import net.minecraft.client.model.EntityModel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;

public class CatModel extends EntityModel<Player> implements ExtendedModel, PatreonModelData<Player> {

    public static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(TenshiLib.MODID, "textures/model/cat.png");

    protected final ReloadableCache<ModelPartsHolder> model;
    protected final BedrockAnimations anim;

    private RenderLocation location;

    public CatModel() {
        this.model = ModelManager.getInstance().getModel(ResourceLocation.fromNamespaceAndPath(TenshiLib.MODID, "cat"));
        this.anim = AnimationManager.getInstance().getAnimation(ResourceLocation.fromNamespaceAndPath(TenshiLib.MODID, "cat"));
    }

    @Override
    public void setupAnim(Player entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        this.model.get().resetPoses();
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
        this.model.get().getMainPart().render(poseStack, buffer, packedLight, packedOverlay, color);
    }

    @Override
    public ModelPartsHolder getHandler() {
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
