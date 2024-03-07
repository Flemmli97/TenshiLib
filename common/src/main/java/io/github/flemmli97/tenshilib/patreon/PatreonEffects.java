package io.github.flemmli97.tenshilib.patreon;

import com.google.common.collect.ImmutableList;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PatreonEffects {

    private static final Map<String, PatreonEffectConfig> CONFIGS = new HashMap<>();

    public static final PatreonEffectConfig MEGU_HAT = register(new PatreonEffectConfig("megu_hat") {
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

    public static final PatreonEffectConfig CAT = register(new PatreonEffectConfig("cat") {
        @Override
        public boolean locationAllowed(RenderLocation loc) {
            return loc != RenderLocation.BACK;
        }

        @Override
        public RenderLocation defaultLoc() {
            return RenderLocation.CIRCLING;
        }
    });

    public static final PatreonEffectConfig HALO = register(new PatreonEffectConfig("halo") {
        @Override
        public boolean locationAllowed(RenderLocation loc) {
            return RenderLocation.isHead(loc);
        }

        @Override
        public RenderLocation defaultLoc() {
            return RenderLocation.HAT;
        }
    });

    private static <T extends PatreonEffectConfig> T register(T conf) {
        CONFIGS.put(conf.id(), conf);
        return conf;
    }

    public static PatreonEffectConfig get(String id) {
        return CONFIGS.get(id);
    }

    public static List<PatreonEffectConfig> allEffects() {
        return ImmutableList.copyOf(CONFIGS.values());
    }

    public static abstract class PatreonEffectConfig {

        private final String id;
        public final int tier;

        public PatreonEffectConfig(String id) {
            this(id, 1);
        }

        public PatreonEffectConfig(String id, int tier) {
            this.id = id;
            this.tier = tier;
        }

        public abstract boolean locationAllowed(RenderLocation loc);

        public abstract RenderLocation defaultLoc();

        public int defaultColor() {
            return 0xFFFFFFFF;
        }

        public String id() {
            return this.id;
        }
    }
}
