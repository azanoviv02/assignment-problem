package com.netcracker.algorithms.auction.auxillary.entities;

import java.util.Arrays;

public class PriceVector {

    public final static double INITIAL_PRICE = 1.0;

    public final static PriceVector getInitialPriceVector(int n) {
        return new PriceVector(getFilledDoubleArray(n, INITIAL_PRICE));
    }


    private double priceArray[];

    private PriceVector(double[] priceArray) {
        this.priceArray = priceArray;
    }

    public double getPriceFor(int index) {
        return priceArray[index];
    }

    public void increasePrice(int index, double amount) {
        priceArray[index] += amount;
    }

    @Override
    public String toString() {
        return Arrays.toString(priceArray);
    }


    private static double[] getFilledDoubleArray(int n, double value) {
        double[] array = new double[n];
        Arrays.fill(array, value);
        return array;
    }
}
