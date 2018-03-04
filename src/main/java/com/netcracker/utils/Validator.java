package com.netcracker.utils;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Validator {

    public static boolean containsDuplicates(List<Integer> list) {
        Set<Integer> set = new HashSet<>(list);
        return set.size() != list.size();
    }

    public static boolean assignmentsAreSame(Map<String, List<Integer>> assignmentMap) {
        return assignmentMap.values().stream().distinct().limit(2).count() <= 1;
    }
}
