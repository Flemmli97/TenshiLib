package io.github.flemmli97.tenshilib.patreon.effects;

import io.github.flemmli97.tenshilib.common.utils.RayTraceUtils;
import io.github.flemmli97.tenshilib.patreon.RenderLocation;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;

public class ParticleEffect extends PatreonEffectConfig {

    private static final Set<GuiElement> NONE = Set.of();
    private static final Vector3f YP = new Vector3f(0, 1, 0);

    private final Particle[] particles;

    public ParticleEffect(String id, int tier, Particle... particles) {
        super(id, tier);
        this.particles = particles;
    }

    @Override
    public void tick(Player player) {
        GameRenderer r;

        if (!(player instanceof ServerPlayer serverPlayer))
            return;
        for (int i = 0; i < this.particles.length; i++) {
            Particle particle = this.particles[i];
            if (particle.chance < 1 && player.getRandom().nextFloat() < particle.chance)
                continue;
            Vector3f off = RayTraceUtils.rotatedAround(particle.position, YP, -player.getYHeadRot());
            Vec3 pos = player.position().add(off.x(), off.y(), off.z());
            if (particle.container.positionMod.isPresent())
                pos = pos.add(particle.container.positionMod.get().apply(player, i));
            Vec3 delta = Vec3.ZERO;
            if (particle.container.delta.isPresent())
                delta = particle.container.delta.get().apply(player, i);
            serverPlayer.serverLevel().sendParticles(particle.container.particle, pos.x(), pos.y(), pos.z(), 0, delta.x, delta.y, delta.z, 1);
        }
    }

    @Override
    public boolean locationAllowed(RenderLocation loc) {
        return false;
    }

    @Override
    public RenderLocation defaultLoc() {
        return RenderLocation.HAT;
    }

    @Override
    public Set<GuiElement> guiElements() {
        return NONE;
    }

    public record Particle(Vec3 position, float chance, ParticleContainer container) {
    }

    public static class Builder {

        private final List<Particle> particles = new ArrayList<>();

        public static Builder of() {
            return new Builder();
        }

        public Builder add(Pattern pattern) {
            this.particles.addAll(pattern.compile());
            return this;
        }

        public ParticleEffect build(String id, int tier) {
            return new ParticleEffect(id, tier, this.particles.toArray(new Particle[0]));
        }
    }

    public static class Pattern {

        public static Function<Player, Vec3> randomAll(double scale) {
            return player -> new Vec3(player.getRandom().nextGaussian() * scale,
                    player.getRandom().nextGaussian() * scale,
                    player.getRandom().nextGaussian() * scale);
        }

        private final Direction.Axis axis;
        private final Vec3 initialOffset;
        private double spacing = 0.5;
        private final Map<Character, ParticleContainer> particles = new HashMap<>();
        private final List<String> patterns = new ArrayList<>();

        public Pattern(Direction.Axis axis, Vec3 initialOffset) {
            this.axis = axis;
            this.initialOffset = initialOffset;
        }

        public Pattern setSpacing(double spacing) {
            this.spacing = spacing;
            return this;
        }

        public Pattern addParticle(char key, ParticleOptions particle) {
            return this.addParticle(key, particle, null, null);
        }

        public Pattern addParticleDelta(char key, ParticleOptions particle, ParticleVector delta) {
            return this.addParticle(key, particle, null, delta);
        }

        public Pattern addParticlePos(char key, ParticleOptions particle, ParticleVector position) {
            return this.addParticle(key, particle, position, null);
        }

        public Pattern addParticle(char key, ParticleOptions particle, ParticleVector position, ParticleVector delta) {
            this.particles.put(key, new ParticleContainer(particle, Optional.ofNullable(position), Optional.ofNullable(delta)));
            return this;
        }

        public Pattern addPattern(String... pattern) {
            this.patterns.addAll(List.of(pattern));
            return this;
        }

        public List<Particle> compile() {
            List<Particle> result = new ArrayList<>();
            double planeX = 0;
            double planeY = 0;
            for (String pattern : this.patterns) {
                for (char c : pattern.toCharArray()) {
                    ParticleContainer particle = this.particles.get(c);
                    if (particle != null) {
                        Vec3 pos = switch (this.axis) {
                            case X -> this.initialOffset.add(planeX, planeY, 0);
                            case Y -> this.initialOffset.add(planeX, 0, planeY);
                            case Z -> this.initialOffset.add(0, planeY, planeX);
                        };
                        result.add(new Particle(pos, 1, particle));
                    }
                    planeX += this.spacing;
                }
                planeY += this.spacing;
                planeX = 0;
            }
            return result;
        }
    }

    private record ParticleContainer(ParticleOptions particle, Optional<ParticleVector> positionMod,
                                     Optional<ParticleVector> delta) {
    }

    public interface ParticleVector {

        Vec3 apply(Player player, int num);
    }
}
