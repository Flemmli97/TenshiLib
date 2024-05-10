package io.github.flemmli97.tenshilib.forge.platform.patreon;

import io.github.flemmli97.tenshilib.TenshiLib;
import io.github.flemmli97.tenshilib.forge.network.PacketHandler;
import io.github.flemmli97.tenshilib.patreon.PatreonDataManager;
import io.github.flemmli97.tenshilib.patreon.PatreonPlatform;
import io.github.flemmli97.tenshilib.patreon.PatreonPlayerSetting;
import io.github.flemmli97.tenshilib.patreon.pkts.S2CEffectUpdatePkt;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.attachment.IAttachmentHolder;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.common.util.INBTSerializable;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

public class PatreonImpl implements PatreonPlatform {

    private static final DeferredRegister<AttachmentType<?>> ATTACHMENT_TYPES = DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, TenshiLib.MODID);

    private static final DeferredHolder<AttachmentType<?>, AttachmentType<PlayerCap>> PATREON_DATA = ATTACHMENT_TYPES.register("patreon_data", () -> AttachmentType.serializable(PlayerCap::new).build());

    public static void initPatreonData(IEventBus modBus) {
        ATTACHMENT_TYPES.register(modBus);
        NeoForge.EVENT_BUS.addListener(PatreonImpl::onLogin);
        NeoForge.EVENT_BUS.addListener(PatreonImpl::playerClone);
        NeoForge.EVENT_BUS.addListener(PatreonImpl::track);
        NeoForge.EVENT_BUS.addListener(PatreonImpl::tick);
        PatreonDataManager.init();
    }

    public static void onLogin(EntityJoinLevelEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            PatreonPlatform.INSTANCE.playerSettings(player).setToDefault(false);
            PatreonPlatform.INSTANCE.sendToClient(player, player);
        }
    }

    public static void playerClone(PlayerEvent.Clone event) {
        if (event.getEntity() instanceof ServerPlayer serverPlayer) {
            PatreonPlayerSetting setting = PatreonPlatform.INSTANCE.playerSettings(serverPlayer);
            setting.read(PatreonPlatform.INSTANCE.playerSettings(event.getOriginal()).save(new CompoundTag()));
            PatreonPlatform.INSTANCE.sendToClient(serverPlayer, serverPlayer);
        }
    }

    public static void tick(PlayerTickEvent.Post event) {
        PatreonPlatform.INSTANCE.playerSettings(event.getEntity()).tick(event.getEntity());
    }

    public static void track(PlayerEvent.StartTracking event) {
        if (event.getTarget() instanceof ServerPlayer targetPlayer)
            PatreonPlatform.INSTANCE.sendToClient((ServerPlayer) event.getEntity(), targetPlayer);
    }

    @Override
    public PatreonPlayerSetting playerSettings(Player player) {
        return player.getData(PATREON_DATA);
    }

    @Override
    public void sendToClient(ServerPlayer player, ServerPlayer target) {
        PatreonPlayerSetting setting = PatreonPlatform.INSTANCE.playerSettings(target);
        if (PatreonDataManager.get(target.getUUID().toString()).tier() < 1)
            setting.setEffect(null);
        PacketHandler.sendToClientChecked(new S2CEffectUpdatePkt(target.getId(), setting.effect() != null ? setting.effect().id() : "", setting.shouldRender(), setting.getRenderLocation(), setting.getColor()), player);
    }

    @Override
    public void sendToTracking(ServerPlayer player, S2CEffectUpdatePkt pkt) {
        PacketHandler.sendToTracking(pkt, player);
    }

    public static class PlayerCap extends PatreonPlayerSetting implements INBTSerializable<CompoundTag> {

        public PlayerCap(IAttachmentHolder player) {
            super(tryCastTo(player));
        }

        private static Player tryCastTo(IAttachmentHolder holder) {
            if (holder instanceof Player player)
                return player;
            throw new IllegalStateException("Attachment only supported for player");
        }

        @Override
        public CompoundTag serializeNBT(HolderLookup.Provider provider) {
            return this.save(new CompoundTag());
        }

        @Override
        public void deserializeNBT(HolderLookup.Provider provider, CompoundTag tag) {
            this.read(tag);
        }
    }
}
