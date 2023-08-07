package io.github.flemmli97.tenshilib.common.entity;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.syncher.EntityDataSerializer;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.world.phys.Vec3;

import java.util.Optional;

public class CustomDataSerializers {

    private static boolean registered = false;

    public static final EntityDataSerializer<Vec3> VEC = new EntityDataSerializer<Vec3>() {

        @Override
        public void write(FriendlyByteBuf buffer, Vec3 value) {
            buffer.writeDouble(value.x());
            buffer.writeDouble(value.y());
            buffer.writeDouble(value.z());
        }

        @Override
        public Vec3 read(FriendlyByteBuf buffer) {
            return new Vec3(buffer.readDouble(), buffer.readDouble(), buffer.readDouble());
        }

        @Override
        public Vec3 copy(Vec3 value) {
            return value;
        }
    };

    public static final EntityDataSerializer<Optional<Vec3>> OPTIONAL_VEC = new EntityDataSerializer<Optional<Vec3>>() {

        @Override
        public void write(FriendlyByteBuf buffer, Optional<Vec3> value) {
            buffer.writeBoolean(value.isPresent());
            value.ifPresent(vec -> {
                buffer.writeDouble(vec.x());
                buffer.writeDouble(vec.y());
                buffer.writeDouble(vec.z());
            });
        }

        @Override
        public Optional<Vec3> read(FriendlyByteBuf buffer) {
            if (!buffer.readBoolean()) {
                return Optional.empty();
            }
            return Optional.of(new Vec3(buffer.readDouble(), buffer.readDouble(), buffer.readDouble()));
        }

        @Override
        public Optional<Vec3> copy(Optional<Vec3> value) {
            return value;
        }
    };

    public static void register() {
        if (registered)
            return;
        registered = true;
        EntityDataSerializers.registerSerializer(VEC);
        EntityDataSerializers.registerSerializer(OPTIONAL_VEC);
    }
}
