package com.netcracker.algorithms.auction.auxillary.entities;

import java.util.Arrays;

public class PriceVector {
    private double prices[];

    public PriceVector(double[] prices) {
        this.prices = prices;
    }

    public double[] getPrices() {
        return prices;
    }

    @Override
    public String toString() {
        return Arrays.toString(prices);
    }
}
