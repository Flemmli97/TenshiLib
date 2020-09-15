package com.flemmli97.tenshilib.common.entity;

import net.minecraft.util.Hand;

public interface ILastHand {

    Hand lastSwungHand();

    void updateLastHand();
}
