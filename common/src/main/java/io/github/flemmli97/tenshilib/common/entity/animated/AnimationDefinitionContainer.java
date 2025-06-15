package io.github.flemmli97.tenshilib.common.entity.animated;

import com.google.common.collect.ImmutableMap;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

import java.util.Collection;
import java.util.List;
import java.util.Map;

public class AnimationDefinitionContainer {

    public static final StreamCodec<FriendlyByteBuf, AnimationDefinitionContainer> STREAM_CODEC = new StreamCodec<>() {

        @Override
        public AnimationDefinitionContainer decode(FriendlyByteBuf buf) {
            ImmutableMap.Builder<String, AnimationDefinition> builder = ImmutableMap.builder();
            List<AnimationDefinition> list = buf.readList(AnimationDefinition.STREAM_CODEC);
            list.forEach(d -> builder.put(d.id(), d));
            return new AnimationDefinitionContainer(builder.build());
        }

        @Override
        public void encode(FriendlyByteBuf buf, AnimationDefinitionContainer data) {
            buf.writeCollection(data.animations.values(), AnimationDefinition.STREAM_CODEC);
        }
    };

    private final Map<String, AnimationDefinition> animations;

    public AnimationDefinitionContainer(Map<String, AnimationDefinition> animations) {
        this.animations = animations;
    }

    public AnimationDefinition get(String id) {
        return this.animations.get(id);
    }

    public Collection<String> all() {
        return this.animations.keySet();
    }
}
