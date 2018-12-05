package com.flemmli97.tenshilib.api.config;

public interface IConfigArrayValue {
	
	public IConfigArrayValue readFromString(String[] s);
	
	public String[] writeToString();

	public String usage();
}
