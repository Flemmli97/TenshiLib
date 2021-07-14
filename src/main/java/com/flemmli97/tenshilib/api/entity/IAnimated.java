package com.flemmli97.tenshilib.api.entity;

import net.minecraft.entity.Entity;


public interface IAnimated<T extends Entity & IAnimated<T>> {

    AnimationHandler<T> getAnimationHandler();

}
