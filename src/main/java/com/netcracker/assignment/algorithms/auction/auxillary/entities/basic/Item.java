package com.netcracker.assignment.algorithms.auction.auxillary.entities.basic;

import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class Item {

    public static final Item NO_ITEM = new Item(-1);

    private final int itemIndex;

    //todo refactor into separate class
    private final ReadWriteLock readWriteLock;

    public Item(int itemIndex) {
        this.itemIndex = itemIndex;
        this.readWriteLock = new ReentrantReadWriteLock();
    }

    public int getItemIndex() {
        return itemIndex;
    }

    public ReadWriteLock getReadWriteLock() {
        return readWriteLock;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Item item = (Item) o;

        return getItemIndex() == item.getItemIndex();
    }

    @Override
    public int hashCode() {
        return getItemIndex();
    }

    @Override
    public String toString() {
        return Integer.toString(itemIndex);
    }
}
