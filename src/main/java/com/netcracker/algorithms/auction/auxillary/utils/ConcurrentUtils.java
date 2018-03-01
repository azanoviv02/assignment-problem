package com.netcracker.algorithms.auction.auxillary.utils;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

public class ConcurrentUtils {

    public static <T> List<T> executeCallableList(List<Callable<T>> callableList, int threadAmount) {
        try {
            ExecutorService executorService = Executors.newFixedThreadPool(threadAmount);
            List<Future<T>> futureList = executorService.invokeAll(callableList);
            List<T> resultList = getResultList(futureList);
            executorService.shutdown();
            return resultList;
        } catch (InterruptedException e) {
            throw new IllegalStateException(e);
        }
    }

    public static <T> List<T> getResultList(List<Future<T>> futureList) {
        return futureList
                .stream()
                .map(future -> getResult(future))
                .collect(Collectors.toList());
    }

    public static <T> T getResult(Future<T> future) {
        try {
            return future.get();
        } catch (InterruptedException | ExecutionException e) {
            throw new IllegalStateException(e);
        }
    }
}
