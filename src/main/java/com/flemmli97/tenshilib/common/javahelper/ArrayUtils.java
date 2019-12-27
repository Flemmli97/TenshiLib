package com.flemmli97.tenshilib.common.javahelper;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.List;

import javax.annotation.Nullable;

import com.google.common.collect.Lists;

public class ArrayUtils {

    public static <T> String arrayToString(T[] t) {
        return arrayToString(t, null);
    }

    @SuppressWarnings("unchecked")
    public static <T> String arrayToString(T[] t, @Nullable StringParser<T> parser) {
        if(t == null || t.length == 0)
            return "";
        if(parser == null)
            parser = (StringParser<T>) StringParser.objToString;
        String s = "" + t[0] == null ? "" : parser.getString(t[0]);
        if(t.length == 1)
            return s;
        for(int i = 1; i < t.length; i++)
            s += "," + t[i] == null ? "NULL" : parser.getString(t[i]);
        return s;
    }

    public static <T> String arrayToString(int[] t) {
        if(t == null || t.length == 0)
            return "";
        String s = "" + t[0];
        if(t.length == 1)
            return s;
        for(int i = 1; i < t.length; i++)
            s += "," + t[i];
        return s;
    }

    public static <T> String arrayToString(float[] t) {
        if(t == null || t.length == 0)
            return "";
        String s = "" + t[0];
        if(t.length == 1)
            return s;
        for(int i = 1; i < t.length; i++)
            s += "," + t[i];
        return s;
    }

    public static <T> String arrayToString(double[] t) {
        if(t == null || t.length == 0)
            return "";
        String s = "" + t[0];
        if(t.length == 1)
            return s;
        for(int i = 1; i < t.length; i++)
            s += "," + t[i];
        return s;
    }

    public static <T> String[] arrayToStringArr(T[] ts) {
        if(ts == null)
            return new String[0];
        String[] arr = new String[ts.length];
        for(int i = 0; i < ts.length; i++)
            arr[i] = ts[i] != null ? ts[i].toString() : "";
        return arr;
    }

    public static String[] arrayToStringArr(int[] ts) {
        if(ts == null)
            return new String[0];
        String[] arr = new String[ts.length];
        for(int i = 0; i < ts.length; i++)
            arr[i] = "" + ts[i];
        return arr;
    }

    public static <T, M> M[] arrayConverter(T[] ts, ObjectConverter<T, M> parser, Class<M> clss) {
        return arrayConverter(ts, parser, clss, false);
    }

    public static <T, M> M[] arrayConverter(T[] ts, ObjectConverter<T, M> parser, Class<M> clss, boolean allowNullReturn) {
        return arrayConverter(ts, parser, clss, false, false);
    }

    @SuppressWarnings("unchecked")
    public static <T, M> M[] arrayConverter(T[] ts, ObjectConverter<T, M> parser, Class<M> clss, boolean allowNullReturn, boolean allowNullValue) {
        if(allowNullReturn && ts == null)
            return null;
        List<M> list = Lists.newArrayList();
        if(ts != null)
            for(T t : ts)
                if(allowNullValue || t != null)
                    list.add(t == null ? null : parser.convertFrom(t));
        M[] ms = (M[]) Array.newInstance(clss, list.size());
        for(int i = 0; i < ms.length; i++)
            ms[i] = list.get(i);
        return ms;
    }

    public static int[] intArrFromStringArr(String[] ts) {
        if(ts == null)
            return new int[0];
        int[] arr = new int[ts.length];
        for(int i = 0; i < ts.length; i++)
            arr[i] = Integer.parseInt(ts[i]);
        return arr;
    }

    public static <T> T[] combine(T[] array, T[][] toAdd) {
        List<T> list = Lists.newArrayList();
        list.addAll(Arrays.asList(array));
        for(T[] a : toAdd){
            list.addAll(Arrays.asList(a));
        }
        return list.toArray(array);
    }
}
