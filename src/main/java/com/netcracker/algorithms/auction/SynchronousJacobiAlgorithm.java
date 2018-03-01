package com.netcracker.algorithms.auction;

import com.netcracker.algorithms.AssignmentProblemSolver;
import com.netcracker.algorithms.auction.auxillary.entities.aggregates.Assignment;
import com.netcracker.algorithms.auction.auxillary.entities.aggregates.BenefitMatrix;
import com.netcracker.algorithms.auction.auxillary.entities.aggregates.ItemList;
import com.netcracker.algorithms.auction.auxillary.entities.basic.Bid;
import com.netcracker.algorithms.auction.auxillary.entities.aggregates.PersonQueue;
import com.netcracker.algorithms.auction.auxillary.entities.aggregates.PriceVector;
import com.netcracker.algorithms.auction.auxillary.entities.results.RelaxationPhaseResult;
import com.netcracker.algorithms.auction.auxillary.entities.basic.Item;
import com.netcracker.algorithms.auction.auxillary.entities.basic.Person;
import com.netcracker.algorithms.auction.auxillary.logic.relaxation.EpsilonProducer;
import com.netcracker.utils.logging.Logger;
import com.netcracker.utils.logging.SystemOutLogger;

import java.util.*;
import java.util.concurrent.*;

@SuppressWarnings("ALL")
public class SynchronousJacobiAlgorithm implements AssignmentProblemSolver {

    private final static double STARTING_VALUE = -Double.MAX_VALUE;

    private final int threadAmount;
    private final Logger logger;
    private final EpsilonProducer epsilonProducer;

    public SynchronousJacobiAlgorithm(int threadAmount) {
        this(threadAmount, new SystemOutLogger(true));
    }

    public SynchronousJacobiAlgorithm(int threadAmount, Logger logger) {
        this.threadAmount = threadAmount;
        this.logger = logger;
        this.epsilonProducer = new EpsilonProducer(1.0, .25);
    }

    @Override
    public int[] findMaxCostMatching(int[][] inputBenefitMatrix) {
        final BenefitMatrix benefitMatrix = new BenefitMatrix(inputBenefitMatrix);
        final int n = benefitMatrix.size();
        logger.info("Solving problem for size: %d", n);
        final PriceVector initialPriceVector = PriceVector.createInitialPriceVector(n);
        final RelaxationPhaseResult finalResult = epsilonProducer
                .getEpsilonList(n)
                .stream()
                .reduce(new RelaxationPhaseResult(null, initialPriceVector),
                        (previousResult, epsilon) -> relaxationPhase(benefitMatrix, previousResult, epsilon),
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
    private RelaxationPhaseResult relaxationPhase(BenefitMatrix benefitMatrix, RelaxationPhaseResult previousResult, double epsilon) {
        final int n = benefitMatrix.size();
        PriceVector priceVector = previousResult.getPriceVector();
        logger.info("  Prices at the beginning of phase: %s", priceVector);
        PersonQueue nonAssignedPersonQueue = PersonQueue.createInitialPersonQueue(n);
        final Assignment assignment = Assignment.createInitialAssignment(n);
        while (!nonAssignedPersonQueue.isEmpty()) {
            auctionRound(assignment, nonAssignedPersonQueue, priceVector, benefitMatrix, epsilon);
        }

        logger.info("  Prices at the end       of phase: %s", priceVector);
        logger.info("  Assignment at the end   of phase: %s", assignment);
        return new RelaxationPhaseResult(assignment, priceVector);
    }

    private void auctionRound(Assignment assignment,
                              PersonQueue nonAssignedPersonQueue,
                              PriceVector priceVector,
                              BenefitMatrix benefitMatrix,
                              double epsilon) {
        final int n = benefitMatrix.size();
        final ItemList itemList = ItemList.createItemList(n);
        final int nonAssignedAmount = nonAssignedPersonQueue.size();
        final int taskAmount = nonAssignedAmount;

        final Set<Bid> bidSet = Collections.newSetFromMap(new ConcurrentHashMap<>());
        final List<Runnable> taskList = new ArrayList<>(taskAmount);

        logger.info("    Non assigned at the beginning of round: %s", nonAssignedPersonQueue);
        logger.info("    Assignment at the beginning of round: %s", assignment);

        /* Bidding phase */
        /* Executed in parallel */
        for (Person person : nonAssignedPersonQueue) {

            Runnable makeBidTask = () -> {
                bidSet.add(
                        createBid(
                                person,
                                itemList,
                                benefitMatrix,
                                priceVector,
                                epsilon
                        )
                );
            };

            taskList.add(makeBidTask);
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
        Map<Item, Queue<Bid>> bidMap = processBids(bidSet, itemList);

        /* Assignment phase*/
        nonAssignedPersonQueue.clear();
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
            throw new IllegalStateException("Queue contains duplicates: " + nonAssignedPersonQueue);
        }

        logger.info("    Non assigned at the end: %s", nonAssignedPersonQueue);
        logger.info("    Assignment at the end of round: %s", assignment);
    }

    private Bid createBid(Person person,
                          ItemList itemList,
                          BenefitMatrix benefitMatrix,
                          PriceVector priceVector,
                          double epsilon) {
        double bestValue = STARTING_VALUE;
        double secondBestValue = STARTING_VALUE;
        Item bestItem = Item.NO_ITEM;

        for (Item item : itemList) {
            int benefit = benefitMatrix.getBenefit(person, item);
            double price = priceVector.getPriceFor(item);
            double value = benefit - price;
            if (value > bestValue) {
                secondBestValue = bestValue;
                bestValue = value;
                bestItem = item;
            } else if (value > secondBestValue) {
                secondBestValue = value;
            }
        }
        double bidValue = bestValue - secondBestValue + epsilon;
        return new Bid(person, bestItem, bidValue);
    }

    private Map<Item, Queue<Bid>> processBids(Set<Bid> bidSet, ItemList itemList) {
        Map<Item, Queue<Bid>> bidMap = new HashMap<>();
        for (Item item : itemList) {
            bidMap.put(item, new PriorityQueue<>(Collections.reverseOrder()));
        }
        for (Bid bid : bidSet) {
            final Item item = bid.getItem();
            final Queue<Bid> bidQueue = bidMap.get(item);
            bidQueue.add(bid);
        }
        return bidMap;
    }
}
