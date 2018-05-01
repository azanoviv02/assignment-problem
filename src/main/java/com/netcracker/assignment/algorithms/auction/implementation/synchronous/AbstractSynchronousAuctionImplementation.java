package com.netcracker.assignment.algorithms.auction.implementation.synchronous;

import com.netcracker.assignment.algorithms.auction.auxillary.entities.aggregates.*;
import com.netcracker.assignment.algorithms.auction.auxillary.entities.aggregates.item.ItemList;
import com.netcracker.assignment.algorithms.auction.auxillary.entities.basic.Bid;
import com.netcracker.assignment.algorithms.auction.auxillary.entities.basic.Item;
import com.netcracker.assignment.algorithms.auction.implementation.AuctionImplementation;

import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static com.netcracker.assignment.algorithms.auction.auxillary.logic.bids.BidAggregator.aggregateBids;
import static com.netcracker.assignment.algorithms.auction.auxillary.logic.bids.BidProcessor.processBidsAndUpdateAssignmentForItemList;
import static com.netcracker.assignment.utils.AssertionMaker.makeAssertion;
import static com.netcracker.assignment.utils.io.logging.StaticLoggerHolder.info;

/**
 * Synchronous implementation of the auction algorithm.
 * <p>
 * I created a common parent for all synchronous implementations because
 * they all share the concept of "auction round" (distinct phases within "epsilonscaling phase"
 * which are marked by synchronization points).
 * <p>
 * Even within this "auction round" all synchronous algorithms have the same actions except
 * during making bids. Here each implementation has its own specifics:
 * - Single threaded implementation uses single bid task for all people
 * and only a single search task per person.
 * - Jacobi implementation has several bid tasks, which are executed in parallel. Within bid
 * task each person still has only one search task (i.e. parallel across the bids).
 * - Gauss-Seidel implementation has only one bid task for all people, but each person
 * has several search tasks (i.e. parallel within bid).
 * - Hybrid is parallel both within and across bids.
 * <p>
 * All other stages (i.e. bid aggregation, bid processing, price and assignment updating) happen
 * without any concurrency.
 */
public abstract class AbstractSynchronousAuctionImplementation implements AuctionImplementation {

    private final int numberOfThreads;

    public AbstractSynchronousAuctionImplementation(int numberOfThreads) {
        this.numberOfThreads = numberOfThreads;
    }

    public int getNumberOfThreads() {
        return numberOfThreads;
    }

    @Override
    public Assignment epsilonScalingPhase(BenefitMatrix benefitMatrix,
                                          PriceVector priceVector,
                                          double epsilon) {
        final int n = benefitMatrix.size();

        final PersonQueue nonAssignedPersonQueue = PersonQueue.createFullPersonQueue(n);
        final ItemList itemList = ItemList.createFullItemList(n);
        final Assignment assignment = Assignment.createInitialAssignment(n);

        final ExecutorService executorService = createExecutorService(numberOfThreads);
        while (!nonAssignedPersonQueue.isEmpty()) {
            auctionRound(
                    benefitMatrix,
                    priceVector,
                    epsilon,
                    nonAssignedPersonQueue,
                    itemList,
                    assignment,
                    executorService
            );
            makeAssertion(!nonAssignedPersonQueue.containsDuplicates());
            info("    Non assigned at the end: %s", nonAssignedPersonQueue);
            info("    Assignment at the end of round: %s", assignment);
        }
        executorService.shutdown();

        info("  Prices at the end       of phase: %s", priceVector);
        info("  Assignment at the end   of phase: %s", assignment);

        return assignment;
    }

    public void auctionRound(BenefitMatrix benefitMatrix,
                             PriceVector priceVector,
                             double epsilon,
                             PersonQueue nonAssignedPersonQueue,
                             ItemList itemList,
                             Assignment assignment,
                             ExecutorService executorService) {
        //==================== Bid making, done in parallel, depends on implementation

        info("Making bids for persons: %s", nonAssignedPersonQueue);
        final List<Bid> bidList = makeBids(
                benefitMatrix,
                priceVector,
                epsilon,
                nonAssignedPersonQueue,
                itemList,
                executorService
        );

        //==================== Bid processing =============================

        info("Aggregating bids: %s", bidList);
        Map<Item, Queue<Bid>> bidMap = aggregateBids(bidList);

        //==================== Assignment ==================================

        info("Processing aggregated bids: %s", bidMap);
        processBidsAndUpdateAssignmentForItemList(
                assignment,
                priceVector,
                nonAssignedPersonQueue,
                itemList,
                bidMap
        );
    }

    public abstract List<Bid> makeBids(BenefitMatrix benefitMatrix, PriceVector priceVector, double epsilon, PersonQueue nonAssignedPersonQueue, ItemList itemList, ExecutorService executorService);

    private static ExecutorService createExecutorService(int numberOfThreads) {
        return Executors.newFixedThreadPool(numberOfThreads);
    }
}
