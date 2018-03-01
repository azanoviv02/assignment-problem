package com.netcracker.algorithms.auction.auxillary.entities;

public class Bid implements Comparable<Bid> {

    private final int bidderIndex;
    private final int objectIndex;
    private final double bidValue;

    public Bid(int bidderIndex, int objectIndex, double bidValue) {
        this.bidderIndex = bidderIndex;
        this.objectIndex = objectIndex;
        this.bidValue = bidValue;
    }

    public int getBidderIndex() {
        return bidderIndex;
    }

    public int getObjectIndex() {
        return objectIndex;
    }

    public double getBidValue() {
        return bidValue;
    }

    @Override
    public int compareTo(Bid o) {
        return Double.compare(this.bidValue, o.bidValue);
    }
}
