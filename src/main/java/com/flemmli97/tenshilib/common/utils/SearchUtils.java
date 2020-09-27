package com.flemmli97.tenshilib.common.utils;

import java.util.List;
import java.util.function.Function;

public class SearchUtils {

    /**
     * Searches for the infimum value in the given array.
     * @param l The sorted list
     * @param search A function that should return 0,1,-1 indicating equals, greater or lesser
     * @return
     */
    public static <T extends Comparable<T>> T searchInfFunc(List<T> l, Function<T,Integer> search){
        return get(l, 0, l.size(), search);
    }

    /**
     * Searches for the infimum value in the given array.
     * @param l The sorted list
     * @param search The value to search for
     * @return
     */
    public static <T extends Comparable<T>> T searchInf(List<T> l, T search){
        return get(l, 0, l.size(), search);
    }

    /**
     * Searches for the infimum value in the given array.
     * @param arr The sorted array
     * @param search The value to search for
     * @return
     */
    public static <T extends Comparable<T>> T searchInf(T[] arr, T search){
        return get(arr, 0, arr.length, search);
    }

    private static <T extends Comparable<T>> T get(List<T> l, int min, int max, Function<T,Integer> search){
        int i = (max-min)/2;
        T val = l.get(min+i);
        if(search.apply(val) == 0)
            return val;
        if(search.apply(val) < 0) {
            if(i+1 == l.size() || search.apply(l.get(min+i+1)) > 0)
                return val;
            return get(l, min+i, max, search);
        }
        return get(l, min, max-i, search);
    }

    private static <T extends Comparable<T>> T get(List<T> l, int min, int max, T search){
        int i = (max-min)/2;
        T val = l.get(min+i);
        if(val.compareTo(search) == 0)
            return val;
        if(val.compareTo(search) < 0) {
            if(i+1 == l.size() || l.get(min+i+1).compareTo(search) > 0)
                return val;
            return get(l, min+i, max, search);
        }
        return get(l, min, max-i, search);
    }

    private static <T extends Comparable<T>> T get(T[]arr, int min, int max, T search){
        int i = (max-min)/2;
        T val = arr[min+i];
        if(val.compareTo(search) == 0)
            return val;
        if(val.compareTo(search) < 0) {
            if(i+1 == arr.length || arr[min+i+1].compareTo(search) > 0)
                return val;
            return get(arr, min+i, max, search);
        }
        return get(arr, min, max-i, search);
    }
}
