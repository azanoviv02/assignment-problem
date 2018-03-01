package com.netcracker.algorithms.auction.auxillary.entities;

// todo think if it is really needed
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
