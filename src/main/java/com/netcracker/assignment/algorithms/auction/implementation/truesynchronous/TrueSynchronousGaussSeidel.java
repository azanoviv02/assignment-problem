package com.netcracker.assignment.algorithms.auction.implementation.truesynchronous;

import com.netcracker.assignment.algorithms.auction.auxillary.entities.aggregates.Assignment;
import com.netcracker.assignment.algorithms.auction.auxillary.entities.aggregates.BenefitMatrix;
import com.netcracker.assignment.algorithms.auction.auxillary.entities.aggregates.PersonQueue;
import com.netcracker.assignment.algorithms.auction.auxillary.entities.aggregates.PriceVector;
import com.netcracker.assignment.algorithms.auction.auxillary.entities.aggregates.item.ItemList;
import com.netcracker.assignment.algorithms.auction.auxillary.entities.basic.Bid;
import com.netcracker.assignment.algorithms.auction.auxillary.entities.basic.Item;
import com.netcracker.assignment.algorithms.auction.auxillary.entities.basic.Person;
import com.netcracker.assignment.algorithms.auction.auxillary.entities.basic.SearchTaskResult;
import com.netcracker.assignment.algorithms.auction.implementation.AuctionImplementation;
import com.netcracker.assignment.utils.ConcurrentUtils;
import com.netcracker.assignment.utils.io.logging.Logger;
import com.netcracker.assignment.utils.io.logging.SystemOutLogger;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicReference;

import static com.netcracker.assignment.algorithms.auction.auxillary.logic.bids.BidMaker.makeBid;
import static com.netcracker.assignment.utils.AssertionMaker.makeAssertion;

/**
 * Failed attempt to create synchronous implementation of the auction algorithm
 * for assignment problem, which would have had separate threads, which were
 * synchronized only at the beginning and end of the iteration.
 */
@SuppressWarnings("All")
public class TrueSynchronousGaussSeidel implements AuctionImplementation {

    private final int numberOfThreads;
    private final Logger logger = new SystemOutLogger(true);

    public TrueSynchronousGaussSeidel(int numberOfThreads) {
        this.numberOfThreads = numberOfThreads;
    }

    public int getNumberOfThreads() {
        return numberOfThreads;
    }

    @Override
    public Assignment epsilonScalingPhase(BenefitMatrix benefitMatrix,
                                          PriceVector priceVector,
                                          double epsilon) {
        final int n = benefitMatrix.size();

        final PersonQueue nonAssignedPersonQueue = PersonQueue.createFullPersonQueue(n);
        final ItemList itemList = ItemList.createFullItemList(n);
        final Assignment assignment = Assignment.createInitialAssignment(n);

        //shared state, accessed simultaniously by several threads
        final AtomicReference<Person> currentPerson = new AtomicReference<>();
        final Set<SearchTaskResult> resultSet = Collections.newSetFromMap(new ConcurrentHashMap<>());

        //bottleneck tasks
        final Runnable personSelectingTask = () -> {
//            logger.info("Selecting new person");
            synchronized (nonAssignedPersonQueue) {
                makeAssertion(!nonAssignedPersonQueue.isEmpty());
                final Person nextPerson = nonAssignedPersonQueue.remove();
                currentPerson.set(nextPerson);
//                logger.info("Person %s selected", nextPerson);
            }
        };
        final Runnable bidMakingAndProcessingTask = () -> {
            final SearchTaskResult finalResult = SearchTaskResult.mergeResults(resultSet);
            final Bid bid = makeBid(
                    currentPerson.get(),
                    finalResult,
                    epsilon
            );
            synchronized (assignment) {
                final Person bidder = bid.getPerson();
                final Item item = bid.getItem();
                final double bidValue = bid.getBidValue();

                final Person oldOwner = assignment.getPersonForItem(item);
                if (oldOwner != Person.NO_PERSON) {
                    synchronized (nonAssignedPersonQueue) {
                        nonAssignedPersonQueue.add(oldOwner);
                    }
                }

                assignment.setPersonForItem(item, bidder);
                synchronized (item) {
                    priceVector.increasePrice(item, bidValue);
                }
            }
            logger.info("Current price vector: %s", priceVector);
        };

        //synchronozation barriers, each also associated with a task
        final CyclicBarrier startSearchBarrier = new CyclicBarrier(numberOfThreads, personSelectingTask);
        final CyclicBarrier finishSearchBarrier = new CyclicBarrier(numberOfThreads, bidMakingAndProcessingTask);

        final ExecutorService executorService = ConcurrentUtils.createExecutorService(numberOfThreads);

        List<Runnable> runnableList = new ArrayList<>(numberOfThreads);
        for (ItemList itemListPart : itemList.split(numberOfThreads)) {
            Runnable runnable = () -> {
                while (!nonAssignedPersonQueue.isEmpty()) {
                    ConcurrentUtils.await(startSearchBarrier);
                    Item currentBestItem = null;
                    double currentBestValue = -1.0;
                    double currentSecondBestValue = -1.0;
                    for (Item item : itemList) {
                        synchronized (item) {
                            int benefit = benefitMatrix.getBenefit(currentPerson.get(), item);
                            double price = priceVector.getPriceFor(item);
                            double value = benefit - price;
                            if (value > currentBestValue) {
                                currentBestItem = item;
                                currentBestValue = value;
                                currentSecondBestValue = currentBestValue;
                            } else if (value > currentSecondBestValue) {
                                currentSecondBestValue = value;
                            }
                        }
                    }
                    SearchTaskResult result = new SearchTaskResult(currentBestItem, currentBestValue, currentSecondBestValue);
                    resultSet.add(result);
                }
                ConcurrentUtils.await(finishSearchBarrier);
            };
            runnableList.add(runnable);
        }

        ConcurrentUtils.executeRunnableList(executorService, runnableList);

        makeAssertion(assignment.isComplete());
        makeAssertion(nonAssignedPersonQueue.isEmpty());

        return assignment;
    }

}
