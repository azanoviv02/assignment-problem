package com.netcracker.algorithms.auction.auxillary.logic.bids;

import com.netcracker.algorithms.auction.auxillary.entities.aggregates.Assignment;
import com.netcracker.algorithms.auction.auxillary.entities.aggregates.ItemList;
import com.netcracker.algorithms.auction.auxillary.entities.aggregates.PersonQueue;
import com.netcracker.algorithms.auction.auxillary.entities.aggregates.PriceVector;
import com.netcracker.algorithms.auction.auxillary.entities.basic.Bid;
import com.netcracker.algorithms.auction.auxillary.entities.basic.Item;
import com.netcracker.algorithms.auction.auxillary.entities.basic.Person;

import java.util.Map;
import java.util.Queue;

import static com.netcracker.utils.io.logging.StaticLoggerHolder.info;

public class BidProcessor {

    public static void processBidsAndUpdateAssignmentForItemList(Assignment assignment,
                                                                 PriceVector priceVector,
                                                                 PersonQueue nonAssignedPersonQueue,
                                                                 ItemList itemList,
                                                                 Map<Item, Queue<Bid>> bidMap) {
        for (Item item : itemList) {
            final Queue<Bid> bidQueue = bidMap.get(item);
            if (bidQueue != null && !bidQueue.isEmpty()) {
                processBidsAndUpdateAssignmentForItem(
                        assignment,
                        priceVector,
                        nonAssignedPersonQueue,
                        item,
                        bidQueue
                );
            }
        }
        assert nonAssignedPersonQueue.containsDuplicates();
    }

    public static void processBidsAndUpdateAssignmentForItem(Assignment assignment,
                                                             PriceVector priceVector,
                                                             PersonQueue nonAssignedPersonQueue,
                                                             Item item,
                                                             Queue<Bid> bidQueue) {
        info("  Bidding for item: %s", item);

        final Person oldOwner = assignment.getPersonForItem(item);
        info("    Old owner: %s", oldOwner);
        if (oldOwner != Person.NO_PERSON) {
            nonAssignedPersonQueue.add(oldOwner);
        }

        info("    Bids: %s", bidQueue);
        final Bid highestBid = bidQueue.remove();
        info("    Highest bid: %s", highestBid);
        final Person highestBidder = highestBid.getPerson();
        final double highestBidValue = highestBid.getBidValue();

        assignment.setPersonForItem(item, highestBidder);
        priceVector.increasePrice(item, highestBidValue);

        info("    Failed bids: %s", bidQueue);
        for (Bid failedBid : bidQueue) {
            nonAssignedPersonQueue.add(failedBid.getPerson());
        }
    }
}
