package com.flemmli97.tenshilib.common.config;

import java.util.List;
import java.util.regex.Pattern;

import javax.annotation.Nullable;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;

import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.common.config.ConfigCategory;
import net.minecraftforge.common.config.ConfigElement;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;
import net.minecraftforge.fml.client.config.DummyConfigElement.DummyCategoryElement;
import net.minecraftforge.fml.client.config.IConfigElement;
import net.minecraftforge.fml.common.FMLLog;

public class ConfigUtils {

	public static List<IConfigElement> list(Configuration... configs)
	{
		List<IConfigElement> list = Lists.newArrayList();
		for(Configuration config : configs)
		{
			//String conf = configs.length==1?"":config.getConfigFile().getName();
			for(String cat : config.getCategoryNames())
			{
                ConfigCategory category = config.getCategory(cat);
                if(cat.isEmpty())
                {
                	list.addAll(new ConfigElement(category).getChildElements());
                }
                else
                {
	                if (category.isChild())
	                    continue;
	                DummyCategoryElement element = new DummyCategoryElement(category.getName(), category.getLanguagekey(), new ConfigElement(category).getChildElements());
	                element.setRequiresMcRestart(category.requiresMcRestart());
	                element.setRequiresWorldRestart(category.requiresWorldRestart());
					list.add(element);
                }
			}
		}
		return list;
	}
	
	public static IConfigElement element(ConfigCategory category, boolean empty)
	{
		if(empty)
			return new ConfigElement(category);
		DummyCategoryElement element = new DummyCategoryElement(category.getName(), category.getLanguagekey(), new ConfigElement(category).getChildElements());
        element.setRequiresMcRestart(category.requiresMcRestart());
        element.setRequiresWorldRestart(category.requiresWorldRestart());
		return element;
	}
	
	private static final Joiner PIPE = Joiner.on('|');
    private static final Joiner NEW_LINE = Joiner.on('\n');

	@SuppressWarnings("unchecked")
	public static <T extends Enum<?>> T getEnumVal(Configuration config, String category, String name, String comment, T defaultVal, @Nullable List<String> pattern)
	{
		Property prop = config.get(category, name, defaultVal.name());
		List<String> lst = pattern;
		if(pattern==null)
		{
			lst = Lists.newArrayList();
	        for(TextFormatting form : TextFormatting.values())
	        	if(form.isColor())
	        		lst.add(form.toString());
		}
		prop.setValidationPattern(Pattern.compile(PIPE.join(lst)));
		prop.setValidValues(lst.toArray(new String[0]));
		String validValues = NEW_LINE.join(lst);
        if (!comment.isEmpty())
            prop.setComment(NEW_LINE.join(new String[] {comment, "Valid values:" }) + "\n" + validValues);
        else
            prop.setComment("Valid values:" + "\n" + validValues);
        try
        {
        	return (T) Enum.valueOf(defaultVal.getClass(), prop.getString());
        }
        catch(Exception e)
        {
        	return defaultVal;
        }
	}
	
	public static <T extends Enum<?>> T getEnumVal(Configuration config, String category, String name, String comment, T defaultVal)
	{
		return getEnumVal(config, category, name, comment, defaultVal, null);
	}
	
	public static int getIntConfig(Configuration config, String name, String category, int defaultValue, int minValue, String comment)
	{
		Property prop = config.get(category, name, Integer.toString(defaultValue), name);
        prop.setLanguageKey(name);
        prop.setComment(comment + "[default: " + defaultValue + "]");
        prop.setMinValue(minValue);
        try
        {
            return Math.max(minValue, prop.getInt());
        }
        catch (Exception e)
        {
            FMLLog.log.error("Failed to get int for {}/{}", name, category, e);
        }
        return defaultValue;
	}
	
	public static float getFloatConfig(Configuration config, String name, String category, float defaultValue, String comment)
	{
		Property prop = config.get(category, name, Float.toString(defaultValue), name);
        prop.setLanguageKey(name);
        prop.setComment(comment + "[default: " + defaultValue + "]");
        try
        {
            float parseFloat = Float.parseFloat(prop.getString());
            return Math.max(0, parseFloat);
        }
        catch (Exception e)
        {
            FMLLog.log.error("Failed to get float for {}/{}", name, category, e);
        }
        return defaultValue;
	}
	
	public enum LoadState
	{
		PREINIT,
		INIT,
		POSTINIT,
		SYNC;
	}
}
