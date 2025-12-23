package io.github.flemmli97.tenshilib.common.entity.ai;

import io.github.flemmli97.tenshilib.common.entity.EntityUtils;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

public record TargetPosition(Vec3 position, double minHeight, double maxHeight) {

    @Deprecated
    public static TargetPosition of(LivingEntity target) {
        return reducedRangeOf(target);
    }

    public static TargetPosition reducedRangeOf(LivingEntity target) {
        double yRed = Mth.clamp(target.getBbHeight() - target.getEyeHeight(), 0, 0.3);
        return of(target, yRed);
    }

    public static TargetPosition fullRangeOf(LivingEntity target) {
        return of(target, 0);
    }

    public static TargetPosition of(LivingEntity target, double heightMod) {
        Vec3 pos = target.position();
        return new TargetPosition(pos, pos.y() + heightMod, pos.y() + target.getBbHeight() - heightMod);
    }

    public static TargetPosition of(Vec3 target) {
        return new TargetPosition(target, target.y(), target.y());
    }

    public Vec3 asVec(Vec3 from) {
        return EntityUtils.getStraightProjectileTarget(from, this.position, this.minHeight, this.maxHeight);
    }
}
