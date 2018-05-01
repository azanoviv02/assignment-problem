package com.netcracker.algorithms.auction.auxillary.utils;

import java.util.List;
import java.util.concurrent.*;
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

    public static ExecutorService createExecutorService(int numberOfThreads) {
        return Executors.newFixedThreadPool(numberOfThreads);
    }

    public static void await(CyclicBarrier barrier) {
        try {
            barrier.await();
        } catch (InterruptedException | BrokenBarrierException e) {
            throw new IllegalStateException(e);
        }
    }
}
