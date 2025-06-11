package io.github.flemmli97.tenshilib.client.model.animation.keyframe;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import io.github.flemmli97.tenshilib.TenshiLib;
import net.minecraft.ResourceLocationException;
import net.minecraft.commands.arguments.ParticleArgument;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.particles.ParticleOptions;
import org.jetbrains.annotations.Nullable;

public class ParticleKeyFrame extends KeyFrameValue {

    private final String particle;
    public final String locator;
    private ParticleOptions particleOptions;

    public ParticleKeyFrame(double startTick, String particle, String locator) {
        super(startTick);
        this.particle = particle;
        this.locator = locator;
    }

    @Nullable
    public ParticleOptions getParticle(RegistryAccess access) {
        if (this.particleOptions == null && !this.particle.isEmpty()) {
            try {
                StringReader reader = new StringReader(this.particle);
                this.particleOptions = ParticleArgument.readParticle(reader, access);
            } catch (ResourceLocationException | CommandSyntaxException e) {
                TenshiLib.LOGGER.error("Could not parse particle {}", this.particle, e);
            }
        }
        return this.particleOptions;
    }

    @Override
    public String toString() {
        return String.format("Particle Frame [@:%s, {%s, %s}]", this.startTick, this.locator, this.particle);
    }
}
