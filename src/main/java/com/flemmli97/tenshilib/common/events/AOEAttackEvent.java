package com.flemmli97.tenshilib.common.events;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.fml.common.eventhandler.Cancelable;

import java.util.List;

@Cancelable
public class AOEAttackEvent extends PlayerEvent {

    private List<EntityLivingBase> list;

    public AOEAttackEvent(EntityPlayer player, List<EntityLivingBase> attackList) {
        super(player);
        this.list = attackList;
    }

    public List<EntityLivingBase> attackList() {
        return this.list;
    }
}
