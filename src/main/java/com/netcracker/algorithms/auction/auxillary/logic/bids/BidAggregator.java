package com.netcracker.algorithms.auction.auxillary.logic.bids;

import com.netcracker.algorithms.auction.auxillary.entities.basic.Bid;
import com.netcracker.algorithms.auction.auxillary.entities.basic.Item;

import java.util.*;

public class BidAggregator {

    /**
     * Aggregates unorganized collection of bids into map of max-priority queues, which contain
     * bids for each item.
     *
     * @param bidCollection
     * @return
     */
    public static Map<Item, Queue<Bid>> aggregateBids(Collection<Bid> bidCollection) {
        Map<Item, Queue<Bid>> bidMap = new HashMap<>();
        for (Bid bid : bidCollection) {
            final Item item = bid.getItem();
            Queue<Bid> bidQueue = bidMap.get(item);
            if (bidQueue == null) {
                bidQueue = new PriorityQueue<>(Collections.reverseOrder());
                bidMap.put(item, bidQueue);
            }
            bidQueue.add(bid);
        }
        return bidMap;
    }
}
