package com.netcracker.algorithms.auction.auxillary.entities.basic;

public class Item {

    public static final Item NO_ITEM = new Item(-1);

    private final int itemIndex;

    public Item(int itemIndex) {
        this.itemIndex = itemIndex;
    }

    public int getItemIndex() {
        return itemIndex;
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
