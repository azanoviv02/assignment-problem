package com.netcracker.assignment.algorithms.auction.auxillary.entities.aggregates.item;

import com.netcracker.assignment.algorithms.auction.auxillary.entities.basic.Item;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class ConcurrentItemList implements Iterable<Item> {

    public static ConcurrentItemList createFullItemList(int numberOfItems) {
        return new ConcurrentItemList(getListOfRange(numberOfItems));
    }

    private final List<Item> itemList;

    private ConcurrentItemList(List<Item> itemList) {
        this.itemList = itemList;
    }

    public List<ConcurrentItemList> split(int numberOfSublists) {
        final int numberOfItems = itemList.size();
        final int sublistSize = numberOfItems / numberOfSublists;
        final List<ConcurrentItemList> sublistList = new ArrayList<>(numberOfSublists);
        for (int i = 0; i < numberOfSublists - 1; i++) {
            int fromIndex = i * sublistSize;
            int toIndex = fromIndex + sublistSize;
            sublistList.add(getSublist(fromIndex, toIndex));
        }
        final int lastSublistStartIndex = sublistSize * (numberOfSublists - 1);
        sublistList.add(getSublist(lastSublistStartIndex, numberOfItems));
        return sublistList;
    }

    public ConcurrentItemList getSublist(int fromIndex, int toIndex) {
        return new ConcurrentItemList(itemList.subList(fromIndex, toIndex));
    }

    @Override
    public Iterator<Item> iterator() {
        return itemList.iterator();
    }

    @Override
    public String toString() {
        return itemList.toString();
    }

    private static List<Item> getListOfRange(int toExclusive) {
        List<Item> rangeList = new ArrayList<>(toExclusive);
        for (int i = 0; i < toExclusive; i++) {
            rangeList.add(new Item(i));
        }
        return rangeList;
    }
}
