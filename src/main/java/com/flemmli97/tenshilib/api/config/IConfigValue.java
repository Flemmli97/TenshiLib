package com.flemmli97.tenshilib.api.config;

public interface IConfigValue {
	
	public IConfigValue readFromString(String s);
	
	public String writeToString();

	public String usage();
}
