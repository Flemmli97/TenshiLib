package com.flemmli97.tenshilib.common.javahelper;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class ReflectionUtils {

    @SuppressWarnings("unchecked")
    public static <T> T getFieldValue(Field field, Object inst) {
        try{
            field.setAccessible(true);
            return (T) field.get(inst);
        }catch(IllegalArgumentException | IllegalAccessException e){
            throw new ReflectionException(e);
        }
    }

    public static <T> void setFieldValue(Field field, Object inst, Object value) {
        try{
            field.setAccessible(true);
            field.set(inst, value);
        }catch(IllegalArgumentException | IllegalAccessException e){
            throw new ReflectionException(e);
        }
    }

    public static Field getField(Class<?> clss, String name) {
        try{
            Field f = clss.getDeclaredField(name);
            f.setAccessible(true);
            return f;
        }catch(NoSuchFieldException | SecurityException e){
            throw new ReflectionException(e);
        }
    }

    public static Method getMethod(Class<?> clss, String name, Class<?>... args) {
        try{
            Method m = clss.getDeclaredMethod(name, args);
            m.setAccessible(true);
            return m;
        }catch(SecurityException | NoSuchMethodException e){
            throw new ReflectionException(e);
        }
    }

    public static Object invokeMethod(Method method, Object inst, Object... args) {
        try{
            method.setAccessible(true);
            return method.invoke(inst, args);
        }catch(SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e){
            throw new ReflectionException(e);
        }
    }

    private static class ReflectionException extends RuntimeException {

        private static final long serialVersionUID = 1L;

        public ReflectionException(Exception e) {
            super(e);
        }
    }
}
