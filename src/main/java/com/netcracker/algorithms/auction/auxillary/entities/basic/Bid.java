package com.netcracker.algorithms.auction.auxillary.entities.basic;

public class Bid implements Comparable<Bid> {

    private final Person person;
    private final Item item;
    private final double bidValue;

    public Bid(Person person, Item item, double bidValue) {
        this.person = person;
        this.item = item;
        this.bidValue = bidValue;
    }

    public Person getPerson() {
        return person;
    }

    public Item getItem() {
        return item;
    }

    public double getBidValue() {
        return bidValue;
    }

    @Override
    public int compareTo(Bid o) {
        return Double.compare(this.bidValue, o.bidValue);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Bid bid = (Bid) o;

        if (Double.compare(bid.getBidValue(), getBidValue()) != 0) return false;
        if (!getPerson().equals(bid.getPerson())) return false;
        return getItem().equals(bid.getItem());
    }

    @Override
    public int hashCode() {
        int result;
        long temp;
        result = getPerson().hashCode();
        result = 31 * result + getItem().hashCode();
        temp = Double.doubleToLongBits(getBidValue());
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        return result;
    }

    @Override
    public String toString() {
        return "Bid{" +
                "person=" + person +
                ", item=" + item +
                ", bidValue=" + bidValue +
                '}';
    }
}
