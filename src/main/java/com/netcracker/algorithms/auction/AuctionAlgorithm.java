package com.netcracker.algorithms.auction;

import com.netcracker.algorithms.AssignmentProblemSolver;
import com.netcracker.algorithms.auction.auxillary.entities.aggregates.Assignment;
import com.netcracker.algorithms.auction.auxillary.entities.aggregates.BenefitMatrix;
import com.netcracker.algorithms.auction.auxillary.entities.aggregates.PriceVector;
import com.netcracker.algorithms.auction.auxillary.logic.epsilonscaling.DefaultEpsilonSequenceProducer;
import com.netcracker.algorithms.auction.auxillary.logic.epsilonscaling.EpsilonSequenceProducer;
import com.netcracker.algorithms.auction.implementation.AuctionImplementation;

import java.util.List;

import static com.netcracker.utils.io.logging.StaticLoggerHolder.info;

/**
 * Class for solving assignment problem using some implementation of the auction algorithm.
 *
 * This class uses composition to decouple actual auction algorithm implementation and the
 * logic of epsilon scaling.
 *
 * This class is aware of the concept of "epsilon scaling phase" (via AuctionImplementation.
 * epsilonScalingPhase(...) method) but it is NOT aware of the concept of "auction round"
 * (because asynchronous implementations have no concept of "round").
 */
public class AuctionAlgorithm implements AssignmentProblemSolver {

    private final AuctionImplementation implementation;
    private final EpsilonSequenceProducer epsilonProducer;

    public AuctionAlgorithm(AuctionImplementation implementation) {
        this(implementation, new DefaultEpsilonSequenceProducer(1.0, 0.25));
    }

    public AuctionAlgorithm(AuctionImplementation implementation, EpsilonSequenceProducer epsilonProducer) {
        this.implementation = implementation;
        this.epsilonProducer = epsilonProducer;
    }

    @Override
    public int[] findMaxCostAssignment(int[][] inputBenefitMatrix) {
        final BenefitMatrix benefitMatrix = new BenefitMatrix(inputBenefitMatrix);
        final int n = benefitMatrix.size();
        info("Solving problem for size: %d", n);

        final PriceVector priceVector = PriceVector.createInitialPriceVector(n);
        final List<Double> epsilonSequence = epsilonProducer.getEpsilonSequence(n)
                ;
        Assignment assignment = null;
        for(Double epsilon : epsilonSequence){
            assignment = implementation.epsilonScalingPhase(benefitMatrix, priceVector, epsilon);
        }

        assert assignment.isComplete();

        return assignment.getPersonAssignment();
    }
}
