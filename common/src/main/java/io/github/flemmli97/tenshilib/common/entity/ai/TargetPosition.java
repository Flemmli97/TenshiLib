package io.github.flemmli97.tenshilib.common.entity.ai;

import io.github.flemmli97.tenshilib.common.entity.EntityUtils;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

public record TargetPosition(Vec3 position, double minHeight, double maxHeight) {

    public static TargetPosition of(LivingEntity target) {
        Vec3 pos = target.position();
        double yRed = Mth.clamp(target.getBbHeight() - target.getEyeHeight(), 0, 0.3);
        return new TargetPosition(pos, pos.y() + yRed, pos.y() + target.getBbHeight() - yRed);
    }

    public static TargetPosition of(Vec3 target) {
        return new TargetPosition(target, target.y(), target.y());
    }

    public Vec3 asVec(Vec3 from) {
        return EntityUtils.getStraightProjectileTarget(from, this.position, this.minHeight, this.maxHeight);
    }
}
