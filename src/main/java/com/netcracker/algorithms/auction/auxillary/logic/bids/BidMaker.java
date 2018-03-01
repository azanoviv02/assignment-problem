package com.netcracker.algorithms.auction.auxillary.logic.bids;

import com.netcracker.algorithms.auction.auxillary.entities.aggregates.BenefitMatrix;
import com.netcracker.algorithms.auction.auxillary.entities.aggregates.ItemList;
import com.netcracker.algorithms.auction.auxillary.entities.aggregates.PersonQueue;
import com.netcracker.algorithms.auction.auxillary.entities.aggregates.PriceVector;
import com.netcracker.algorithms.auction.auxillary.entities.basic.Bid;
import com.netcracker.algorithms.auction.auxillary.entities.basic.Item;
import com.netcracker.algorithms.auction.auxillary.entities.basic.Person;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;

public class BidMaker {

    private final static double STARTING_VALUE = -Double.MAX_VALUE;

    public static Bid makeBid(Person person,
                              ItemList itemList,
                              BenefitMatrix benefitMatrix,
                              PriceVector priceVector,
                              double epsilon) {
        double bestValue = STARTING_VALUE;
        double secondBestValue = STARTING_VALUE;
        Item bestItem = Item.NO_ITEM;

        for (Item item : itemList) {
            int benefit = benefitMatrix.getBenefit(person, item);
            double price = priceVector.getPriceFor(item);
            double value = benefit - price;
            if (value > bestValue) {
                secondBestValue = bestValue;
                bestValue = value;
                bestItem = item;
            } else if (value > secondBestValue) {
                secondBestValue = value;
            }
        }
        double bidValue = bestValue - secondBestValue + epsilon;
        return new Bid(person, bestItem, bidValue);
    }

    public static List<Callable<Bid>> createCallableList(PersonQueue nonAssignedPersonQueue,
                                                         ItemList itemList,
                                                         BenefitMatrix benefitMatrix,
                                                         PriceVector priceVector,
                                                         double epsilon) {
        return nonAssignedPersonQueue
                .stream()
                .map((person) -> createCallableForCreatingBid(
                        person,
                        itemList,
                        benefitMatrix,
                        priceVector,
                        epsilon
                ))
                .collect(Collectors.toList());
    }

    public static Callable<Bid> createCallableForCreatingBid(Person person,
                                                             ItemList itemList,
                                                             BenefitMatrix benefitMatrix,
                                                             PriceVector priceVector,
                                                             double epsilon) {
        return () -> makeBid(
                person,
                itemList,
                benefitMatrix,
                priceVector,
                epsilon
        );
    }
}
