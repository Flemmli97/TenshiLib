package io.github.flemmli97.tenshilib.client;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import io.github.flemmli97.tenshilib.client.model.BlockBenchAnimations;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Stores BlockBenchAnimations. The files are read under assets/<modid>/animation/entity and assigned the
 * id modid:filename without ".json"
 * Example: an animation ./assets/foo/animation/entity/bar.json has the id foo:bar
 */
public class AnimationManager implements ResourceManagerReloadListener {

    private static final Gson gson = new Gson();

    private static final AnimationManager instance = new AnimationManager();

    public static AnimationManager getInstance() {
        return instance;
    }

    private final Map<ResourceLocation, BlockBenchAnimations> animations = new HashMap<>();

    @Override
    public void onResourceManagerReload(ResourceManager resourceManager) {
        Collection<ResourceLocation> anims = resourceManager.listResources("animation/entity", s -> s.endsWith(".json"));
        for (ResourceLocation res : anims) {
            try {
                InputStream input = resourceManager.getResource(res).getInputStream();
                JsonObject obj = gson.getAdapter(JsonObject.class).read(gson.newJsonReader(new InputStreamReader(input)));
                ResourceLocation animID = new ResourceLocation(res.getNamespace(), res.getPath().replace("animation/entity/", "").replace(".json", ""));
                BlockBenchAnimations anim = this.getAnimation(animID);
                anim.reload(obj);
            } catch (IOException e) {
                e.fillInStackTrace();
            }
        }
    }

    public BlockBenchAnimations getAnimation(ResourceLocation res) {
        return this.animations.computeIfAbsent(res, r -> new BlockBenchAnimations());
    }
}
