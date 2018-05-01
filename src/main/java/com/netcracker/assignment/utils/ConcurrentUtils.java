package com.netcracker.assignment.utils;

import java.util.ArrayList;
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

    public static List<Runnable> replicateRunnable(Runnable runnable, int numberOfThreads) {
        ArrayList<Runnable> runnableList = new ArrayList<>(numberOfThreads);
        for (int i = 0; i < numberOfThreads; i++) {
            runnableList.add(runnable);
        }
        return runnableList;
    }

    public static void executeRunnableInParallel(Runnable runnable, int numberOfThreads) {
        ExecutorService executorService = createExecutorService(numberOfThreads);
        executeRunnableInParallel(runnable, numberOfThreads, executorService);
        executorService.shutdown();
    }

    public static void executeRunnableInParallel(Runnable runnable, int numberOfThreads, ExecutorService executorService) {
        List<Runnable> runnableList = replicateRunnable(runnable, numberOfThreads);
        executeRunnableList(executorService, runnableList);
    }

    public static void executeRunnableList(ExecutorService executorService, List<Runnable> runnableList) {
        // execute all tasks
        List<Future> futureList = runnableList
                .stream()
                .map(executorService::submit)
                .collect(Collectors.toList());
        futureList.forEach(future -> {
            try {
                future.get();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
        });
    }
}
