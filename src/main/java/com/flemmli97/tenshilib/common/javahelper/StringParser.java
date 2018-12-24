package com.flemmli97.tenshilib.common.javahelper;

public interface StringParser<T> {
	
	public static final StringParser<Object> objToString = new StringParser<Object>() {
		@Override
		public String getString(Object t) {
			return t.toString();
		}};
		
	public String getString(T t);

}
