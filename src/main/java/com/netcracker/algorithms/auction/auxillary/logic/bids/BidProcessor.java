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

public class BidProcessor {

    public static PersonQueue processBidsAndUpdateAssignmentForItemList(Assignment assignment,
                                                                        PriceVector priceVector,
                                                                        ItemList itemList,
                                                                        Map<Item, Queue<Bid>> bidMap) {
        PersonQueue nonAssignedPersonQueue = PersonQueue.createEmptyPersonQueue();
        System.out.println("New round");
        for (Item item : itemList) {
            final Queue<Bid> bidQueue = bidMap.get(item);
            if (!bidQueue.isEmpty()) {
                nonAssignedPersonQueue.addAll(processBidsAndUpdateAssignmentForItem(assignment, priceVector, item, bidQueue));
            }
        }
        if (nonAssignedPersonQueue.containsDuplicates()) {
            throw new IllegalStateException("Queue contains duplicates: " + nonAssignedPersonQueue);
        }
        return nonAssignedPersonQueue;
    }

    // todo owner and failed bidders both must be added to personQueue
    // todo handle case when person lost one item and failed bid on another
    public static PersonQueue processBidsAndUpdateAssignmentForItem(Assignment assignment,
                                                                    PriceVector priceVector,
                                                                    Item item,
                                                                    Queue<Bid> bidQueue) {
        System.out.println("  Bidding for item: " + item);
        System.out.println("  Bidders: "+bidQueue);
        final PersonQueue nonAssignedPersonQueue = PersonQueue.createEmptyPersonQueue();
        final Person oldOwner = assignment.getPersonForItem(item);
        if (oldOwner != Person.NO_PERSON) {
            nonAssignedPersonQueue.add(oldOwner);
        }
        final Bid highestBid = bidQueue.remove();
        final Person highestBidder = highestBid.getPerson();
        System.out.println("  Highest bidder: "+highestBidder);
        assignment.setPersonForItem(item, highestBidder);
        final double highestBidValue = highestBid.getBidValue();
        priceVector.increasePrice(item, highestBidValue);
        for (Bid failedBid : bidQueue) {
            System.out.println("  Adding failed bidder: "+failedBid);
            nonAssignedPersonQueue.add(failedBid.getPerson());
        }
        if (nonAssignedPersonQueue.containsDuplicates()) {
            throw new IllegalStateException("Queue contains duplicates: " + nonAssignedPersonQueue);
        }
        System.out.println("  == Bidding for item "+item+" is over, failed bidders: "+nonAssignedPersonQueue);
        return nonAssignedPersonQueue;
    }
}
