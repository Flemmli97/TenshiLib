package io.github.flemmli97.tenshilib.mixinhelper;

import net.minecraft.world.InteractionHand;

public interface LastSwungHand {

    InteractionHand tenshilib$lastSwungHand();

    void tenshilib$SetLastSwungHand(InteractionHand hand);
}
