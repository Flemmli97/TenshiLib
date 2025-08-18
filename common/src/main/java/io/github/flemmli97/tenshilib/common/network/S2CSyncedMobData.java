package io.github.flemmli97.tenshilib.common.network;

import io.github.flemmli97.tenshilib.TenshiLib;
import io.github.flemmli97.tenshilib.common.entity.data.SyncedDataContainer;
import io.github.flemmli97.tenshilib.common.entity.data.SyncedMobDataHandler;
import io.github.flemmli97.tenshilib.loader.LoaderNetwork;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.VarInt;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;

import java.util.ArrayList;
import java.util.List;

public class S2CSyncedMobData implements CustomPacketPayload {

    public static final Type<S2CSyncedMobData> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(TenshiLib.MODID, "s2c_synced_mob_data"));

    public static final StreamCodec<RegistryFriendlyByteBuf, S2CSyncedMobData> STREAM_CODEC = new StreamCodec<>() {
        @Override
        public S2CSyncedMobData decode(RegistryFriendlyByteBuf buf) {
            int entity = buf.readInt();
            int size = VarInt.read(buf);
            List<SyncedDataContainer.SyncedContainer<?>> list = new ArrayList<>();
            for (int i = 0; i < size; i++) {
                list.add(i, SyncedDataContainer.SyncedContainer.from(buf));
            }
            return new S2CSyncedMobData(entity, list);
        }

        @Override
        public void encode(RegistryFriendlyByteBuf buf, S2CSyncedMobData pkt) {
            buf.writeInt(pkt.entity);
            VarInt.write(buf, pkt.values.size());
            pkt.values.forEach(cont -> cont.write(buf));
        }
    };

    private final int entity;
    private final List<SyncedDataContainer.SyncedContainer<?>> values;

    private S2CSyncedMobData(int entity, List<SyncedDataContainer.SyncedContainer<?>> values) {
        this.entity = entity;
        this.values = values;
    }

    public static <T extends Entity & SyncedMobDataHandler> void send(T entity, List<SyncedDataContainer.SyncedContainer<?>> data) {
        if (!entity.level().isClientSide && !data.isEmpty())
            LoaderNetwork.INSTANCE.sendToTracking(new S2CSyncedMobData(entity.getId(), data), entity);
    }

    public static <T extends Entity & SyncedMobDataHandler> void sendTo(T entity, List<SyncedDataContainer.SyncedContainer<?>> data, ServerPlayer player) {
        if (!entity.level().isClientSide && !data.isEmpty())
            LoaderNetwork.INSTANCE.sendToPlayer(new S2CSyncedMobData(entity.getId(), data), player);
    }

    public static void handle(S2CSyncedMobData pkt, Player player) {
        Entity entity = player.level().getEntity(pkt.entity);
        if (entity instanceof SyncedMobDataHandler handler) {
            handler.getDataContainer().update(pkt.values);
        }
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
