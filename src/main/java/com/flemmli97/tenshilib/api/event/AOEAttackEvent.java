package com.flemmli97.tenshilib.api.event;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.Cancelable;

import java.util.List;

@Cancelable
public class AOEAttackEvent extends PlayerEvent {

    private List<LivingEntity> list;

    public AOEAttackEvent(PlayerEntity player, List<LivingEntity> attackList) {
        super(player);
        this.list = attackList;
    }

    public List<LivingEntity> attackList() {
        return this.list;
    }
}