package com.flemmli97.tenshilib.common.utils;

import java.util.List;
import java.util.function.Function;

public class SearchUtils {

    /**
     * Searches for the infimum value in the given array.
     * @param l The sorted list
     * @param search A function that should return 0,1,-1 indicating equals, greater or lesser
     */
    public static <T extends Comparable<T>> T searchInfFunc(List<T> l, Function<T,Integer> search, T defaultVal){
        if(l.isEmpty() || search.apply(l.get(0))>0)
            return defaultVal;
        return get(l, 0, l.size(), search);
    }

    /**
     * Searches for the infimum value in the given array.
     * @param l The sorted list
     * @param search The value to search for
     */
    public static <T extends Comparable<T>> T searchInf(List<T> l, T search, T defaultVal){
        if(l.isEmpty() || l.get(0).compareTo(search)>0)
            return defaultVal;
        return get(l, 0, l.size(), search);
    }

    /**
     * Searches for the infimum value in the given array.
     * @param arr The sorted array
     * @param search The value to search for
     */
    public static <T extends Comparable<T>> T searchInf(T[] arr, T search, T defaultVal){
        if(arr.length==0 || arr[0].compareTo(search)>0)
            return defaultVal;
        return get(arr, 0, arr.length, search);
    }

    private static <T extends Comparable<T>> T get(List<T> l, int min, int max, Function<T,Integer> search){
        int id = ((max-min)/2)+min;
        T val = l.get(id);
        if(search.apply(val) == 0)
            return val;
        if(search.apply(val) < 0) {
            if(id+1 >= l.size() || search.apply(l.get(id+1)) > 0)
                return val;
            return get(l, id, max, search);
        }
        return get(l, min, id, search);
    }

    private static <T extends Comparable<T>> T get(List<T> l, int min, int max, T search){
        int id = ((max-min)/2)+min;
        T val = l.get(id);
        if(val.compareTo(search) == 0)
            return val;
        if(val.compareTo(search) < 0) {
            if(id+1 >= l.size() || l.get(id+1).compareTo(search) > 0)
                return val;
            return get(l, id, max, search);
        }
        return get(l, min, id, search);
    }

    private static <T extends Comparable<T>> T get(T[]arr, int min, int max, T search){
        int id = ((max-min)/2)+min;
        T val = arr[id];
        if(val.compareTo(search) == 0)
            return val;
        if(val.compareTo(search) < 0) {
            if(id+1 >= arr.length || arr[id+1].compareTo(search) > 0)
                return val;
            return get(arr, id, max, search);
        }
        return get(arr, min, id, search);
    }
}
