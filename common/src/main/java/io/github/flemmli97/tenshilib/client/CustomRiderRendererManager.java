package io.github.flemmli97.tenshilib.client;

import com.google.common.collect.ImmutableSet;
import io.github.flemmli97.tenshilib.client.render.layer.RiderEntityLayer;
import io.github.flemmli97.tenshilib.mixin.LivingEntityRendererAccessor;
import io.github.flemmli97.tenshilib.mixinhelper.EntityRenderDispatcherAccess;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.minecraft.world.entity.EntityType;

import java.util.Map;
import java.util.Set;

/**
 * Checks for all registered renderers to see which ones have a {@link RiderEntityLayer} added
 * and adds those to a list.
 */
public class CustomRiderRendererManager implements ResourceManagerReloadListener {

    private static final CustomRiderRendererManager INSTANCE = new CustomRiderRendererManager();

    public static CustomRiderRendererManager getInstance() {
        return INSTANCE;
    }

    private Set<EntityType<?>> types;

    public boolean hasRiderLayerRenderer(EntityType<?> type) {
        return this.types.contains(type);
    }

    @SuppressWarnings("rawtypes")
    @Override
    public void onResourceManagerReload(ResourceManager resourceManager) {
        Map<EntityType<?>, EntityRenderer<?>> renderers = EntityRenderDispatcherAccess.getAllRenders();
        ImmutableSet.Builder<EntityType<?>> builder = ImmutableSet.builder();
        for (Map.Entry<EntityType<?>, EntityRenderer<?>> entry : renderers.entrySet()) {
            if (entry.getValue() instanceof LivingEntityRenderer lR) {
                for (Object layer : ((LivingEntityRendererAccessor) lR).getLayers()) {
                    if (layer instanceof RiderEntityLayer)
                        builder.add(entry.getKey());
                }
            }
        }
        this.types = builder.build();
    }
}
