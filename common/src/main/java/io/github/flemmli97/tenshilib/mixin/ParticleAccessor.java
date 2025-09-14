package io.github.flemmli97.tenshilib.mixin;

import net.minecraft.client.particle.Particle;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(Particle.class)
public interface ParticleAccessor {

    @Accessor("hasPhysics")
    void setHasPhysics(boolean hasPhysics);

    @Accessor("alpha")
    void setAlpha(float alpha);

    @Accessor("gravity")
    void setGravity(float gravity);

    @Accessor("x")
    double getX();

    @Accessor("y")
    double getY();

    @Accessor("z")
    double getZ();

    @Accessor("xo")
    void setXo(double xo);

    @Accessor("yo")
    void setYo(double yo);

    @Accessor("zo")
    void setZo(double zo);
}
