package io.github.flemmli97.tenshilib.fabric.client;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import io.github.flemmli97.tenshilib.fabric.mixin.BlockModelAccessor;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.BlockModel;
import net.minecraft.client.renderer.block.model.ItemModelGenerator;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.Material;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.client.resources.model.ModelState;
import net.minecraft.client.resources.model.UnbakedModel;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.function.Function;

/**
 * Based on NeoForges SeparateTransformsModel.
 */
public class SeparateTransformsModel extends BlockModel {

    private static final ItemModelGenerator ITEM_MODEL_GENERATOR = new ItemModelGenerator();

    private final BlockModel base;
    private final ImmutableMap<ItemDisplayContext, BlockModel> perspectives;

    public SeparateTransformsModel(BlockModel base, ImmutableMap<ItemDisplayContext, BlockModel> perspectives) {
        super(((BlockModelAccessor) base).tenshilib$getParentLocation(), ((BlockModelAccessor) base).tenshilib$getElements(), ((BlockModelAccessor) base).tenshilib$getTextureMap(),
                base.hasAmbientOcclusion(), base.getGuiLight(), base.getTransforms(), base.getOverrides());
        this.base = base;
        this.perspectives = perspectives;
    }

    public static BakedModel bake(BlockModel model, ModelBaker baker, BlockModel owner, Function<Material, TextureAtlasSprite> spriteGetter, ModelState state, boolean guiLight3d) {
        if (model.getRootModel() == ModelBakery.GENERATION_MARKER)
            return ITEM_MODEL_GENERATOR.generateBlockModel(spriteGetter, model).bake(baker, model, spriteGetter, state, guiLight3d);
        return model.bake(baker, owner, spriteGetter, state, guiLight3d);
    }

    @Override
    public BlockModel getRootModel() {
        return this;
    }

    @Override
    public Collection<ResourceLocation> getDependencies() {
        Set<ResourceLocation> set = Sets.newHashSet();
        set.addAll(this.base.getDependencies());
        this.perspectives.values().forEach(model -> set.addAll(model.getDependencies()));
        return set;
    }

    @Override
    public void resolveParents(Function<ResourceLocation, UnbakedModel> resolver) {
        this.base.resolveParents(resolver);
        this.perspectives.values().forEach(model -> model.resolveParents(resolver));
    }

    @Override
    public BakedModel bake(ModelBaker baker, BlockModel model, Function<Material, TextureAtlasSprite> spriteGetter, ModelState state, boolean guiLight3d) {
        BakedModel base = bake(this.base, baker, this.base, spriteGetter, state, guiLight3d);
        return new Baked(base, ImmutableMap.copyOf(Maps.transformValues(this.perspectives,
                value -> bake(value, baker, value, spriteGetter, state, guiLight3d))));
    }

    public static class Baked implements BakedModel {

        private final BakedModel baseModel;
        private final ImmutableMap<ItemDisplayContext, BakedModel> perspectives;

        public Baked(BakedModel baseModel, ImmutableMap<ItemDisplayContext, BakedModel> perspectives) {
            this.baseModel = baseModel;
            this.perspectives = perspectives;
        }

        @Override
        public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction direction, RandomSource random) {
            return this.baseModel.getQuads(state, direction, random);
        }

        @Override
        public boolean useAmbientOcclusion() {
            return this.baseModel.useAmbientOcclusion();
        }

        @Override
        public boolean isGui3d() {
            return this.baseModel.isGui3d();
        }

        @Override
        public boolean usesBlockLight() {
            return this.baseModel.usesBlockLight();
        }

        @Override
        public boolean isCustomRenderer() {
            return false;
        }

        @Override
        public TextureAtlasSprite getParticleIcon() {
            return this.baseModel.getParticleIcon();
        }

        @Override
        public ItemTransforms getTransforms() {
            return this.baseModel.getTransforms();
        }

        @Override
        public ItemOverrides getOverrides() {
            return this.baseModel.getOverrides();
        }

        public BakedModel getContextModel(ItemDisplayContext context) {
            if (this.perspectives.containsKey(context)) {
                BakedModel model = this.perspectives.get(context);
                if (model != null) {
                    return model;
                }
            }
            return this.baseModel;
        }
    }
}
