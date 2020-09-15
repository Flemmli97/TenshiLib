package com.flemmli97.tenshilib.client.events;

import net.minecraft.client.renderer.entity.model.PlayerModel;
import net.minecraft.entity.LivingEntity;
import net.minecraftforge.eventbus.api.Event;

public class PlayerRotationEvent<T extends LivingEntity> extends Event {

    private final float limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch;
    private final T entity;
    private final PlayerModel<T> model;

    public PlayerRotationEvent(T entity, PlayerModel<T> model, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        this.limbSwing = limbSwing;
        this.limbSwingAmount = limbSwingAmount;
        this.ageInTicks = ageInTicks;
        this.netHeadYaw = netHeadYaw;
        this.headPitch = headPitch;
        this.entity = entity;
        this.model = model;
    }

    public float getLimbSwing() {
        return this.limbSwing;
    }

    public float getLimbSwingAmount() {
        return this.limbSwingAmount;
    }

    public float getAgeInTicks() {
        return this.ageInTicks;
    }

    public float getNetHeadYaw() {
        return this.netHeadYaw;
    }

    public float getHeadPitch() {
        return this.headPitch;
    }

    public T getEntity() {
        return this.entity;
    }

    public PlayerModel<T> getModel(){
        return this.model;
    }
}