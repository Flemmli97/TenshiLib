package com.flemmli97.tenshilib.common.events.handler;

import com.flemmli97.tenshilib.TenshiLib;
import com.flemmli97.tenshilib.api.item.IDualWeapon;
import com.flemmli97.tenshilib.common.config.ConfigHandler;
import com.flemmli97.tenshilib.common.network.PacketHandler;
import com.flemmli97.tenshilib.common.network.PacketStructure;
import com.flemmli97.tenshilib.common.world.structure.StructureBase;
import com.flemmli97.tenshilib.common.world.structure.StructureGenerator;
import com.flemmli97.tenshilib.common.world.structure.StructureMap;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.EnumHand;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.world.WorldEvent.PotentialSpawns;
import net.minecraftforge.fml.client.event.ConfigChangedEvent.OnConfigChangedEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerLoggedOutEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.PlayerTickEvent;

public class CommonEvents {

    private boolean prev;

    @SubscribeEvent
    public void tick(PlayerTickEvent event) {
        if(!event.player.world.isRemote){
            boolean now = event.player.world.getGameRules().getBoolean("showBoundingBox");
            if(now)
                StructureMap.get(event.player.world).current(event.player, this.prev != now);
            else
                PacketHandler.sendTo(new PacketStructure(null), (EntityPlayerMP) event.player);
            this.prev = event.player.world.getGameRules().getBoolean("showBoundingBox");
        }
    }

    @SubscribeEvent
    public void config(OnConfigChangedEvent event) {
        if(event.getModID().equals(TenshiLib.MODID))
            ConfigHandler.load();
    }

    @SubscribeEvent
    public void loggOut(PlayerLoggedOutEvent event) {
        PacketHandler.sendTo(new PacketStructure(null), (EntityPlayerMP) event.player);
    }

    //Isnt this operation suupperr heavy? idk
    @SubscribeEvent
    public void mobSpawnStructure(PotentialSpawns event) {
        StructureBase base = StructureMap.get(event.getWorld()).current(event.getPos());
        if(base != null){
            if(StructureGenerator.doesStructurePreventSpawn(base.getStructureId()))
                event.getList().clear();
            event.getList().addAll(StructureGenerator.getSpawnList(base.getStructureId(), event.getType()));
        }
    }

    @SubscribeEvent
    public void stopOffhand(PlayerInteractEvent.RightClickItem event) {
        if(event.getHand() == EnumHand.OFF_HAND && event.getItemStack().getItem() instanceof IDualWeapon
                && ((IDualWeapon) event.getItemStack().getItem()).disableOffhand())
            event.setCanceled(true);
    }

    @SubscribeEvent
    public void stopOffhand(PlayerInteractEvent.RightClickBlock event) {
        if(event.getHand() == EnumHand.OFF_HAND && event.getItemStack().getItem() instanceof IDualWeapon
                && ((IDualWeapon) event.getItemStack().getItem()).disableOffhand())
            event.setCanceled(true);
    }
}
