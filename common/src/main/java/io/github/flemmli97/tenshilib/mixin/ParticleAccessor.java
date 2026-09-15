package io.github.flemmli97.tenshilib.mixin;

import net.minecraft.client.particle.Particle;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(Particle.class)
public interface ParticleAccessor {

    @Accessor("hasPhysics")
    void tenshilib$set_hasPhysics(boolean hasPhysics);

    @Accessor("alpha")
    void tenshilib$set_alpha(float alpha);

    @Accessor("gravity")
    void tenshilib$set_gravity(float gravity);

    @Accessor("xo")
    void tenshilib$set_xo(double xo);

    @Accessor("yo")
    void tenshilib$set_yo(double yo);

    @Accessor("zo")
    void tenshilib$set_zo(double zo);

    @Accessor("x")
    double tenshilib$x();

    @Accessor("y")
    double tenshilib$y();

    @Accessor("z")
    double tenshilib$z();

    @Accessor("xd")
    double tenshilib$xd();

    @Accessor("yd")
    double tenshilib$yd();

    @Accessor("zd")
    double tenshilib$zd();
}
