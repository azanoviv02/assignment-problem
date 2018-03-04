package com.netcracker.algorithms.auction.implementation.synchronous;

import com.netcracker.algorithms.auction.auxillary.entities.aggregates.BenefitMatrix;
import com.netcracker.algorithms.auction.auxillary.entities.aggregates.ItemList;
import com.netcracker.algorithms.auction.auxillary.entities.aggregates.PersonQueue;
import com.netcracker.algorithms.auction.auxillary.entities.aggregates.PriceVector;
import com.netcracker.algorithms.auction.auxillary.entities.basic.Bid;
import com.netcracker.algorithms.auction.auxillary.entities.basic.Person;
import com.netcracker.algorithms.auction.auxillary.entities.tasks.BidTask;
import com.netcracker.algorithms.auction.implementation.AuctionImplementation;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutorService;

import static com.netcracker.algorithms.auction.auxillary.utils.GeneralUtils.flatten;
import static com.netcracker.algorithms.auction.auxillary.utils.ConcurrentUtils.executeCallableList;

@SuppressWarnings("ALL")
public class SynchronousJacobi extends AbstractSynchronousAuctionImplementation implements AuctionImplementation {

    private final int numberOfPersonsPerBidTasks;

    public SynchronousJacobi(int numberOfThreads, int numberOfPersonsPerBidTasks) {
        super(numberOfThreads);
        this.numberOfPersonsPerBidTasks = numberOfPersonsPerBidTasks;
    }

    public int getNumberOfPersonsPerBidTasks() {
        return numberOfPersonsPerBidTasks;
    }

    @Override
    public List<Bid> makeBids(BenefitMatrix benefitMatrix,
                              PriceVector priceVector,
                              double epsilon,
                              PersonQueue nonAssignedPersonQueue,
                              ItemList itemList,
                              ExecutorService executorService) {
        final List<BidTask> bidTaskList = new LinkedList<>();
        while (!nonAssignedPersonQueue.isEmpty()) {
            List<Person> personList = removePersonsFromQueue(
                    nonAssignedPersonQueue,
                    numberOfPersonsPerBidTasks
            );
            BidTask bidTask = new BidTask(
                    benefitMatrix,
                    priceVector,
                    personList,
                    itemList,
                    epsilon
            );
            bidTaskList.add(bidTask);
        }
        return flatten(executeCallableList(bidTaskList, executorService));
    }

    public static List<Person> removePersonsFromQueue(PersonQueue personQueue,
                                                      int numberOfPersonsToRemove){
        final List<Person> removedPersonList = new ArrayList<>(numberOfPersonsToRemove);
        int alreadyRemoved = 0;
        while(alreadyRemoved < numberOfPersonsToRemove && !personQueue.isEmpty()){
            final Person person = personQueue.remove();
            removedPersonList.add(person);
        }
        return removedPersonList;
    }
}
