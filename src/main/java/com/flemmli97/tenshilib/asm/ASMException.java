package com.flemmli97.tenshilib.asm;

@SuppressWarnings("serial")
public class ASMException extends RuntimeException{

	private final Method m;
	public ASMException(String error, Method m)
	{
		super(error);
		this.m=m;
	}	
	
	public Method getMethod()
	{
		return this.m;
	}
	
	public static class ASMLoadException extends RuntimeException
	{
		public ASMLoadException()
		{
			super("TenshiCore failed to load");
		}
	}
}
