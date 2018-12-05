package com.flemmli97.tenshilib.api.config;

import java.util.Map;
import java.util.Map.Entry;

import javax.annotation.Nullable;

import net.minecraftforge.common.config.Configuration;

public class MapSerializable<M, T extends IConfigSerializable<T>> implements IConfigSerializable<MapSerializable<M, T>>{

	private Map<M, T> map;
	private StringParser<M> parser;
	
	private static final StringParser<Object> objToString = new StringParser<Object>() {
		@Override
		public String getString(Object t) {
			return t.toString();
		}};
	
	@SuppressWarnings("unchecked")
	public MapSerializable (Map<M, T> map, @Nullable StringParser<M> parser)
	{
		this.map=map;
		this.parser=parser;
		if(parser==null)
			this.parser=(StringParser<M>) objToString;
		
	}
	
	@Override
	public MapSerializable<M, T> config(Configuration config, MapSerializable<M, T> old,
			String configCategory) {
		for(Entry<M, T> entry : map.entrySet())
		{			
			map.put(entry.getKey(), entry.getValue().config(config, entry.getValue(), configCategory+"."+this.parser.getString(entry.getKey())));
		}
		return this;
	}

}
