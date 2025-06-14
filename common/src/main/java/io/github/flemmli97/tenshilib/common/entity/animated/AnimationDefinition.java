package io.github.flemmli97.tenshilib.common.entity.animated;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

import java.util.Map;

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

    public boolean is(AnimationDefinition... definitions) {
        for (AnimationDefinition other : definitions)
            if (other != null && this.id().equals(other.id()))
                return true;
        return false;
    }

    public boolean is(String... definitions) {
        for (String other : definitions)
            if (this.id().equals(other))
                return true;
        return false;
    }

    @Override
    public boolean equals(Object obj) {
        return obj == this || (obj instanceof AnimationDefinition other && this.id().equals(other.id()));
    }

    @Override
    public int hashCode() {
        return this.id().hashCode();
    }
}
