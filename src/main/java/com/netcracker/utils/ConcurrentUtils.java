package com.netcracker.utils;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

public class ConcurrentUtils {

    public static <T> List<T> executeCallableList(List<? extends Callable<T>> callableList, ExecutorService executorService) {
        try {
            return getResultList(executorService.invokeAll(callableList));
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

    public static void awaitFutureCompletion(Future future) {
        try {
            future.get();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
    }

    public static void awaitFurureListCompletion(List<Future> futureList) {
        futureList.forEach(future -> {
            awaitFutureCompletion(future);
        });
    }
}
