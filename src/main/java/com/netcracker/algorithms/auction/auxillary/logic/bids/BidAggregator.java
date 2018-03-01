package com.netcracker.algorithms.auction.auxillary.logic.bids;

import com.netcracker.algorithms.auction.auxillary.entities.aggregates.ItemList;
import com.netcracker.algorithms.auction.auxillary.entities.basic.Bid;
import com.netcracker.algorithms.auction.auxillary.entities.basic.Item;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Queue;

public class BidAggregator {

    public static Map<Item, Queue<Bid>> aggregateBids(Collection<Bid> bidSet, ItemList itemList) {
        Map<Item, Queue<Bid>> bidMap = new HashMap<>();
        for (Item item : itemList) {
            bidMap.put(item, new PriorityQueue<>(Collections.reverseOrder()));
        }
        for (Bid bid : bidSet) {
            final Item item = bid.getItem();
            final Queue<Bid> bidQueue = bidMap.get(item);
            bidQueue.add(bid);
        }
        return bidMap;
    }
}
