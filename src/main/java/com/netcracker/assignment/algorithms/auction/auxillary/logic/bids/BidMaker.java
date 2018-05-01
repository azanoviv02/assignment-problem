package com.netcracker.assignment.algorithms.auction.auxillary.logic.bids;

import com.netcracker.assignment.algorithms.auction.auxillary.entities.basic.Bid;
import com.netcracker.assignment.algorithms.auction.auxillary.entities.basic.Item;
import com.netcracker.assignment.algorithms.auction.auxillary.entities.basic.Person;
import com.netcracker.assignment.algorithms.auction.auxillary.entities.basic.SearchTaskResult;

public class BidMaker {

    public static Bid makeBid(Person person,
                              SearchTaskResult result,
                              double epsilon) {
        final Item bestItem = result.getBestItem();
        final double bestValue = result.getBestValue();
        final double secondBestValue = result.getSecondBestValue();

        final double bidValue = bestValue - secondBestValue + epsilon;
        return new Bid(person, bestItem, bidValue);
    }
}
