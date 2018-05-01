package com.netcracker.assignment.algorithms.auction.implementation.synchronous;

import com.netcracker.assignment.algorithms.auction.auxillary.entities.aggregates.BenefitMatrix;
import com.netcracker.assignment.algorithms.auction.auxillary.entities.aggregates.item.ItemList;
import com.netcracker.assignment.algorithms.auction.auxillary.entities.aggregates.PersonQueue;
import com.netcracker.assignment.algorithms.auction.auxillary.entities.aggregates.PriceVector;
import com.netcracker.assignment.algorithms.auction.auxillary.entities.basic.Bid;
import com.netcracker.assignment.algorithms.auction.auxillary.entities.basic.Person;
import com.netcracker.assignment.algorithms.auction.auxillary.entities.tasks.ParallelBidTask;
import com.netcracker.assignment.algorithms.auction.implementation.AuctionImplementation;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutorService;

/**
 * Provides parallel execution withing the bid.
 * Several threads can be used to search for the best item for one person.
 */
public class SynchronousGaussSeidel extends AbstractSynchronousAuctionImplementation implements AuctionImplementation {

    private final int numberOfSearchTasksPerPerson;

    public SynchronousGaussSeidel(int numberOfThreads, int numberOfSearchTasksPerPerson) {
        super(numberOfThreads);
        this.numberOfSearchTasksPerPerson = numberOfSearchTasksPerPerson;
    }

    public int getNumberOfSearchTasksPerPerson() {
        return numberOfSearchTasksPerPerson;
    }

    @Override
    public List<Bid> makeBids(BenefitMatrix benefitMatrix,
                              PriceVector priceVector,
                              double epsilon,
                              PersonQueue nonAssignedPersonQueue,
                              ItemList itemList,
                              ExecutorService executorService) {
        final List<Bid> bidList = new LinkedList<>();
        while (!nonAssignedPersonQueue.isEmpty()) {
            Person person = nonAssignedPersonQueue.remove();
            ParallelBidTask parallelBidTask = new ParallelBidTask(
                    benefitMatrix,
                    priceVector,
                    person,
                    itemList,
                    epsilon,
                    executorService,
                    numberOfSearchTasksPerPerson
            );
            bidList.addAll(parallelBidTask.call());
        }
        return bidList;
    }
}
