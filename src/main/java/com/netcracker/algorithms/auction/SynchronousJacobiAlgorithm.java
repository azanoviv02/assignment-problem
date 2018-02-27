package com.netcracker.algorithms.auction;

import com.netcracker.algorithms.AssignmentProblemSolver;
import com.netcracker.algorithms.auction.auxillary.Bid;

import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@SuppressWarnings("ALL")
public class SynchronousJacobiAlgorithm implements AssignmentProblemSolver {

    private final static double INITIAL_PRICE = 1.0;
    private final static int UNASSIGNED_VALUE = Integer.MAX_VALUE;

    private final int threadAmount;

    List<Integer> nonAssigned;

    public SynchronousJacobiAlgorithm(int threadAmount) {
        this.threadAmount = threadAmount;
    }

    @Override
    public int[] findMaxCostMatching(int[][] costMatrix) {
        final int n = costMatrix.length;

        double[] prices = getFilledDoubleArray(n, INITIAL_PRICE);
        int[] assignment = getFilledIntArray(n, UNASSIGNED_VALUE);

        for (double epsilon = 1.0; epsilon > 1.0 / n; epsilon *= .25) {
            assignment = getFilledIntArray(n, UNASSIGNED_VALUE);
            nonAssigned = new LinkedList<>();
            for(int i = 0; i < n; i++){
                nonAssigned.add(i);
            }
            while (arrayContains(assignment, UNASSIGNED_VALUE)) {
                auctionRound(assignment, prices, costMatrix, epsilon);
            }
        }

        return assignment;
    }

    private void auctionRound(int[] assignment,
                              double[] prices,
                              int[][] costMatrix,
                              double epsilon) {

        final int n = prices.length;

        Set<Bid> bidSet = Collections.newSetFromMap(new ConcurrentHashMap<>());
        List<Runnable> taskList = new ArrayList<>(nonAssigned.size());
        /* Bidding phase */
        for (int i : nonAssigned) {
            Runnable findBidTask = ()-> {
                double bestValue = -UNASSIGNED_VALUE;
                double secondBestValue = -UNASSIGNED_VALUE;

                int bestObjectIndex = 0;

                for (int j = 0; j < n; j++) {
                    int cost = costMatrix[i][j];
                    double price = prices[j];
                    double value = cost - price;

                    if (value > bestValue) {
                        secondBestValue = bestValue;
                        bestValue = value;
                        bestObjectIndex = j;
                    } else if (value > secondBestValue) {
                        secondBestValue = value;
                    }
                }

			    /* Computes the highest reasonable bid for the best object for this person */
                double bidValue = bestValue - secondBestValue + epsilon;

                bidSet.add(new Bid(i, bestObjectIndex, bidValue));
            };
            taskList.add(findBidTask);
        }
        ExecutorService executorService = Executors.newFixedThreadPool(threadAmount);
        List<Future> futureList = new LinkedList<>();
        for(Runnable runnable : taskList){
            futureList.add(executorService.submit(runnable));
        }
        for(Future future : futureList){
            try {
                future.get();
            } catch (InterruptedException e) {
                throw new IllegalStateException(e);
            } catch (ExecutionException e) {
                throw new IllegalStateException(e);
            }
        }
        executorService.shutdown();

        Map<Integer, Bid> bidMap = new HashMap<>();
        for(Bid bid : bidSet){
            int objectIndex = bid.getObjectIndex();
            Bid oldBid = bidMap.get(objectIndex);
            if(oldBid == null || bid.compareTo(oldBid) > 0){
                bidMap.put(objectIndex, bid);
            }
        }

        /* Assignment phase*/
        for (int j = 0; j < n; j++) {
//            List<Integer> bidderIndexList = getIndicesWithValue(tempBidded, j);
            Bid highestBid = bidMap.get(j);
            if (highestBid != null) {
                double highestBidValue = highestBid.getBidValue();
                int highestBidderIndex = highestBid.getBidderIndex();

			    /* Find the other person who has object j and make them unassigned */
                for (int i = 0; i < assignment.length; i++) {
                    if (assignment[i] == j) {
                        nonAssigned.add(i);
                        assignment[i] = UNASSIGNED_VALUE;
//                        System.out.println("Bidder "+i+" lost object "+j);
                        break;
                    }
                }

                assignment[highestBidderIndex] = j;
                nonAssigned.remove(Integer.valueOf(highestBidderIndex));
                prices[j] += highestBidValue;
            }
        }
    }

    private static int[] getFilledIntArray(int n, int value) {
        int[] array = new int[n];
        Arrays.fill(array, value);
        return array;
    }

    private static double[] getFilledDoubleArray(int n, double value) {
        double[] array = new double[n];
        Arrays.fill(array, value);
        return array;
    }

    private static boolean arrayContains(int[] array, int value) {
        for (int element : array) {
            if (element == value) {
                return true;
            }
        }
        return false;
    }

    private static List<Integer> getIndicesWithValue(final List<Integer> list, final int value) {
        return IntStream
                .range(0, list.size())
                .filter(i -> list.get(i) == value)
                .boxed()
                .collect(Collectors.toList());
    }
}
