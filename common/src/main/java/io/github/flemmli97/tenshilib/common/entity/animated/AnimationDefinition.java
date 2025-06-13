package io.github.flemmli97.tenshilib.common.entity.animated;

import com.google.common.collect.ImmutableMap;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ExtraCodecs;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.DoubleStream;

public record AnimationDefinition(String id, String animation, double length, double speed, boolean shouldRunOut,
                                  int startTransition, int endTransition,
                                  Map<String, double[]> markers) {

    public static final StreamCodec<FriendlyByteBuf, AnimationDefinition> STREAM_CODEC = new StreamCodec<>() {
        @Override
        public AnimationDefinition decode(FriendlyByteBuf buf) {
            return new AnimationDefinition(buf.readUtf(), buf.readUtf(),
                    buf.readDouble(), buf.readDouble(), buf.readBoolean(), buf.readInt(), buf.readInt(),
                    buf.readMap(FriendlyByteBuf::readUtf, b -> {
                        double[] ds = new double[b.readVarInt()];
                        for (int i = 0; i < ds.length; i++) {
                            ds[i] = buf.readDouble();
                        }
                        return ds;
                    }));
        }

        @Override
        public void encode(FriendlyByteBuf buf, AnimationDefinition data) {
            buf.writeUtf(data.id);
            buf.writeUtf(data.animation);
            buf.writeDouble(data.length);
            buf.writeDouble(data.speed);
            buf.writeBoolean(data.shouldRunOut);
            buf.writeInt(data.startTransition);
            buf.writeInt(data.endTransition);
            buf.writeMap(data.markers, FriendlyByteBuf::writeUtf, (b, ds) -> {
                buf.writeVarInt(ds.length);
                for (double d : ds)
                    buf.writeDouble(d);
            });
        }
    };

    private static Map<String, List<Double>> asListMap(Map<String, double[]> markers) {
        Map<String, List<Double>> map = new LinkedHashMap<>();
        markers.forEach((s, ds) -> map.put(s, DoubleStream.of(ds).boxed().toList()));
        return map;
    }

    public PartialDefinition asPartial() {
        return new PartialDefinition(this.animation.equals(this.id) ? "" : this.animation, this.length, this.speed, this.shouldRunOut,
                this.startTransition, this.endTransition, this.markers);
    }

    @Override
    public boolean equals(Object obj) {
        return obj == this || (obj instanceof AnimationDefinition other && this.id().equals(other.id()));
    }

    @Override
    public int hashCode() {
        return this.id().hashCode();
    }

    /**
     * Without the id for de/serialization
     */
    public record PartialDefinition(String animation, double length, double speed, boolean shouldRunOut,
                                    int defaultStartTransition, int defaultEndTransition,
                                    Map<String, double[]> markers) {

        public static Codec<PartialDefinition> CODEC = RecordCodecBuilder.create(inst -> inst.group(
                Codec.STRING.optionalFieldOf("animation").forGetter(d -> d.animation.isEmpty() ? Optional.empty() : Optional.of(d.animation)),
                Codec.DOUBLE.fieldOf("length").forGetter(d -> d.length),
                Codec.DOUBLE.optionalFieldOf("speed").forGetter(d -> d.speed == 1 ? Optional.empty() : Optional.of(d.speed)),
                Codec.BOOL.optionalFieldOf("should_run_out").forGetter(d -> d.shouldRunOut ? Optional.empty() : Optional.of(false)),
                ExtraCodecs.NON_NEGATIVE_INT.optionalFieldOf("start_transition").forGetter(d -> d.defaultStartTransition == 0 ? Optional.empty() : Optional.of(d.defaultStartTransition)),
                ExtraCodecs.NON_NEGATIVE_INT.optionalFieldOf("end_transition").forGetter(d -> d.defaultEndTransition == AnimationHandler.DEFAULT_TRANSIT_TIME ? Optional.empty() : Optional.of(d.defaultEndTransition)),
                Codec.unboundedMap(Codec.STRING, Codec.DOUBLE.listOf()).optionalFieldOf("markers").forGetter(d -> d.markers.isEmpty() ? Optional.empty() : Optional.of(asListMap(d.markers)))
        ).apply(inst, (animation, length, speed, runOut,
                       startTrans, endTrans, markers) -> {
            ImmutableMap.Builder<String, double[]> builder = ImmutableMap.builder();
            markers.ifPresent(m -> m.forEach((s, ds) ->
                    builder.put(s, ds.stream().mapToDouble(d -> d).toArray())));
            return new PartialDefinition(animation.orElse(""), length, speed.orElse(1.), runOut.orElse(true),
                    startTrans.orElse(0), endTrans.orElse(AnimationHandler.DEFAULT_TRANSIT_TIME), builder.build());
        }));

        public AnimationDefinition create(String id) {
            return new AnimationDefinition(id, this.animation.isEmpty() ? id : this.animation, this.length, this.speed, this.shouldRunOut,
                    this.defaultStartTransition, this.defaultEndTransition, this.markers);
        }
    }
}
