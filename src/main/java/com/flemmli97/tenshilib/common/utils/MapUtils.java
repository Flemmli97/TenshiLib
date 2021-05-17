package com.flemmli97.tenshilib.common.utils;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class MapUtils {

    /**
     * Sorts the map using the given comparator into a {@link LinkedHashMap}
     */
    public static <K, L> Map<K, L> sort(Map<K, L> old, Comparator<K> c) {
        return old.entrySet().stream().sorted(Map.Entry.comparingByKey(c))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));
    }

    public static <K, V> List<String> toListKey(Map<K, V> map, Function<K, String> key) {
        List<String> list = new ArrayList<>();
        for (Map.Entry<K, V> e : map.entrySet())
            list.add(key.apply(e.getKey()) + " - " + e.getValue());
        return list;
    }

    public static <K, V> List<String> toListVal(Map<K, V> map, Function<V, String> val) {
        List<String> list = new ArrayList<>();
        for (Map.Entry<K, V> e : map.entrySet())
            list.add(e.getKey() + " - " + val.apply(e.getValue()));
        return list;
    }

    /**
     * Turns a map into a list of string in form "key - value". For consistency the map should be sorted or provide a consistent order
     */
    public static <K, V> List<String> mapToStringList(Map<K, V> map, Function<K, String> key, Function<V, String> val) {
        List<String> list = new ArrayList<>();
        for (Map.Entry<K, V> e : map.entrySet())
            list.add(key.apply(e.getKey()) + " - " + val.apply(e.getValue()));
        return list;
    }

    public static <K, V> String toString(Map<K, V> map, Function<K, String> key, Function<V, String> val) {
        return map.entrySet().stream().map(e -> key.apply(e.getKey()) + "=" + val.apply(e.getValue()))
                .collect(Collectors.joining(", ", "{", "}"));
    }
}
