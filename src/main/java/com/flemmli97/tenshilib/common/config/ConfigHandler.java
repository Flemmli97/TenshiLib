package com.flemmli97.tenshilib.common.config;

import com.flemmli97.tenshilib.TenshiLib;

import net.minecraftforge.common.config.Config;

@Config(modid = TenshiLib.MODID)
public class ConfigHandler {

	@Config.Comment(value="Random seed used by the world generator. Changing it also changes structure generation")
	@Config.Name(value="Random seed")
	public static int seed = 454345783;
	@Config.Comment(value="The chunk radius in which the world generator checks. Higher value makes bigger structures possible but also makes it more heavier")
	@Config.Name(value="Generator Check Radius")
	@Config.RangeInt(min=8)
	public static int generatorRadius = 8;
	@Config.Comment(value="Show the bounding box of the last visited structure")
	@Config.Name(value="Show Bounding Box")
	public static boolean showStructure;
}
