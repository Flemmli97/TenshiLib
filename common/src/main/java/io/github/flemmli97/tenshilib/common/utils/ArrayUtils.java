package io.github.flemmli97.tenshilib.common.utils;

import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class ArrayUtils {

    public static <T> String arrayToString(T[] t, @Nullable Function<T, String> parser) {
        if (t == null || t.length == 0)
            return "";
        if (parser == null)
            parser = T::toString;
        StringBuilder s = new StringBuilder(t[0] == null ? "" : parser.apply(t[0]));
        if (t.length == 1)
            return s.toString();
        for (int i = 1; i < t.length; i++)
            s.append(",").append(t[i] == null ? "NULL" : parser.apply(t[i]));
        return s.toString();
    }

    public static <T, M> M[] arrayConverter(T[] ts, Function<T, M> parser, Class<M> clss) {
        return arrayConverter(ts, parser, clss, false);
    }

    public static <T, M> M[] arrayConverter(T[] ts, Function<T, M> parser, Class<M> clss, boolean allowNullReturn) {
        return arrayConverter(ts, parser, clss, false, false);
    }

    @SuppressWarnings("unchecked")
    public static <T, M> M[] arrayConverter(T[] ts, Function<T, M> parser, Class<M> clss, boolean allowNullReturn, boolean allowNullValue) {
        if (allowNullReturn && ts == null)
            return null;
        List<M> list = new ArrayList<>();
        if (ts != null)
            for (T t : ts)
                if (allowNullValue || t != null)
                    list.add(t == null ? null : parser.apply(t));
        M[] ms = (M[]) Array.newInstance(clss, list.size());
        for (int i = 0; i < ms.length; i++)
            ms[i] = list.get(i);
        return ms;
    }
}
