package com.netcracker.algorithms.auction.auxillary.entities.aggregates;

import com.netcracker.algorithms.auction.auxillary.entities.basic.Item;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class ItemList implements Iterable<Item> {

    public static ItemList createItemList(int n){
        return new ItemList(getListOfRange(n));
    }

    private final List<Item> itemList;

    private ItemList(List<Item> itemList) {
        this.itemList = itemList;
    }

    @Override
    public Iterator<Item> iterator() {
        return itemList.iterator();
    }

    private static List<Item> getListOfRange(int toExclusive) {
        List<Item> rangeList = new ArrayList<>(toExclusive);
        for (int i = 0; i < toExclusive; i++) {
            rangeList.add(new Item(i));
        }
        return rangeList;
    }
}
