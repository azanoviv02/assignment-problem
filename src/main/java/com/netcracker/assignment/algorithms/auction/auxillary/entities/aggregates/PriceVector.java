package com.netcracker.assignment.algorithms.auction.auxillary.entities.aggregates;

import com.netcracker.assignment.algorithms.auction.auxillary.entities.basic.Item;

import java.util.Arrays;

public class PriceVector {

    public final static double INITIAL_PRICE = 1.0;

    public final static PriceVector createInitialPriceVector(int n) {
        return new PriceVector(getFilledDoubleArray(n, INITIAL_PRICE));
    }


    private double priceArray[];

    private PriceVector(double[] priceArray) {
        this.priceArray = priceArray;
    }

    public double getPriceFor(Item item) {
        return priceArray[item.getItemIndex()];
    }

    public void increasePrice(Item item, double amount) {
        priceArray[item.getItemIndex()] += amount;
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
