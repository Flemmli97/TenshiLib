package com.flemmli97.tenshilib.common.javahelper;

import java.lang.reflect.Array;
import java.util.List;

import javax.annotation.Nullable;

import com.google.common.collect.Lists;

public class ArrayUtils {

	@SuppressWarnings("unchecked")
	public static <T> String arrayToString(T[] t, @Nullable StringParser<T> parser)
	{
		if(t.length==0)
			return "";
		if(parser==null)
			parser = (StringParser<T>) StringParser.objToString;
		String s = ""+parser.getString(t[0]);
		if(t.length==1)
			return s;
		for(int i = 1; i < t.length; i++)
			s+=","+parser.getString(t[i]);
		return s;
	}
	
	public static <T> String arrayToString(int[] t)
	{
		if(t.length==0)
			return "";
		String s = ""+t[0];
		if(t.length==1)
			return s;
		for(int i = 1; i < t.length; i++)
			s+=","+t[i];
		return s;
	}
	
	public static <T> String arrayToString(float[] t)
	{
		if(t.length==0)
			return "";
		String s = ""+t[0];
		if(t.length==1)
			return s;
		for(int i = 1; i < t.length; i++)
			s+=","+t[i];
		return s;
	}
	
	public static <T> String arrayToString(double[] t)
	{
		if(t.length==0)
			return "";
		String s = ""+t[0];
		if(t.length==1)
			return s;
		for(int i = 1; i < t.length; i++)
			s+=","+t[i];
		return s;
	}
	
	@SuppressWarnings("unchecked")
	public static <T,M> M[] arrayConverter(T[] ts, ObjectConverter<T,M> parser, Class<M> clss)
	{
		List<M> list = Lists.newArrayList();
		for(T t : ts)
			list.add(parser.convertFrom(t));
		return (M[]) Array.newInstance(clss, list.size());
	}
}
