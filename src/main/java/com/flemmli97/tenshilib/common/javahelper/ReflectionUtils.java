package com.flemmli97.tenshilib.common.javahelper;

import java.lang.reflect.Field;

public class ReflectionUtils {
	
	@SuppressWarnings("unchecked")
	public static <T> T getFieldValue(Field field, Object inst)
	{
		try {
			return (T) field.get(inst);
		} catch (IllegalArgumentException | IllegalAccessException e) {			
			throw new ReflectionException(e);
		}
	}
	
	public static <T> void setFieldValue(Field field, Object inst, Object value)
	{
		try {
			 field.set(inst, value);
		} catch (IllegalArgumentException | IllegalAccessException e) {
			throw new ReflectionException(e);
		}
	}

	private static class ReflectionException extends RuntimeException
	{
		private static final long serialVersionUID = 1L;

		public ReflectionException(Exception e)
		{
			super(e);
		}
	}
}
