package com.netcracker.algorithms.auction.auxillary.entities.aggregates;

import org.junit.Test;

import java.util.List;

public class ItemListTest {

    @Test
    public void split() throws Exception {
        ItemList initialItemList = ItemList.createFullItemList(22);
        List<ItemList> partedList = initialItemList.split(5);
        for(ItemList part : partedList){
            System.out.println(part);
        }
    }
}