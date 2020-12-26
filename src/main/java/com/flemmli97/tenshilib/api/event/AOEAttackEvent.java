package com.flemmli97.tenshilib.api.event;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.Cancelable;

import java.util.List;

@Cancelable
public class AOEAttackEvent extends PlayerEvent {

    private List<Entity> list;

    public AOEAttackEvent(PlayerEntity player, List<Entity> attackList) {
        super(player);
        this.list = attackList;
    }

    public List<Entity> attackList() {
        return this.list;
    }
}