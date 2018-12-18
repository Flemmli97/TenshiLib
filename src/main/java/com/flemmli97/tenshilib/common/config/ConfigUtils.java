package com.flemmli97.tenshilib.common.config;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.commons.lang3.tuple.Triple;

import com.flemmli97.tenshilib.TenshiLib;
import com.flemmli97.tenshilib.api.config.ConfigAnnotations;
import com.flemmli97.tenshilib.api.config.IConfigArrayValue;
import com.flemmli97.tenshilib.api.config.IConfigSerializable;
import com.flemmli97.tenshilib.api.config.IConfigValue;
import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import net.minecraftforge.common.config.Config;
import net.minecraftforge.common.config.ConfigCategory;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;

public class ConfigUtils {

	private static Map<Class<?>, List<Triple<Field, String,  Object>>> preInit = Maps.newHashMap();
	private static Map<Class<?>, List<Triple<Field, String,  Object>>> init = Maps.newHashMap();
	private static Map<Class<?>, List<Triple<Field, String,  Object>>> postInit = Maps.newHashMap();
	private static Map<Class<?>, Configuration> classConfigMap = Maps.newHashMap();
	
	private static final Map<Class<?>, List<Field>> classFieldMap = Maps.newHashMap();
	
	public static void init(Init type)
	{
		switch(type)
		{
			case INIT: processMap(init);
						init=null;
				break;
			case POST: processMap(postInit);
						postInit=null;
						classConfigMap=null;
				break;
			case PRE: processMap(preInit);
						preInit=null;
				break;
			default:
				break;
		}
	}
	
	private static boolean setIConfigValue(Field f, Configuration config, String category, Class<?> clss, Object obj)
	{
		boolean success=false;
		String prev = null;
		try {
        	IConfigValue val = (IConfigValue) f.get(obj);
        	String name = f.getName();
        	if(f.isAnnotationPresent(Config.Name.class))
        		name=f.getAnnotation(Config.Name.class).value();
        	String comment=null;
        	if(f.isAnnotationPresent(Config.Comment.class))
        		comment=Joiner.on('\n').join(f.getAnnotation(Config.Comment.class).value());
        	comment=comment==null?('\n'+val.usage()):(comment+'\n'+val.usage());
            String lang = null;
        	if(f.isAnnotationPresent(Config.LangKey.class))
        		lang=f.getAnnotation(Config.LangKey.class).value();
        	boolean restart=f.isAnnotationPresent(Config.RequiresMcRestart.class);
        	boolean worldRestart=f.isAnnotationPresent(Config.RequiresWorldRestart.class);
        	prev = val.writeToString();
        	Property prop = config.get(category, name, val.writeToString());       	
        	if(!comment.replaceAll("\n", "").isEmpty())
        		prop.setComment(comment);
        	prop.setRequiresMcRestart(restart);
        	prop.setRequiresWorldRestart(worldRestart);
        	if(lang!=null)
        		prop.setLanguageKey(lang);
			f.set(obj, val.readFromString(prop.getString()));
			success=true;
		} catch (Exception e) {
			if(prev!=null)
			config.get(category, f.getName(), prev); 
			TenshiLib.logger.error("Error reading IConfigValue {} from config", f.getName());
			e.printStackTrace();
		}
		return success;
	}
	
	private static boolean setIConfigArrayValue(Field f, Configuration config, String category, Class<?> clss, Object obj)
	{
		boolean success=false;
		String[] prev = null;
		try {
			IConfigArrayValue val = (IConfigArrayValue) f.get(obj);
        	String name = f.getName();
        	if(f.isAnnotationPresent(Config.Name.class))
        		name=f.getAnnotation(Config.Name.class).value();
        	String comment=null;
        	if(f.isAnnotationPresent(Config.Comment.class))
        		comment=Joiner.on('\n').join(f.getAnnotation(Config.Comment.class).value());
        	comment=comment==null?('\n'+val.usage()):(comment+'\n'+val.usage());
            String lang = null;
        	if(f.isAnnotationPresent(Config.LangKey.class))
        		lang=f.getAnnotation(Config.LangKey.class).value();
        	boolean restart=f.isAnnotationPresent(Config.RequiresMcRestart.class);
        	boolean worldRestart=f.isAnnotationPresent(Config.RequiresWorldRestart.class);
        	prev = val.writeToString();
        	Property prop = config.get(category, name, val.writeToString());       	
        	if(!comment.replaceAll("\n", "").isEmpty())
        		prop.setComment(comment);
        	prop.setRequiresMcRestart(restart);
        	prop.setRequiresWorldRestart(worldRestart);
        	if(lang!=null)
        		prop.setLanguageKey(lang);
			f.set(obj, val.readFromString(prop.getStringList()));
			success=true;
		} catch (Exception e) {
			if(prev!=null)
			config.get(category, f.getName(), prev); 
			TenshiLib.logger.error("Error reading IConfigValue {} from config", f.getName());
			e.printStackTrace();
		}
		return success;
	}
	
	@SuppressWarnings("unchecked")
	private static void setSerializableConfig(Field f, Configuration config, String category, Class<?> clss, Object obj)
	{
		String field = f.isAnnotationPresent(Config.Name.class)?f.getAnnotation(Config.Name.class).value().toLowerCase(Locale.ENGLISH):f.getName().toLowerCase(Locale.ENGLISH);
		String confCategory = category.isEmpty()?field:(category+(field.isEmpty()?"":(Configuration.CATEGORY_SPLITTER+field)));
		ConfigCategory cat = config.getCategory(confCategory);
		if(f.isAnnotationPresent(Config.Comment.class))
			cat.setComment(Joiner.on('\n').join(f.getAnnotation(Config.Comment.class).value()));
		if(f.isAnnotationPresent(Config.LangKey.class))
			cat.setLanguageKey(f.getAnnotation(Config.LangKey.class).value());
        try {
			@SuppressWarnings("rawtypes")
			IConfigSerializable val = (IConfigSerializable) f.get(obj);
			f.set(obj, val.config(config, val, confCategory));
		} catch (Exception e) {
			TenshiLib.logger.error("Error parsing IConfigSerializable {} from config", f.getName());
			e.printStackTrace();
		}
	}
	
	public static void load(Configuration config, String category, Class<?> clss, Object obj)
	{
		//Config init
		if(!classFieldMap.containsKey(clss))
		{
			for (Field f : clss.getDeclaredFields())
	        {
				if (Modifier.isStatic(f.getModifiers()) != (obj == null))
	                continue;
				if (Modifier.isPublic(f.getModifiers()) && f.isAnnotationPresent(ConfigAnnotations.ConfigValue.class))
				{
					addGlobal(clss, f, config, obj);			        
			        if(f.getAnnotation(ConfigAnnotations.ConfigValue.class).getInitTime()==Init.RUNTIME)
			        {
			        	if(IConfigValue.class.isAssignableFrom(f.getType()))
			        		setIConfigValue(f, config, category, clss, obj);
			        	else if(IConfigArrayValue.class.isAssignableFrom(f.getType()))
			        		setIConfigArrayValue(f, config, category, clss, obj);
			        	else if(IConfigSerializable.class.isAssignableFrom(f.getType()))
		        			setSerializableConfig(f, config, category, clss, obj);			    
			        }
			        else
			        {
			        	Init type = f.getAnnotation(ConfigAnnotations.ConfigValue.class).getInitTime();
			        	switch(type)
			        	{
							case INIT: add(init, clss, f, config, obj, category);	
								break;
							case POST: add(postInit, clss, f, config, obj, category);	
								break;
							case PRE: add(preInit, clss, f, config, obj, category);	
								break;
							default:
								break;		        	
			        	}    	
			        }
				}	
	        }
		}
		//Config process
		else
		{
			if(classFieldMap.containsKey(clss))
				for(Field f : classFieldMap.get(clss))
		        {
		        	if(IConfigValue.class.isAssignableFrom(f.getType()))
		        		setIConfigValue(f, config, category, clss, obj);
		        	else if(IConfigArrayValue.class.isAssignableFrom(f.getType()))
		        		setIConfigArrayValue(f, config, category, clss, obj);
		        	else if(IConfigSerializable.class.isAssignableFrom(f.getType()))
		        		setSerializableConfig(f, config, category, clss, obj);
		        }
		}
	}
	
	private static void addGlobal(Class<?> clss, Field f, Configuration config, Object obj)
	{
		List<Field> list = classFieldMap.get(clss);
		if(list==null)
			list=Lists.newArrayList();
		list.add(f);
		classFieldMap.put(clss, list);
	}
	
	//The maps for initialization need Pair<Field,Object>.
	private static void add(Map<Class<?>, List<Triple<Field, String, Object>>> map, Class<?> clss, Field f, Configuration config, Object obj, String category)
	{
		List<Triple<Field, String, Object>> list = map.get(clss);
		if(list==null)
			list=Lists.newArrayList();
		list.add(Triple.of(f, category, obj));
		map.put(clss, list);
		classConfigMap.put(clss, config);
	}
		
	//Called during preInit, init and postInit
	private static void processMap(Map<Class<?>, List<Triple<Field, String, Object>>> map)
	{
		for(Class<?> clss : map.keySet())
		{
			Configuration config = classConfigMap.get(clss);
			for(Triple<Field, String, Object> t : map.get(clss))
	        {
	        	if(IConfigValue.class.isAssignableFrom(t.getLeft().getType()))
	        		setIConfigValue(t.getLeft(), config, t.getMiddle(), clss, t.getRight());
	        	else if(IConfigArrayValue.class.isAssignableFrom(t.getLeft().getType()))
	        		setIConfigArrayValue(t.getLeft(), config, t.getMiddle(), clss, t.getRight());
	        	else if(IConfigSerializable.class.isAssignableFrom(t.getLeft().getType()))
	        		setSerializableConfig(t.getLeft(), config, t.getMiddle(), clss, t.getRight());
	        }
			config.save();
		}
	}
	
	

	public static enum Init
	{
		RUNTIME,
		PRE,
		INIT,
		POST;
	}
}
