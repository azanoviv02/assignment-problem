package com.netcracker.algorithms.auction;

import com.netcracker.algorithms.AssignmentProblemSolver;
import com.netcracker.algorithms.auction.auxillary.entities.Bid;
import com.netcracker.algorithms.auction.auxillary.entities.RelaxationPhaseResult;
import com.netcracker.algorithms.auction.auxillary.logic.relaxation.EpsilonProducer;
import com.netcracker.utils.logging.Logger;
import com.netcracker.utils.logging.SystemOutLogger;

import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@SuppressWarnings("ALL")
public class SynchronousJacobiAlgorithm implements AssignmentProblemSolver {

    private final static double INITIAL_PRICE = 1.0;
    private final static int UNASSIGNED_VALUE = -1;
    private final static double UNASSIGNED_DOUBLE = -Double.MAX_VALUE;

    private final int threadAmount;
    private final Logger logger;
    private final EpsilonProducer epsilonProducer;

    public SynchronousJacobiAlgorithm(int threadAmount) {
        this(threadAmount, new SystemOutLogger(false));
    }

    public SynchronousJacobiAlgorithm(int threadAmount, Logger logger) {
        this.threadAmount = threadAmount;
        this.logger = logger;
        this.epsilonProducer = new EpsilonProducer(1.0, .25);
    }

    @Override
    public int[] findMaxCostMatching(int[][] costMatrix) {
        logger.info("Solving problem for size: " + costMatrix.length);
        final int n = costMatrix.length;
        final double[] prices = getFilledDoubleArray(n, INITIAL_PRICE);

        final RelaxationPhaseResult finalResult =
                epsilonProducer
                        .getEpsilonList(n)
                        .stream()
                        .reduce(new RelaxationPhaseResult(null, prices),
                                (previousResult, epsilon) -> relaxationPhase(costMatrix, previousResult, epsilon),
                                (a, b) -> b
                        );

        final int[] assignment = finalResult.getAssignment();
        if (arrayContains(assignment, UNASSIGNED_VALUE)) {
            throw new IllegalStateException("Assignment is not complete");
        }
        return getReversedAssignment(assignment);
    }

    //todo find correct name for this method
    private RelaxationPhaseResult relaxationPhase(int[][] costMatrix, RelaxationPhaseResult relaxationPhaseResult, double epsilon) {
        double[] prices = relaxationPhaseResult.getPrices();
        logger.info("  Prices at the beginning of phase: " + Arrays.toString(prices));
        final int n = costMatrix.length;
        Queue<Integer> nonAssignedPersonQueue = getQueueOfRange(n);
        final int[] assignment = getFilledIntArray(n, UNASSIGNED_VALUE);

        while (!nonAssignedPersonQueue.isEmpty()) {
            auctionRound(assignment, nonAssignedPersonQueue, prices, costMatrix, epsilon);
        }

        logger.info("  Prices at the end       of phase: " + Arrays.toString(prices));
        logger.info("  Assignment at the end   of phase: " + Arrays.toString(assignment));
        return new RelaxationPhaseResult(assignment, prices);
    }

    private void auctionRound(int[] assignment,
                              Queue<Integer> nonAssignedPersonQueue,
                              double[] prices,
                              int[][] costMatrix,
                              double epsilon) {
        final int n = prices.length;
        final int nonAssignedAmount = nonAssignedPersonQueue.size();
        final int taskAmount = nonAssignedAmount;

        final Set<Bid> bidSet = Collections.newSetFromMap(new ConcurrentHashMap<>());
        final List<Runnable> taskList = new ArrayList<>(taskAmount);

        logger.info("    Non assigned at the beginning of round: " + nonAssignedPersonQueue);
        logger.info("    Assignment at the beginning of round: " + Arrays.toString(assignment));
        /* Bidding phase */
        for (int i : nonAssignedPersonQueue) {
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

        nonAssignedPersonQueue.clear();

        /* Assignment phase*/
        for (int j = 0; j < n; j++) {
            final int oldOwner = assignment[j];
            logger.info("      Bids for object number " + j);
            logger.info("      Old owner: " + oldOwner);
            final Queue<Bid> bidQueue = bidMap.get(j);
            if (bidQueue.isEmpty()) {
                logger.info("        No bids");
                continue;
            }
            if (oldOwner != -1) {
                nonAssignedPersonQueue.add(oldOwner);
            }

            final Bid highestBid = bidQueue.remove();
            final int highestBidderIndex = highestBid.getBidderIndex();
            assignment[j] = highestBidderIndex;
            final double highestBidValue = highestBid.getBidValue();
            prices[j] += highestBidValue;
            logger.info("        Highest bid: bidder - " + highestBidderIndex);

            for (Bid failedBid : bidQueue) {
                nonAssignedPersonQueue.add(failedBid.getBidderIndex());
                logger.info("        Failed bid: bidder - " + failedBid.getBidderIndex());
            }
        }

        Set<Integer> nonAssignedSet = new HashSet<>(nonAssignedPersonQueue);
        if (nonAssignedSet.size() != nonAssignedPersonQueue.size()) {
            throw new IllegalStateException("Queue contains duplicates");
        }

        logger.info("    Non assigned at the end: " + nonAssignedPersonQueue);
        logger.info("    Assignment at the end of round: " + Arrays.toString(assignment));
    }

    private static Queue<Integer> getQueueOfRange(int toExclusive) {
        Queue<Integer> rangeQueue = new ArrayDeque<>(toExclusive);
        for (int i = 0; i < toExclusive; i++) {
            rangeQueue.add(i);
        }
        return rangeQueue;
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

    private static int[] getReversedAssignment(int[] assignment) {
        final int n = assignment.length;
        final int[] reversedAssignment = new int[n];
        for (int i = 0; i < n; i++) {
            final int value = assignment[i];
            reversedAssignment[value] = i;
        }
        return reversedAssignment;
    }
}
