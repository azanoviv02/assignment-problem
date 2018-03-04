package com.netcracker.algorithms.auction;

import com.netcracker.algorithms.AssignmentProblemSolver;
import com.netcracker.algorithms.auction.auxillary.entities.aggregates.Assignment;
import com.netcracker.algorithms.auction.auxillary.entities.aggregates.BenefitMatrix;
import com.netcracker.algorithms.auction.auxillary.entities.aggregates.ItemList;
import com.netcracker.algorithms.auction.auxillary.entities.aggregates.PriceVector;
import com.netcracker.algorithms.auction.auxillary.logic.relaxation.EpsilonProducer;
import com.netcracker.algorithms.auction.implementation.AuctionImplementation;

import java.util.List;

import static com.netcracker.utils.io.logging.StaticLoggerHolder.info;

/**
 * Class for solving assignment problem using some implementation of the auction algorithm.
 *
 * This class uses composition to decouple actual auction algorithm implementation and the
 * logic of e-scaling.
 */
public class AuctionAlgorithm implements AssignmentProblemSolver {

    private final AuctionImplementation implementation;
    private final EpsilonProducer epsilonProducer;

    public AuctionAlgorithm(AuctionImplementation implementation) {
        this(implementation, new EpsilonProducer(1.0, 0.25));
    }

    public AuctionAlgorithm(AuctionImplementation implementation, EpsilonProducer epsilonProducer) {
        this.implementation = implementation;
        this.epsilonProducer = epsilonProducer;
    }

    @Override
    public int[] findMaxCostMatching(int[][] inputBenefitMatrix) {
        final BenefitMatrix benefitMatrix = new BenefitMatrix(inputBenefitMatrix);
        final int n = benefitMatrix.size();
        info("Solving problem for size: %d", n);
        final ItemList itemList = ItemList.createItemList(n);
        final PriceVector priceVector = PriceVector.createInitialPriceVector(n);
        final List<Double> epsilonList = epsilonProducer.getEpsilonList(n);
        Assignment assignment = null;
        for(Double epsilon : epsilonList){
            assignment = implementation.relaxationPhase(benefitMatrix, priceVector, epsilon);
        }
        if (assignment.isComplete()) {
            return assignment.getPersonAssignment();
        } else {
            throw new IllegalStateException("Assignment is not complete");
        }
    }
}
