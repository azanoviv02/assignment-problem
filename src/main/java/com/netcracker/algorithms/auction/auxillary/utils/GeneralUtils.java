package com.netcracker.algorithms.auction.auxillary.utils;

import java.util.List;
import java.util.stream.Collectors;

public class GeneralUtils {

    public static <T> List<T> flatten(List<List<T>> nestedList) {
        return nestedList
                .stream()
                .flatMap(List::stream)
                .collect(Collectors.toList());
    }
}
