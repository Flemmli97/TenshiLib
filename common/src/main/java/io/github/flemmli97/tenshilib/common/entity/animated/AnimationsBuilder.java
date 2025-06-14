package io.github.flemmli97.tenshilib.common.entity.animated;

import com.google.common.collect.ImmutableMap;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.ExtraCodecs;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.DoubleStream;

public class AnimationsBuilder {

    public static final Codec<AnimationsBuilder> CODEC = Codec.unboundedMap(Codec.STRING, DefinitionBuilder.CODEC)
            .xmap(m -> {
                AnimationsBuilder builder = new AnimationsBuilder();
                builder.definitions.putAll(m);
                return builder;
            }, builder -> builder.definitions);

    private final Map<String, DefinitionBuilder> definitions = new LinkedHashMap<>();
    private boolean built;

    public static DefinitionBuilder definition(double length) {
        return definition(length, true);
    }

    public static DefinitionBuilder definition(double length, boolean seconds) {
        return new DefinitionBuilder(seconds ? length * 20 : length);
    }

    public String add(String id, DefinitionBuilder builder) {
        if (this.built)
            throw new IllegalStateException("Builder has already been built!");
        this.definitions.put(id, builder);
        return id;
    }

    public String add(String id, String copyOf) {
        if (this.built)
            throw new IllegalStateException("Builder has already been built!");
        this.definitions.put(id, this.definitions.get(copyOf).copyWith(copyOf));
        return id;
    }

    public AnimationDefinitionContainer build() {
        this.built = true;
        ImmutableMap.Builder<String, AnimationDefinition> builder = ImmutableMap.builder();
        this.definitions.forEach((id, def) -> {
            AnimationDefinition definition = def.build(id);
            builder.put(definition.id(), definition);
        });
        return new AnimationDefinitionContainer(builder.build());
    }

    public static class DefinitionBuilder {

        public static Codec<DefinitionBuilder> CODEC = RecordCodecBuilder.create(inst -> inst.group(
                Codec.STRING.optionalFieldOf("animation").forGetter(d -> d.animation.isEmpty() ? Optional.empty() : Optional.of(d.animation)),
                Codec.DOUBLE.fieldOf("length").forGetter(d -> d.length),
                Codec.DOUBLE.optionalFieldOf("speed").forGetter(d -> d.speed == 1 ? Optional.empty() : Optional.of(d.speed)),
                Codec.BOOL.optionalFieldOf("should_run_out").forGetter(d -> d.shouldRunOut ? Optional.empty() : Optional.of(false)),
                ExtraCodecs.NON_NEGATIVE_INT.optionalFieldOf("start_transition").forGetter(d -> d.defaultStartTransition == 0 ? Optional.empty() : Optional.of(d.defaultStartTransition)),
                ExtraCodecs.NON_NEGATIVE_INT.optionalFieldOf("end_transition").forGetter(d -> d.defaultEndTransition == AnimationHandler.DEFAULT_TRANSIT_TIME ? Optional.empty() : Optional.of(d.defaultEndTransition)),
                Codec.unboundedMap(Codec.STRING, Codec.DOUBLE.listOf()).optionalFieldOf("markers").forGetter(d -> d.marker.isEmpty() ? Optional.empty() : Optional.of(asListMap(d.marker)))
        ).apply(inst, (animation, length, speed, runOut,
                       startTrans, endTrans, markers) -> {
            DefinitionBuilder builder = new DefinitionBuilder(length)
                    .animationId(animation.orElse(""))
                    .speed(speed.orElse(1.))
                    .withTransitionTime(startTrans.orElse(0), endTrans.orElse(AnimationHandler.DEFAULT_TRANSIT_TIME));
            if (!runOut.orElse(true))
                builder.infinite();
            markers.ifPresent(m -> m.forEach((s, ds) -> builder.marker(s, ds.stream().mapToDouble(d -> d).toArray())));
            return builder;
        }));

        private final double length;

        private String animation;
        private double speed = 1;

        private boolean shouldRunOut = true;
        private int defaultStartTransition;
        private int defaultEndTransition = AnimationHandler.DEFAULT_TRANSIT_TIME;
        private final Map<String, double[]> marker = new LinkedHashMap<>();

        private DefinitionBuilder(double length) {
            this.length = Math.max(1, length);
        }

        private static Map<String, List<Double>> asListMap(Map<String, double[]> markers) {
            Map<String, List<Double>> map = new LinkedHashMap<>();
            markers.forEach((s, ds) -> map.put(s, DoubleStream.of(ds).boxed().toList()));
            return map;
        }

        public DefinitionBuilder animationId(String animation) {
            this.animation = animation;
            return this;
        }

        public DefinitionBuilder speed(double speed) {
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

        public DefinitionBuilder copyWith(String animation) {
            DefinitionBuilder copy = new DefinitionBuilder(this.length)
                    .animationId(animation)
                    .speed(this.speed).withTransitionTime(this.defaultStartTransition, this.defaultEndTransition);
            copy.shouldRunOut = this.shouldRunOut;
            copy.marker.putAll(this.marker);
            return copy;
        }

        public AnimationDefinition build(String id) {
            return new AnimationDefinition(id, this.animation.isEmpty() ? id : this.animation, this.length, this.speed,
                    this.shouldRunOut, this.defaultStartTransition, this.defaultEndTransition, ImmutableMap.copyOf(this.marker));
        }
    }
}
