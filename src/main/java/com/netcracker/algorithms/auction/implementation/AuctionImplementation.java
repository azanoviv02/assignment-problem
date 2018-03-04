package com.netcracker.algorithms.auction.implementation;

import com.netcracker.algorithms.auction.auxillary.entities.aggregates.Assignment;
import com.netcracker.algorithms.auction.auxillary.entities.aggregates.BenefitMatrix;
import com.netcracker.algorithms.auction.auxillary.entities.aggregates.PriceVector;

public interface AuctionImplementation {

    /**
     *
     * @param benefitMatrix
     * @param priceVector mutable PriceVector object, contains updated prices at the end
     * @param epsilon
     * @return new assigment
     */
    //todo find correct name for this method
    Assignment relaxationPhase(BenefitMatrix benefitMatrix,
                               PriceVector priceVector,
                               double epsilon);
}
