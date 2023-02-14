package io.github.flemmli97.tenshilib.patreon.client;

import com.mojang.blaze3d.vertex.PoseStack;
import io.github.flemmli97.tenshilib.patreon.PatreonEffects;
import io.github.flemmli97.tenshilib.patreon.RenderLocation;
import io.github.flemmli97.tenshilib.patreon.client.model.CatModel;
import io.github.flemmli97.tenshilib.patreon.client.model.ChomusukeModel;
import io.github.flemmli97.tenshilib.patreon.client.model.HaloModel;
import io.github.flemmli97.tenshilib.patreon.client.model.MeguHatModel;
import io.github.flemmli97.tenshilib.patreon.client.model.PatreonModelData;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.world.entity.player.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

public class PatreonModelProvider {

    private static final Map<PatreonEffects.PatreonEffectConfig, EffectRenderer<?>> data = new HashMap<>();

    private static final EffectRenderer<MeguHatModel> megumin = register(PatreonEffects.meguHat, new EffectRenderer<>(MeguHatModel::new));
    private static final EffectRenderer<ChomusukeModel> chomusuke = register(PatreonEffects.chomusuke, new EffectRenderer<>(ChomusukeModel::new));
    private static final EffectRenderer<CatModel> cat = register(PatreonEffects.cat, new EffectRenderer<>(CatModel::new));
    private static final EffectRenderer<HaloModel> halo = register(PatreonEffects.halo, new EffectRenderer<>(HaloModel::new) {
        @Override
        public void render(PoseStack stack, MultiBufferSource buffer, int packedLight, Player entity, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch, int red, int green, int blue, int alpha, RenderLocation location) {
            HaloModel model = this.get();
            model.setRenderLocation(location);
            model.setupAnim(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
            int i = LivingEntityRenderer.getOverlayCoords(entity, 0);
            model.renderToBuffer(stack, buffer.getBuffer(alpha != 255 ? RenderType.entityTranslucent(model.texture(entity)) : model.renderType(model.texture(entity))), packedLight, i, red / 255f, green / 255f, blue / 255f, alpha / 255f);
            stack.scale(1.15f, 1.15f, 1.15f);
            stack.translate(0, -0.175, 0);
            model.renderToBuffer(stack, buffer.getBuffer(RenderType.entityTranslucent(model.texture(entity))), packedLight, i, red / 255f, green / 255f, blue / 255f, (alpha * 0.15f) / 255f);
        }
    });

    public static EffectRenderer<?> get(PatreonEffects.PatreonEffectConfig conf) {
        return data.get(conf);
    }

    public static void registerModelLayers(BiConsumer<ModelLayerLocation, Supplier<LayerDefinition>> cons) {
        cons.accept(MeguHatModel.LAYER_LOCATION, MeguHatModel::createBodyLayer);
        cons.accept(ChomusukeModel.LAYER_LOCATION, ChomusukeModel::createBodyLayer);
        cons.accept(CatModel.LAYER_LOCATION, CatModel::createBodyLayer);
        cons.accept(HaloModel.LAYER_LOCATION, HaloModel::createBodyLayer);
    }

    private static <T extends EntityModel<Player> & PatreonModelData<Player>, M extends EffectRenderer<T>> M register(PatreonEffects.PatreonEffectConfig conf, M val) {
        data.put(conf, val);
        return val;
    }

    public static class EffectRenderer<T extends EntityModel<Player> & PatreonModelData<Player>> {

        private final Supplier<T> factory;
        private T val;

        EffectRenderer(Supplier<T> factory) {
            this.factory = factory;
        }

        public T get() {
            if (this.val == null)
                this.val = this.factory.get();
            return this.val;
        }

        public void render(PoseStack stack, MultiBufferSource buffer, int packedLight, Player entity, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch, int red, int green, int blue, int alpha, RenderLocation location) {
            T model = this.get();
            model.setRenderLocation(location);
            model.setupAnim(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
            int i = LivingEntityRenderer.getOverlayCoords(entity, 0);
            model.renderToBuffer(stack, buffer.getBuffer(alpha != 255 ? RenderType.entityTranslucent(model.texture(entity)) : model.renderType(model.texture(entity))), packedLight, i, red / 255f, green / 255f, blue / 255f, alpha / 255f);
        }
    }
}
