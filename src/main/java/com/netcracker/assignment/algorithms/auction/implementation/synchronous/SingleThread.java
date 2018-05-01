package com.netcracker.assignment.algorithms.auction.implementation.synchronous;

import com.netcracker.assignment.algorithms.auction.auxillary.entities.aggregates.BenefitMatrix;
import com.netcracker.assignment.algorithms.auction.auxillary.entities.aggregates.item.ItemList;
import com.netcracker.assignment.algorithms.auction.auxillary.entities.aggregates.PersonQueue;
import com.netcracker.assignment.algorithms.auction.auxillary.entities.aggregates.PriceVector;
import com.netcracker.assignment.algorithms.auction.auxillary.entities.basic.Bid;
import com.netcracker.assignment.algorithms.auction.auxillary.entities.basic.Person;
import com.netcracker.assignment.algorithms.auction.auxillary.entities.tasks.BidTask;

import java.util.List;
import java.util.concurrent.ExecutorService;

/**
 * Implementation of the plain single thread auction algorithm.
 * I have added it to highlight the difference between single- and multithreaded versions.
 *
 * Here we basically create only one regular bid task, which encompasses all people and
 * creates only one search task per person.
 *
 * Executor service is provided by parent class through method parameters but it is never used.
 */
public class SingleThread extends AbstractSynchronousAuctionImplementation {

    public SingleThread() {
        super(1);
    }

    @Override
    public List<Bid> makeBids(BenefitMatrix benefitMatrix,
                              PriceVector priceVector,
                              double epsilon,
                              PersonQueue nonAssignedPersonQueue,
                              ItemList itemList,
                              ExecutorService executorService) {
        List<Person> personList = nonAssignedPersonQueue.removeAll();
        BidTask bidTask = new BidTask(
                benefitMatrix,
                priceVector,
                personList,
                itemList,
                epsilon
        );
        return bidTask.call();
    }
}
