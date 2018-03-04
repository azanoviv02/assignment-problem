package com.netcracker.utils;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collector;
import java.util.stream.Collectors;

public class GeneralUtils {

    public static <T> List<T> flatten(List<List<T>> nestedList) {
        return nestedList
                .stream()
                .flatMap(List::stream)
                .collect(Collectors.toList());
    }

    public static List<Integer> convertArrayToList(int[] array) {
        return Arrays
                .stream(array)
                .boxed()
                .collect(Collectors.toList());
    }

    public static <T, K, U> Collector<T, ?, Map<K, U>> toLinkedMap(
            Function<? super T, ? extends K> keyMapper,
            Function<? super T, ? extends U> valueMapper) {
        return Collectors.toMap(keyMapper, valueMapper,
                (u, v) -> {
                    throw new IllegalStateException(String.format("Duplicate key %s", u));
                },
                LinkedHashMap::new);
    }
}
