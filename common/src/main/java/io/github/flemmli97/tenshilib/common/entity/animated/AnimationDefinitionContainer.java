package io.github.flemmli97.tenshilib.common.entity.animated;

import com.google.common.collect.ImmutableMap;
import com.mojang.serialization.Codec;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class AnimationDefinitionContainer {

    public static final Codec<AnimationDefinitionContainer> CODEC = Codec.unboundedMap(Codec.STRING, AnimationDefinition.PartialDefinition.CODEC)
            .xmap(m -> {
                        ImmutableMap.Builder<String, AnimationDefinition> builder = ImmutableMap.builder();
                        m.forEach((s, partial) -> builder.put(s, partial.create(s)));
                        return new AnimationDefinitionContainer(builder.build());
                    }, holder -> holder.animations.entrySet().stream().collect(Collectors.toMap(
                            Map.Entry::getKey,
                            e -> e.getValue().asPartial(),
                            (e1, e2) -> e1,
                            LinkedHashMap::new))
            );
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

    public static class Builder {

        Map<String, AnimationDefinition> definitions = new LinkedHashMap<>();

        /**
         * @param name   ID of the animation
         * @param length length in seconds
         */
        public DefinitionBuilder add(String name, double length) {
            return this.add(name, length, true);
        }

        public DefinitionBuilder add(String name, double length, boolean seconds) {
            return new DefinitionBuilder(name, (seconds ? length * 20 : length));
        }

        public AnimationDefinitionContainer build() {
            return new AnimationDefinitionContainer(this.definitions);
        }

        public class DefinitionBuilder {

            private final String id;
            private final double length;

            private String animation;
            private double speed = 1;

            private boolean shouldRunOut = true;
            private int defaultStartTransition;
            private int defaultEndTransition = AnimationHandler.DEFAULT_TRANSIT_TIME;
            private final Map<String, double[]> marker = new LinkedHashMap<>();

            public DefinitionBuilder(String id, double length) {
                this.id = id;
                this.length = Math.max(1, length);
            }

            public DefinitionBuilder animationId(String animation) {
                this.animation = animation;
                return this;
            }

            public DefinitionBuilder speed(float speed) {
                this.speed = speed;
                return this;
            }

            public DefinitionBuilder infinite() {
                this.shouldRunOut = false;
                return this;
            }

            public DefinitionBuilder withTransitionTime(int start, int end) {
                this.defaultStartTransition = start;
                this.defaultEndTransition = end;
                return this;
            }

            public DefinitionBuilder marker(String identifier, double... time) {
                this.marker.put(identifier, time);
                return this;
            }

            public Builder build() {
                AnimationDefinition definition = new AnimationDefinition(this.id, this.animation, this.length, this.speed,
                        this.shouldRunOut, this.defaultStartTransition, this.defaultEndTransition, this.marker);
                Builder.this.definitions.put(definition.id(), definition);
                return Builder.this;
            }
        }
    }
}
