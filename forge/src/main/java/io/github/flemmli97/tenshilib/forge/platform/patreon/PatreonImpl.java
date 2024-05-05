package io.github.flemmli97.tenshilib.forge.platform.patreon;

import io.github.flemmli97.tenshilib.TenshiLib;
import io.github.flemmli97.tenshilib.forge.network.PacketHandler;
import io.github.flemmli97.tenshilib.patreon.PatreonDataManager;
import io.github.flemmli97.tenshilib.patreon.PatreonPlatform;
import io.github.flemmli97.tenshilib.patreon.PatreonPlayerSetting;
import io.github.flemmli97.tenshilib.patreon.pkts.S2CEffectUpdatePkt;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

import java.util.Optional;

public class PatreonImpl implements PatreonPlatform {

    private static final ResourceLocation CAP_ID = new ResourceLocation(TenshiLib.MODID, "patreon");

    private static final Capability<PatreonPlayerSetting> PATREONPLAYER = CapabilityManager.get(new CapabilityToken<>() {
    });

    public static void initPatreonData() {
        IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();
        modBus.addListener(PatreonImpl::register);
        MinecraftForge.EVENT_BUS.addGenericListener(Entity.class, PatreonImpl::cap);
        MinecraftForge.EVENT_BUS.addListener(PatreonImpl::onLogin);
        MinecraftForge.EVENT_BUS.addListener(PatreonImpl::playerClone);
        MinecraftForge.EVENT_BUS.addListener(PatreonImpl::track);
        MinecraftForge.EVENT_BUS.addListener(PatreonImpl::tick);
        PatreonDataManager.init();
    }

    public static void register(RegisterCapabilitiesEvent event) {
        event.register(PatreonPlayerSetting.class);
    }

    public static void cap(AttachCapabilitiesEvent<Entity> event) {
        if (event.getObject() instanceof Player player)
            event.addCapability(CAP_ID, new PlayerCap(player));
    }

    public static void onLogin(EntityJoinWorldEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            PatreonPlatform.INSTANCE.playerSettings(player)
                    .ifPresent(setting -> setting.setToDefault(false));
            PatreonPlatform.INSTANCE.sendToClient(player, player);
        }
    }

    public static void playerClone(PlayerEvent.Clone event) {
        if (event.getPlayer() instanceof ServerPlayer serverPlayer) {
            boolean rev = PatreonPlatform.INSTANCE.playerSettings(event.getOriginal()).isPresent();
            if (!rev)
                event.getOriginal().reviveCaps();
            PatreonPlatform.INSTANCE.playerSettings(serverPlayer).ifPresent(setting -> setting.read(PatreonPlatform.INSTANCE.playerSettings(event.getOriginal()).orElseThrow(() -> new NullPointerException("Capability of old player is null!")).save(new CompoundTag())));
            PatreonPlatform.INSTANCE.sendToClient(serverPlayer, serverPlayer);
            if (!rev)
                event.getOriginal().invalidateCaps();
        }
    }

    public static void tick(TickEvent.PlayerTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            PatreonPlatform.INSTANCE.playerSettings(event.player).ifPresent(s -> s.tick(event.player));
        }
    }

    public static void track(PlayerEvent.StartTracking event) {
        if (event.getTarget() instanceof ServerPlayer targetPlayer)
            PatreonPlatform.INSTANCE.sendToClient((ServerPlayer) event.getPlayer(), targetPlayer);
    }

    @Override
    public Optional<PatreonPlayerSetting> playerSettings(Player player) {
        return player.getCapability(PATREONPLAYER).resolve();
    }

    @Override
    public void sendToClient(ServerPlayer player, ServerPlayer target) {
        PatreonPlatform.INSTANCE.playerSettings(target).ifPresent(setting -> {
            if (PatreonDataManager.get(target.getUUID().toString()).tier() < 1)
                setting.setEffect(null);
            PacketHandler.sendToClientChecked(new S2CEffectUpdatePkt(target.getId(), setting.effect() != null ? setting.effect().id() : "", setting.shouldRender(), setting.getRenderLocation(), setting.getColor()), player);
        });
    }

    @Override
    public void sendToTracking(ServerPlayer player, S2CEffectUpdatePkt pkt) {
        PacketHandler.sendToTracking(pkt, player);
    }

    public static class PlayerCap extends PatreonPlayerSetting implements ICapabilitySerializable<CompoundTag> {

        private final LazyOptional<PatreonPlayerSetting> instance = LazyOptional.of(() -> this);

        public PlayerCap(Player player) {
            super(player);
        }

        @Override
        public <T> LazyOptional<T> getCapability(Capability<T> cap, Direction side) {
            return PatreonImpl.PATREONPLAYER.orEmpty(cap, this.instance);
        }

        @Override
        public CompoundTag serializeNBT() {
            return this.save(new CompoundTag());
        }

        @Override
        public void deserializeNBT(CompoundTag nbt) {
            this.read(nbt);
        }
    }
}
