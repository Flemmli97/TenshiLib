package com.flemmli97.tenshilib.asm;

import java.util.Map;

import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin;
import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin.MCVersion;
import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin.Name;

@MCVersion(value="1.12.2")
@Name(value="TenshiCore")
public class ASMLoader implements IFMLLoadingPlugin{

	private static boolean deObfEnviroment = false;
	public static boolean asmLoaded;
	@Override
	public String[] getASMTransformerClass() {
		return new String[]{ASMTransformer.class.getName()};	}

	@Override
	public String getModContainerClass() {
		return null;
	}

	@Override
	public String getSetupClass() {
		return null;
	}

	@Override
	public void injectData(Map<String, Object> data) {
		deObfEnviroment = !(Boolean)data.get("runtimeDeobfuscationEnabled");
	}

	@Override
	public String getAccessTransformerClass() {
		return null;
	}

	public static boolean isDeobfEnvironment()
	{
		return deObfEnviroment;
	}
}
