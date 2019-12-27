package com.flemmli97.tenshilib.common.config;

import java.io.File;

import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.common.Loader;

public class ConfigHandler {

    public static Configuration config;

    public static int seed = 454345783;
    public static int generatorRadius = 8;
    public static boolean showStructure;

    public static void load() {
        if(config == null){
            config = new Configuration(new File(Loader.instance().getConfigDir(), "tenshilib.cfg"));
            config.load();
        }
        seed = config.get("general", "Random seed", seed, "Random seed used by the world generator. Changing it also changes structure generation")
                .getInt();
        //Min 8
        generatorRadius = config.get("general", "Generator Check Radius", generatorRadius,
                "The chunk radius in which the world generator checks. Higher value makes bigger structures possible but also makes it more heavier")
                .getInt();
        showStructure = config.getBoolean("Show Bounding Box", "general", showStructure, "Show the bounding box of the last visited structure");
        config.save();
    }
}
