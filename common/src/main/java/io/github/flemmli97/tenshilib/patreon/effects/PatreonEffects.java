package io.github.flemmli97.tenshilib.patreon.effects;

import com.google.common.collect.ImmutableList;
import com.mojang.math.Vector3f;
import io.github.flemmli97.tenshilib.patreon.RenderLocation;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PatreonEffects {

    private static final Map<String, PatreonEffectConfig> CONFIGS = new HashMap<>();
    private static final List<PatreonEffectConfig> EFFECTS = new ArrayList<>();

    public static final PatreonEffectConfig MEGU_HAT = register(new PatreonEffectConfig("megu_hat", 2) {
        @Override
        public boolean locationAllowed(RenderLocation loc) {
            return RenderLocation.isHead(loc) || RenderLocation.isCircling(loc);
        }

        @Override
        public RenderLocation defaultLoc() {
            return RenderLocation.HAT;
        }
    });

    public static final PatreonEffectConfig CHOMUSUKE = register(new PatreonEffectConfig("chomusuke", 2) {
        @Override
        public boolean locationAllowed(RenderLocation loc) {
            return loc != RenderLocation.BACK;
        }

        @Override
        public RenderLocation defaultLoc() {
            return RenderLocation.CIRCLING;
        }
    });

    public static final PatreonEffectConfig CAT = register(new PatreonEffectConfig("cat", 3) {
        @Override
        public boolean locationAllowed(RenderLocation loc) {
            return loc != RenderLocation.BACK;
        }

        @Override
        public RenderLocation defaultLoc() {
            return RenderLocation.CIRCLING;
        }
    });

    public static final PatreonEffectConfig HALO = register(new PatreonEffectConfig("halo", 2) {
        @Override
        public boolean locationAllowed(RenderLocation loc) {
            return RenderLocation.isHead(loc);
        }

        @Override
        public RenderLocation defaultLoc() {
            return RenderLocation.HAT;
        }
    });

    public static final PatreonEffectConfig HALO_PARTICLE = register(ParticleEffect.Builder.of()
            .add(new ParticleEffect.Pattern(Direction.Axis.Y, new Vec3(-0.3, 0, -0.3))
                    .setSpacing(0.12)
                    .addParticlePos('p', new DustParticleOptions(new Vector3f(Vec3.fromRGB24(0xebed68)), 0.7f), (p, i) ->
                            Vec3.ZERO.add(0, p.getBbHeight() + 0.2, 0))
                    .addPattern(" pppp ",
                            "pp  pp",
                            "p    p",
                            "p    p",
                            "pp  pp",
                            " pppp "))
            .build("halo_particle", 1));

    public static final PatreonEffectConfig CLOUD_PARTICLE = register(ParticleEffect.Builder.of()
            .add(new ParticleEffect.Pattern(Direction.Axis.Y, new Vec3(-0.705, 0.1, -0.705))
                    .setSpacing(0.47)
                    .addParticle('p', ParticleTypes.CLOUD)
                    .addPattern(" pp ",
                            "pppp",
                            "pppp",
                            " pp "))
            .build("cloud_particle", 1));

    public static final PatreonEffectConfig ENCHANTED_PARTICLE = register(ParticleEffect.Builder.of()
            .add(new ParticleEffect.Pattern(Direction.Axis.X, new Vec3(0, 0, 0))
                    .setSpacing(0.1)
                    .addParticle('p', ParticleTypes.ENCHANT, (p, i) ->
                            ParticleEffect.Pattern.randomAll(0.7).apply(p).add(0, p.getBbHeight() * 0.5 - 0.1, 0), (p, i) ->
                            ParticleEffect.Pattern.randomAll(0.3).apply(p))
                    .addPattern("p",
                            "p",
                            "p"))
            .add(new ParticleEffect.Pattern(Direction.Axis.X, new Vec3(0, 0, 0))
                    .setSpacing(0.1)
                    .addParticle('p', ParticleTypes.ENCHANT, (p, i) ->
                            ParticleEffect.Pattern.randomAll(0.7).apply(p).add(0, p.getBbHeight() * 0.5 - 0.1, 0), (p, i) ->
                            ParticleEffect.Pattern.randomAll(0.3).apply(p))
                    .addPattern("p",
                            "p",
                            "p"))
            .build("enchanted", 1));

    public static final PatreonEffectConfig FLAMING_SHIELD = register(ParticleEffect.Builder.of()
            .add(new ParticleEffect.Pattern(Direction.Axis.X, new Vec3(0, 0, 0))
                    .setSpacing(0)
                    .addParticlePos('p', ParticleTypes.FLAME, (p, i) -> {
                        float tick = p.tickCount;
                        double height = p.getBbHeight() * 0.5;
                        float offset = i * Mth.PI / 2;
                        return new Vec3(Mth.cos(Mth.DEG_TO_RAD * tick * 5 + offset),
                                Mth.sin(Mth.DEG_TO_RAD * tick * 2f * (i > 3 ? -1 : 1)) * height + height,
                                Mth.sin(Mth.DEG_TO_RAD * tick * 5 + offset));
                    })
                    .addPattern("pppppppp"))
            .build("flaming_shield", 1));

    private static <T extends PatreonEffectConfig> T register(T conf) {
        CONFIGS.put(conf.id(), conf);
        EFFECTS.add(conf);
        return conf;
    }

    public static PatreonEffectConfig get(String id) {
        return CONFIGS.get(id);
    }

    public static List<PatreonEffectConfig> allEffects() {
        return ImmutableList.copyOf(EFFECTS);
    }
}
