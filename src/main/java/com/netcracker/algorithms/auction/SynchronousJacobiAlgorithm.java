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
    private final static double UNASSIGNED_DOUBLE = -Double.MAX_VALUE;

    private final int threadAmount;

    public SynchronousJacobiAlgorithm(int threadAmount) {
        this.threadAmount = threadAmount;
    }

    @Override
    public int[] findMaxCostMatching(int[][] costMatrix) {
        System.out.println("Solving problem for size: " + costMatrix.length);
        final int n = costMatrix.length;
        final double[] prices = getFilledDoubleArray(n, INITIAL_PRICE);

        int[] assignment = null;
        for (double epsilon = 1.0; epsilon > 1.0 / n; epsilon *= .25) {
            assignment = relaxationPhase(costMatrix, prices, epsilon);
        }
        return getReversedAssignment(assignment);
    }

    private int[] relaxationPhase(int[][] costMatrix, double[] prices, double epsilon) {
        System.out.println("  Prices at the beginning of phase: " + Arrays.toString(prices));
        final int n = costMatrix.length;
        List<Integer> nonAssignedList = getListOfRange(n);
        final int[] assignment = getFilledIntArray(n, UNASSIGNED_VALUE);
        while (!nonAssignedList.isEmpty()) {
            auctionRound(assignment, nonAssignedList, prices, costMatrix, epsilon);
        }
        System.out.println("  Prices at the end       of phase: " + Arrays.toString(prices));
        System.out.println("  Assignment at the end   of phase: " + Arrays.toString(assignment));
        return assignment;
    }

    private void auctionRound(int[] assignment,
                              List<Integer> nonAssignedList,
                              double[] prices,
                              int[][] costMatrix,
                              double epsilon) {
        final int n = prices.length;
        final int nonAssignedAmount = nonAssignedList.size();
        final int taskAmount = nonAssignedAmount;

        final Set<Bid> bidSet = Collections.newSetFromMap(new ConcurrentHashMap<>());
        final List<Runnable> taskList = new ArrayList<>(taskAmount);

        System.out.println("    Non assigned at the beginning of round: " + nonAssignedList);
        System.out.println("    Assignment at the beginning of round: " + Arrays.toString(assignment));
        /* Bidding phase */
        for (int i : nonAssignedList) {
            Runnable findBidTask = () -> {
                double bestValue = UNASSIGNED_DOUBLE;
                double secondBestValue = UNASSIGNED_DOUBLE;

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
        for (Runnable runnable : taskList) {
            futureList.add(executorService.submit(runnable));
        }
        for (Future future : futureList) {
            try {
                future.get();
            } catch (InterruptedException | ExecutionException e) {
                throw new IllegalStateException(e);
            }
        }
        executorService.shutdown();

        /* Processing bids */
        Map<Integer, Queue<Bid>> bidMap = new HashMap<>();
        for (int i = 0; i < n; i++) {
            bidMap.put(i, new PriorityQueue<>(Collections.reverseOrder()));
        }
        for (Bid bid : bidSet) {
            final int objectIndex = bid.getObjectIndex();
            final Queue<Bid> bidQueue = bidMap.get(objectIndex);
            bidQueue.add(bid);
        }

        nonAssignedList.clear();

        /* Assignment phase*/
        for (int j = 0; j < n; j++) {
            System.out.println("      Bids for object number " + j);
            final Queue<Bid> bidQueue = bidMap.get(j);
            if (bidQueue.isEmpty()) {
                System.out.println("        No bids");
                continue;
            }

            final Bid highestBid = bidQueue.remove();
            final int highestBidderIndex = highestBid.getBidderIndex();
            assignment[j] = highestBidderIndex;
            final double highestBidValue = highestBid.getBidValue();
            prices[j] += highestBidValue;
            System.out.println("        Highest bid: bidder - " + highestBidderIndex);

            for (Bid failedBid : bidQueue) {
                nonAssignedList.add(failedBid.getBidderIndex());
                System.out.println("        Failed bid: bidder - " + failedBid.getBidderIndex());
            }
        }

        Set<Integer> nonAssignedSet = new HashSet<>(nonAssignedList);
        if (nonAssignedSet.size() != nonAssignedList.size()) {
            throw new IllegalStateException("List contains duplicates");
        }

        System.out.println("    Non assigned at the end: " + nonAssignedList);
        System.out.println("    Assignment at the end of round: " + Arrays.toString(assignment));
    }

    private static List<Integer> getListOfRange(int toExclusive) {
        List<Integer> rangeList = new LinkedList<>();
        for (int i = 0; i < toExclusive; i++) {
            rangeList.add(i);
        }
        return rangeList;
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

    private static int[] getReversedAssignment(int[] assignment){
        final int n = assignment.length;
        final int[] reversedAssignment = new int[n];
        for(int i = 0; i < n; i++){
            final int value = assignment[i];
            reversedAssignment[value] = i;
        }
        return reversedAssignment;
    }
}
