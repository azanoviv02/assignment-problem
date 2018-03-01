package com.netcracker.algorithms.auction;

import com.netcracker.algorithms.AssignmentProblemSolver;
import com.netcracker.algorithms.auction.auxillary.entities.aggregates.Assignment;
import com.netcracker.algorithms.auction.auxillary.entities.aggregates.BenefitMatrix;
import com.netcracker.algorithms.auction.auxillary.entities.aggregates.ItemList;
import com.netcracker.algorithms.auction.auxillary.entities.aggregates.PersonQueue;
import com.netcracker.algorithms.auction.auxillary.entities.aggregates.PriceVector;
import com.netcracker.algorithms.auction.auxillary.entities.basic.Bid;
import com.netcracker.algorithms.auction.auxillary.entities.basic.Item;
import com.netcracker.algorithms.auction.auxillary.entities.results.RelaxationPhaseResult;
import com.netcracker.utils.logging.Logger;
import com.netcracker.utils.logging.SystemOutLogger;

import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.Callable;

import static com.netcracker.algorithms.auction.auxillary.logic.bids.BidAggregator.aggregateBids;
import static com.netcracker.algorithms.auction.auxillary.logic.bids.BidMaker.createCallableList;
import static com.netcracker.algorithms.auction.auxillary.logic.bids.BidProcessor.processBidsAndUpdateAssignmentForItemList;
import static com.netcracker.algorithms.auction.auxillary.logic.relaxation.EpsilonProducer.getEpsilonList;
import static com.netcracker.algorithms.auction.auxillary.utils.ConcurrentUtils.executeCallableList;

@SuppressWarnings("ALL")
public class SynchronousJacobiAlgorithm implements AssignmentProblemSolver {

    private final int threadAmount;
    private final Logger logger;

    public SynchronousJacobiAlgorithm(int threadAmount) {
        this(threadAmount, new SystemOutLogger(false));
    }

    public SynchronousJacobiAlgorithm(int threadAmount, Logger logger) {
        this.threadAmount = threadAmount;
        this.logger = logger;
    }

    @Override
    public int[] findMaxCostMatching(int[][] inputBenefitMatrix) {
        final BenefitMatrix benefitMatrix = new BenefitMatrix(inputBenefitMatrix);
        final int n = benefitMatrix.size();
        logger.info("Solving problem for size: %d", n);
        final ItemList itemList = ItemList.createItemList(n);
        final PriceVector initialPriceVector = PriceVector.createInitialPriceVector(n);
        final RelaxationPhaseResult finalResult =
                getEpsilonList(1.0, 0.25, n)
                        .stream()
                        .reduce(new RelaxationPhaseResult(null, initialPriceVector),
                                (previousResult, epsilon) -> relaxationPhase(benefitMatrix, itemList, previousResult, epsilon),
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
    private RelaxationPhaseResult relaxationPhase(BenefitMatrix benefitMatrix,
                                                  ItemList itemList,
                                                  RelaxationPhaseResult previousResult,
                                                  double epsilon) {
        final int n = benefitMatrix.size();
        PriceVector priceVector = previousResult.getPriceVector();
        logger.info("  Prices at the beginning of phase: %s", priceVector);
        PersonQueue nonAssignedPersonQueue = PersonQueue.createFullPersonQueue(n);
        final Assignment assignment = Assignment.createInitialAssignment(n);
        while (!nonAssignedPersonQueue.isEmpty()) {
            auctionRound(
                    assignment,
                    nonAssignedPersonQueue,
                    itemList,
                    priceVector,
                    benefitMatrix,
                    epsilon
            );
        }

        logger.info("  Prices at the end       of phase: %s", priceVector);
        logger.info("  Assignment at the end   of phase: %s", assignment);
        return new RelaxationPhaseResult(assignment, priceVector);
    }

    private void auctionRound(Assignment assignment,
                              PersonQueue nonAssignedPersonQueue,
                              ItemList itemList,
                              PriceVector priceVector,
                              BenefitMatrix benefitMatrix,
                              double epsilon) {
        final int n = benefitMatrix.size();
        final int nonAssignedAmount = nonAssignedPersonQueue.size();
        final int taskAmount = nonAssignedAmount;

        logger.info("    Non assigned at the beginning of round: %s", nonAssignedPersonQueue);
        logger.info("    Assignment at the beginning of round: %s", assignment);

        //==================== Bid making =================================

        /*
            Multithreading part start
         */
        final List<Callable<Bid>> callableList = createCallableList(
                nonAssignedPersonQueue,
                itemList,
                benefitMatrix,
                priceVector,
                epsilon
        );
        final List<Bid> bidList = executeCallableList(callableList, threadAmount);
        /*
            Multithreading part end
         */

        //==================== Bid processing =============================

        Map<Item, Queue<Bid>> bidMap = aggregateBids(bidList, itemList);

        //==================== Assigment ==================================

        nonAssignedPersonQueue = processBidsAndUpdateAssignmentForItemList(
                assignment,
                priceVector,
                itemList,
                bidMap
        );

        if (nonAssignedPersonQueue.containsDuplicates()) {
            throw new IllegalStateException("Queue contains duplicates: " + nonAssignedPersonQueue);
        }

        logger.info("    Non assigned at the end: %s", nonAssignedPersonQueue);
        logger.info("    Assignment at the end of round: %s", assignment);
    }
}
