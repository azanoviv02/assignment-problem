package com.netcracker.algorithms.auction.implementation.truesynchronous;

import com.netcracker.algorithms.auction.auxillary.entities.aggregates.*;
import com.netcracker.algorithms.auction.auxillary.entities.basic.Bid;
import com.netcracker.algorithms.auction.auxillary.entities.basic.Item;
import com.netcracker.algorithms.auction.auxillary.entities.basic.Person;
import com.netcracker.algorithms.auction.auxillary.entities.basic.SearchTaskResult;
import com.netcracker.algorithms.auction.implementation.AuctionImplementation;
import com.netcracker.utils.io.logging.Logger;
import com.netcracker.utils.io.logging.SystemOutLogger;
import com.netcracker.utils.io.logging.decorators.ThreadIdPrintingLogger;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.netcracker.algorithms.auction.auxillary.logic.bids.BidMaker.makeBid;
import static com.netcracker.algorithms.auction.auxillary.utils.ConcurrentUtils.await;
import static com.netcracker.algorithms.auction.auxillary.utils.ConcurrentUtils.createExecutorService;
import static com.netcracker.utils.AssertionMaker.makeAssertion;
import static com.netcracker.utils.ConcurrentUtils.awaitFurureListCompletion;
import static com.netcracker.utils.ConcurrentUtils.awaitFutureCompletion;
import static com.netcracker.utils.GeneralUtils.mapList;

@SuppressWarnings("All")
public class TrueSynchronousGaussSeidel implements AuctionImplementation {

    private final int numberOfThreads;
    private final Logger logger = new ThreadIdPrintingLogger(new SystemOutLogger(true));

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

        //bottleneck tasks, which are performed each iteration by one thread (the one which arrives last to the barrier)
        final Runnable personSelectingTask = createPersonSelectingTask(nonAssignedPersonQueue, currentPerson);

        final Runnable bidMakingAndProcessingTask = createBidMakingAndProcessingTask(
                priceVector,
                epsilon,
                nonAssignedPersonQueue,
                assignment,
                currentPerson,
                resultSet
        );

        //synchronozation barriers, each also associated with a task
        final CyclicBarrier startSearchBarrier = new CyclicBarrier(numberOfThreads, personSelectingTask);
        final CyclicBarrier finishSearchBarrier = new CyclicBarrier(numberOfThreads, bidMakingAndProcessingTask);

        final ExecutorService executorService = createExecutorService(numberOfThreads);

        List<Runnable> runnableList = new ArrayList<>(numberOfThreads);
        for (ItemList itemListPart : itemList.split(numberOfThreads)) {
            Runnable bidSearchTask = createBidSearchTask(
                    benefitMatrix,
                    priceVector,
                    nonAssignedPersonQueue,
                    itemListPart,
                    currentPerson,
                    resultSet,
                    startSearchBarrier,
                    finishSearchBarrier
            );
            runnableList.add(bidSearchTask);
        }

        List<Future> futureList = mapList(runnableList, executorService::submit);
        awaitFurureListCompletion(futureList);

        makeAssertion(assignment.isComplete());
        makeAssertion(nonAssignedPersonQueue.isEmpty());

        return assignment;
    }

    private Runnable createPersonSelectingTask(PersonQueue nonAssignedPersonQueue,
                                               AtomicReference<Person> currentPerson) {
        return () -> {
            logger.info("== Person Selecting Task ==");
            synchronized (nonAssignedPersonQueue) {
                makeAssertion(!nonAssignedPersonQueue.isEmpty());
                final Person selectedPerson = nonAssignedPersonQueue.remove();
                currentPerson.set(selectedPerson);
                logger.info("Person %s selected", selectedPerson);
            }
        };
    }

    private Runnable createBidMakingAndProcessingTask(PriceVector priceVector,
                                                      double epsilon,
                                                      PersonQueue nonAssignedPersonQueue,
                                                      Assignment assignment,
                                                      AtomicReference<Person> currentPerson,
                                                      Set<SearchTaskResult> resultSet) {
        return () -> {
            logger.info("== Bid Making Task ==");
            final SearchTaskResult finalResult = SearchTaskResult.mergeResults(resultSet);
            final Bid bid = makeBid(
                    currentPerson.get(),
                    finalResult,
                    epsilon
            );
            logger.info("Making bid: %s", bid);

            synchronized (assignment) {
                logger.info("Processing bid: %s", bid);
                final Person bidder = bid.getPerson();
                final Item item = bid.getItem();
                final double bidValue = bid.getBidValue();

                final Person oldOwner = assignment.getPersonForItem(item);
                if (oldOwner != Person.NO_PERSON) {
                    synchronized (nonAssignedPersonQueue) {
                        logger.info("Making old owner unaasigned: %s");
                        nonAssignedPersonQueue.add(oldOwner);
                    }
                }
                assignment.setPersonForItem(item, bidder);
                synchronized (item) {
                    logger.info("Increasing price for item %s by %s", item, bidValue);
                    priceVector.increasePrice(item, bidValue);
                }
            }
            logger.info("Price vector after bid proccessing: %s", priceVector);
        };
    }

    private Runnable createBidSearchTask(BenefitMatrix benefitMatrix,
                                         PriceVector priceVector,
                                         PersonQueue nonAssignedPersonQueue,
                                         ItemList itemList,
                                         AtomicReference<Person> currentPerson,
                                         Set<SearchTaskResult> resultSet,
                                         CyclicBarrier startSearchBarrier,
                                         CyclicBarrier finishSearchBarrier) {
        return () -> {
            while (!nonAssignedPersonQueue.isEmpty()) {
                await(startSearchBarrier);

                logger.info("== Bid Searching Task ==");
                logger.info("Searching the best item among items %s", itemList);
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
                logger.info("Search result: %s", result);
                resultSet.add(result);

                await(finishSearchBarrier);
            }
        };
    }
}
