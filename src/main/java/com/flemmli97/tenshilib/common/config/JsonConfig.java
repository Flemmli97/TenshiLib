package com.flemmli97.tenshilib.common.config;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import javax.annotation.Nullable;

import com.google.common.io.Files;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonIOException;

public class JsonConfig<T extends JsonElement> {

	public static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
	
	private File file;
	private T element;
	private Class<T> type;
	private boolean mcRestart, worldRestart;
	private String name;
	private Gson gson = GSON;
	
	public JsonConfig(File file, Class<T> type, @Nullable File defaultConfig)
	{
		this.file=file;
		this.type=type;
		this.name=this.file.getName();
		if(this.file.getParentFile()==null)
			this.file.mkdirs();
		if(!this.file.exists())
			try {
				this.file.createNewFile();
				if(defaultConfig!=null && defaultConfig.exists())
					Files.copy(defaultConfig, this.file);
			} catch (IOException e) {
				e.printStackTrace();
			}
		this.load();
	}
	
	public JsonConfig<T> setGson(Gson gson)
	{
		this.gson=gson;
		return this;
	}
	
	public File getConfigFile()
	{
		return this.file;
	}
	
	public T getElement()
	{
		return this.element;
	}
	
	public JsonConfig<T> setName(String name)
	{
		this.name=name;
		return this;
	}
	
	public String getName()
	{
		return this.name;
	}
	
	public void setElement(T element)
	{
		this.element=element;
	}
	
	public JsonConfig<T> setMCRestart(boolean flag)
	{
		this.mcRestart=flag;
		return this;
	}
	
	public JsonConfig<T> setWorldRestart(boolean flag)
	{
		this.worldRestart=flag;
		return this;
	}
	
	public boolean mcRestart()
	{
		return this.mcRestart;
	}
	
	public boolean worldRestart()
	{
		return this.worldRestart;
	}
	
	public void load()
	{
		try {
			FileReader reader = new FileReader(this.file);
			this.element=gson.fromJson(reader, this.type);
			reader.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void save()
	{
		try {
			FileWriter writer = new FileWriter(this.file);
			gson.toJson(this.element, writer);
			writer.close();
		} catch (JsonIOException | IOException e) {
			e.printStackTrace();
		}
	}
}
