package com.netcracker.algorithms.auction;

import com.netcracker.algorithms.AssignmentProblemSolver;
import com.netcracker.algorithms.auction.auxillary.entities.aggregates.Assignment;
import com.netcracker.algorithms.auction.auxillary.entities.aggregates.ItemList;
import com.netcracker.algorithms.auction.auxillary.entities.basic.Bid;
import com.netcracker.algorithms.auction.auxillary.entities.aggregates.PersonQueue;
import com.netcracker.algorithms.auction.auxillary.entities.aggregates.PriceVector;
import com.netcracker.algorithms.auction.auxillary.entities.RelaxationPhaseResult;
import com.netcracker.algorithms.auction.auxillary.entities.basic.Item;
import com.netcracker.algorithms.auction.auxillary.entities.basic.Person;
import com.netcracker.algorithms.auction.auxillary.logic.relaxation.EpsilonProducer;
import com.netcracker.utils.logging.Logger;
import com.netcracker.utils.logging.SystemOutLogger;

import java.util.*;
import java.util.concurrent.*;

@SuppressWarnings("ALL")
public class SynchronousJacobiAlgorithm implements AssignmentProblemSolver {

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
        final int n = costMatrix.length;
        logger.info("Solving problem for size: %d", n);
        final PriceVector initialPriceVector = PriceVector.createInitialPriceVector(n);
        final RelaxationPhaseResult finalResult = epsilonProducer
                .getEpsilonList(n)
                .stream()
                .reduce(new RelaxationPhaseResult(null, initialPriceVector),
                        (previousResult, epsilon) -> relaxationPhase(costMatrix, previousResult, epsilon),
                        (a, b) -> b
                );
        final Assignment finalAssignment = finalResult.getAssignment();
        if (finalAssignment.isComplete()) {
            return finalAssignment.getPersonAssignment();
        } else {
            throw new IllegalStateException("Assignment is not complete");
        }
    }

    //todo find correct name for this method
    private RelaxationPhaseResult relaxationPhase(int[][] costMatrix, RelaxationPhaseResult previousResult, double epsilon) {
        PriceVector priceVector = previousResult.getPriceVector();
        logger.info("  Prices at the beginning of phase: %s", priceVector);
        final int n = costMatrix.length;
        PersonQueue nonAssignedPersonQueue = PersonQueue.createInitialPersonQueue(n);
        final Assignment assignment = Assignment.createInitialAssignment(n);
        while (!nonAssignedPersonQueue.isEmpty()) {
            auctionRound(assignment, nonAssignedPersonQueue, priceVector, costMatrix, epsilon);
        }

        logger.info("  Prices at the end       of phase: %s", priceVector);
        logger.info("  Assignment at the end   of phase: %s", assignment);
        return new RelaxationPhaseResult(assignment, priceVector);
    }

    private void auctionRound(Assignment assignment,
                              PersonQueue nonAssignedPersonQueue,
                              PriceVector priceVector,
                              int[][] costMatrix,
                              double epsilon) {
        final int n = costMatrix.length;
        final ItemList itemList = ItemList.createItemList(n);
        final int nonAssignedAmount = nonAssignedPersonQueue.size();
        final int taskAmount = nonAssignedAmount;

        final Set<Bid> bidSet = Collections.newSetFromMap(new ConcurrentHashMap<>());
        final List<Runnable> taskList = new ArrayList<>(taskAmount);

        logger.info("    Non assigned at the beginning of round: %s", nonAssignedPersonQueue);
        logger.info("    Assignment at the beginning of round: %s", assignment);
        /* Bidding phase */
        for (Person person : nonAssignedPersonQueue) {
            Runnable findBidTask = () -> {
                double bestValue = UNASSIGNED_DOUBLE;
                double secondBestValue = UNASSIGNED_DOUBLE;

                Item bestItem = Item.NO_ITEM;
                for (Item item : itemList) {
                    int cost = costMatrix[person.getPersonIndex()][item.getItemIndex()];
                    double price = priceVector.getPriceFor(item);
                    double value = cost - price;

                    if (value > bestValue) {
                        secondBestValue = bestValue;
                        bestValue = value;
                        bestItem = item;
                    } else if (value > secondBestValue) {
                        secondBestValue = value;
                    }
                }

			    /* Computes the highest reasonable bid for the best item for this person */
                double bidValue = bestValue - secondBestValue + epsilon;

                bidSet.add(new Bid(person, bestItem, bidValue));
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
        Map<Item, Queue<Bid>> bidMap = new HashMap<>();
        for (Item item : itemList) {
            bidMap.put(item, new PriorityQueue<>(Collections.reverseOrder()));
        }
        for (Bid bid : bidSet) {
            final Item item = bid.getItem();
            final Queue<Bid> bidQueue = bidMap.get(item);
            bidQueue.add(bid);
        }

        nonAssignedPersonQueue.clear();

        /* Assignment phase*/
        for (Item item : itemList) {
            final Person oldOwner = assignment.getPersonForItem(item);
            logger.info("      Bids for item number %s", item);
            logger.info("      Old owner: %s", oldOwner);
            final Queue<Bid> bidQueue = bidMap.get(item);
            if (bidQueue.isEmpty()) {
                logger.info("        No bids");
                continue;
            }
            if (oldOwner != Person.NO_PERSON) {
                nonAssignedPersonQueue.add(oldOwner);
            }

            final Bid highestBid = bidQueue.remove();
            final Person highestBidder = highestBid.getPerson();
            assignment.setPersonForItem(item, highestBidder);
            final double highestBidValue = highestBid.getBidValue();
            priceVector.increasePrice(item, highestBidValue);
            logger.info("        Highest bid: bidder - %s", highestBidder);

            for (Bid failedBid : bidQueue) {
                nonAssignedPersonQueue.add(failedBid.getPerson());
                logger.info("        Failed bid: bidder - %s", failedBid.getPerson());
            }
        }

        if (nonAssignedPersonQueue.containsDuplicates()) {
            throw new IllegalStateException("Queue contains duplicates: "+nonAssignedPersonQueue);
        }

        logger.info("    Non assigned at the end: %s", nonAssignedPersonQueue);
        logger.info("    Assignment at the end of round: %s", assignment);
    }
}
