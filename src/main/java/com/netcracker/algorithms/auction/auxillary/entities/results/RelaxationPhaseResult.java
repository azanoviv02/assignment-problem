package com.netcracker.algorithms.auction.auxillary.entities.results;

import com.netcracker.algorithms.auction.auxillary.entities.aggregates.Assignment;
import com.netcracker.algorithms.auction.auxillary.entities.aggregates.PriceVector;

// todo make immutable
public class RelaxationPhaseResult {
    final Assignment assignment;
    final PriceVector priceVector;

    public RelaxationPhaseResult(Assignment assignment, PriceVector priceVector) {
        this.assignment = assignment;
        this.priceVector = priceVector;
    }

    public Assignment getAssignment() {
        return assignment;
    }

    public PriceVector getPriceVector() {
        return priceVector;
    }
}
