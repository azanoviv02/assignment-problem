package com.netcracker.algorithms.auction.auxillary.entities;

// todo think if it is really needed
public class RelaxationPhaseResult {
    final int[] assignment;
    final double[] prices;

    public RelaxationPhaseResult(int[] assignment, double[] prices) {
        this.assignment = assignment;
        this.prices = prices;
    }

    public int[] getAssignment() {
        return assignment;
    }

    public double[] getPrices() {
        return prices;
    }
}
