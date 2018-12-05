package com.flemmli97.tenshilib.api.config;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.flemmli97.tenshilib.asm.ConfigUtils.Init;

/**
 * To fields with any of this annotations added additional it needs to have the @Config.Ignore annotation too.
 * Else it will get messed up.
 */
public class ConfigAnnotations {

	/**
	 * Field needs to implement IConfigValue, IConfigArrayValue or IConfigSerializable
	 */
	@Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.FIELD)
	public static @interface ConfigValue
	{
		/**
		 * Time where this field gets initialized. If its e.g. POST it gets initialized in PostInit
		 */
		Init getInitTime() default Init.RUNTIME;
	}	
}
