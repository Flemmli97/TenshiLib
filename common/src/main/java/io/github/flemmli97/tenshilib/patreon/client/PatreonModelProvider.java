package io.github.flemmli97.tenshilib.patreon.client;

import com.mojang.blaze3d.vertex.PoseStack;
import io.github.flemmli97.tenshilib.patreon.RenderLocation;
import io.github.flemmli97.tenshilib.patreon.client.model.CatModel;
import io.github.flemmli97.tenshilib.patreon.client.model.ChomusukeModel;
import io.github.flemmli97.tenshilib.patreon.client.model.HaloModel;
import io.github.flemmli97.tenshilib.patreon.client.model.MeguHatModel;
import io.github.flemmli97.tenshilib.patreon.client.model.PatreonModelData;
import io.github.flemmli97.tenshilib.patreon.effects.PatreonEffectConfig;
import io.github.flemmli97.tenshilib.patreon.effects.PatreonEffects;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.util.FastColor;
import net.minecraft.world.entity.player.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

public class PatreonModelProvider {

    private static final Map<PatreonEffectConfig, EffectRenderer<?>> DATA = new HashMap<>();

    private static final EffectRenderer<MeguHatModel> MEGUMIN = register(PatreonEffects.MEGU_HAT, new EffectRenderer<>(MeguHatModel::new));
    private static final EffectRenderer<ChomusukeModel> CHOMUSUKE = register(PatreonEffects.CHOMUSUKE, new EffectRenderer<>(ChomusukeModel::new));
    private static final EffectRenderer<CatModel> CAT = register(PatreonEffects.CAT, new EffectRenderer<>(CatModel::new));
    private static final EffectRenderer<HaloModel> HALO = register(PatreonEffects.HALO, new EffectRenderer<>(HaloModel::new) {
        @Override
        public void render(PoseStack stack, MultiBufferSource buffer, int packedLight, Player entity, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch, int color, RenderLocation location) {
            HaloModel model = this.get();
            model.setRenderLocation(location);
            model.setupAnim(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
            int i = LivingEntityRenderer.getOverlayCoords(entity, 0);
            int alpha = FastColor.ARGB32.alpha(color);
            model.renderToBuffer(stack, buffer.getBuffer(alpha != 255 ? RenderType.entityTranslucent(model.texture(entity)) : model.renderType(model.texture(entity))), packedLight, i, color);
            stack.scale(1.15f, 1.15f, 1.15f);
            stack.translate(0, -0.175, 0);
            model.renderToBuffer(stack, buffer.getBuffer(RenderType.entityTranslucent(model.texture(entity))), packedLight, i, FastColor.ARGB32.color((int) (alpha * 0.15f), color));
        }
    });

    public static EffectRenderer<?> get(PatreonEffectConfig conf) {
        return DATA.get(conf);
    }

    public static void registerModelLayers(BiConsumer<ModelLayerLocation, Supplier<LayerDefinition>> cons) {
        cons.accept(MeguHatModel.LAYER_LOCATION, MeguHatModel::createBodyLayer);
        cons.accept(ChomusukeModel.LAYER_LOCATION, ChomusukeModel::createBodyLayer);
        cons.accept(CatModel.LAYER_LOCATION, CatModel::createBodyLayer);
        cons.accept(HaloModel.LAYER_LOCATION, HaloModel::createBodyLayer);
    }

    private static <T extends EntityModel<Player> & PatreonModelData<Player>, M extends EffectRenderer<T>> M register(PatreonEffectConfig conf, M val) {
        DATA.put(conf, val);
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

        public void render(PoseStack stack, MultiBufferSource buffer, int packedLight, Player entity, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch, int color, RenderLocation location) {
            T model = this.get();
            model.setRenderLocation(location);
            model.setupAnim(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
            int i = LivingEntityRenderer.getOverlayCoords(entity, 0);
            model.renderToBuffer(stack, buffer.getBuffer((color >> 24 & 0xFF) != 255 ? RenderType.entityTranslucent(model.texture(entity)) : model.renderType(model.texture(entity))), packedLight, i, color);
        }
    }
}
