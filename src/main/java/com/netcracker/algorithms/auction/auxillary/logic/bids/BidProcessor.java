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
        info("New round");
        for (Item item : itemList) {
            final Queue<Bid> bidQueue = bidMap.get(item);
            if (!bidQueue.isEmpty()) {
                nonAssignedPersonQueue.addAll(processBidsAndUpdateAssignmentForItem(assignment, priceVector, item, bidQueue));
            }
        }
        if (nonAssignedPersonQueue.containsDuplicates()) {
            throw new IllegalStateException("Queue contains duplicates");
        }
    }

    public static PersonQueue processBidsAndUpdateAssignmentForItem(Assignment assignment,
                                                                    PriceVector priceVector,
                                                                    Item item,
                                                                    Queue<Bid> bidQueue) {
        info("  Bidding for item: %s", item);
        info("  Bidders: "+bidQueue);
        final PersonQueue nonAssignedPersonQueue = PersonQueue.createEmptyPersonQueue();
        final Person oldOwner = assignment.getPersonForItem(item);
        info("  Old owner: "+oldOwner);
        if (oldOwner != Person.NO_PERSON) {
            nonAssignedPersonQueue.add(oldOwner);
        }
        final Bid highestBid = bidQueue.remove();
        final Person highestBidder = highestBid.getPerson();
        info("  Highest bidder: "+highestBidder);
        assignment.setPersonForItem(item, highestBidder);
        final double highestBidValue = highestBid.getBidValue();
        priceVector.increasePrice(item, highestBidValue);
        for (Bid failedBid : bidQueue) {
            info("  Adding failed bidder: "+failedBid);
            nonAssignedPersonQueue.add(failedBid.getPerson());
        }
        if (nonAssignedPersonQueue.containsDuplicates()) {
            throw new IllegalStateException("Queue contains duplicates");
        }
        info("  == Bidding for item %s is over, failed bidders %s", item, nonAssignedPersonQueue);
        return nonAssignedPersonQueue;
    }
}
