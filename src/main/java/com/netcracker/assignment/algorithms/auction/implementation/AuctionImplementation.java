package com.netcracker.assignment.algorithms.auction.implementation;

import com.netcracker.assignment.algorithms.auction.auxillary.entities.aggregates.Assignment;
import com.netcracker.assignment.algorithms.auction.auxillary.entities.aggregates.BenefitMatrix;
import com.netcracker.assignment.algorithms.auction.auxillary.entities.aggregates.PriceVector;

public interface AuctionImplementation {

    /**
     * Distinct phase within the auction algorithm, which always produces complete
     * if not optimal assignment.
     *
     * This phase is going to be present in EVERY auction algorithm implementation, both
     * synchronous and asynchronous.
     *
     * I separated it into distinct method in order to clearly define input and output.
     *
     * @param benefitMatrix immutable
     * @param priceVector mutable, contains updated prices at the end
     * @param epsilon
     * @return new COMPLETE assigment
     */
    Assignment epsilonScalingPhase(BenefitMatrix benefitMatrix,
                                   PriceVector priceVector,
                                   double epsilon);
}
