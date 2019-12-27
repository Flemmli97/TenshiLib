package com.flemmli97.tenshilib.api.config;

public interface IConfigArrayValue<T extends IConfigArrayValue<T>> {
	
	public T readFromString(String[] s);
	
	public String[] writeToString();

	public String usage();
}
