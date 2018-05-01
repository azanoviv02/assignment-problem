package com.netcracker.assignment.algorithms.auction.implementation.asynchronous;

import com.netcracker.assignment.algorithms.auction.auxillary.entities.aggregates.Assignment;
import com.netcracker.assignment.algorithms.auction.auxillary.entities.aggregates.BenefitMatrix;
import com.netcracker.assignment.algorithms.auction.auxillary.entities.aggregates.PersonQueue;
import com.netcracker.assignment.algorithms.auction.auxillary.entities.aggregates.PriceVector;
import com.netcracker.assignment.algorithms.auction.auxillary.entities.aggregates.item.ItemList;
import com.netcracker.assignment.algorithms.auction.auxillary.entities.basic.Item;
import com.netcracker.assignment.algorithms.auction.auxillary.entities.basic.Person;
import com.netcracker.assignment.algorithms.auction.implementation.AuctionImplementation;
import com.netcracker.assignment.utils.io.logging.Logger;
import com.netcracker.assignment.utils.io.logging.SystemOutLogger;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import static com.netcracker.assignment.algorithms.auction.auxillary.entities.aggregates.PersonQueue.createFullPersonQueue;
import static com.netcracker.assignment.utils.AssertionMaker.makeAssertion;
import static com.netcracker.assignment.utils.ConcurrentUtils.executeRunnableInParallel;


/**
 * Asynchronous Jacobi implementation of the auction algorithms for assignment problem.
 * At the beginning of epsilon scaling phase it launches specified number of threads,
 * each executing the same task
 * (see nested static class {@link AsynchronousJacobi.AsynchronousJacobiTask})
 * <p>
 * Uses one simple lock for the queue of unassigned persons and one read-write lock
 * for each item (read lock is taken during the searching phase, write lock is taken
 * during assignment and price update).
 * <p>
 * P.S.: by convention, all used classes and structures (i.e. Person, Item, Assigment,
 * ItemList, etc.) are NOT thread-safe. All synchronization happens above them, explicitly.
 */
@SuppressWarnings("All")
public class AsynchronousJacobi implements AuctionImplementation {

    private final int numberOfThreads;
    private final Logger logger = new SystemOutLogger(true);

    public AsynchronousJacobi(int numberOfThreads) {
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

        // Declaring new structures which are specific to this epsilon scaling phase.
        final ItemList itemList = ItemList.createFullItemList(n);
        final Assignment assignment = Assignment.createInitialAssignment(n);

        final PersonQueue nonAssignedPersonQueue = createFullPersonQueue(n);
        final Lock personQueueLock = new ReentrantLock();

        // All threads have the same task
        Runnable runnable = new AsynchronousJacobiTask(
                benefitMatrix,
                itemList,
                epsilon,
                priceVector,
                assignment,
                nonAssignedPersonQueue,
                personQueueLock
        );

        // Executing task in several threads simultaniously
        executeRunnableInParallel(runnable, numberOfThreads);

        // Makins several simple assertion at the end of current phase
        makeAssertion(assignment.isComplete());
        makeAssertion(nonAssignedPersonQueue.isEmpty());

        return assignment;
    }

    /**
     * Task, which is executed by every available thread.
     * <p>
     * While unnassigned persons queue is not empty, a thread selects one person,
     * finds the best item for him, computes a bid and updates price and assigment.
     * <p>
     * All of it happens asynchroniously, possibly using stale information about prices.
     * <p>
     * Scope braces (i.e. { }) are used when taking explicit locks, for improved readability.
     */
    private static class AsynchronousJacobiTask implements Runnable {

        /*
            Description for locking policy for each data structure is provided below.
        */

        /*
            Benefit matrix, iteml list and epsilon are immutable, so no locks are used.
        */
        private final BenefitMatrix benefitMatrix;
        private final ItemList itemList;
        private final double epsilon;

        /*
            Price vector and assignment for each item
            are read/updated using the read/write lock of said item.
         */
        private final PriceVector priceVector;
        private final Assignment assignment;

        /*
            Person queue uses its own explicit lock
         */
        private final PersonQueue nonAssignedPersonQueue;
        private final Lock personQueueLock;

        public AsynchronousJacobiTask(BenefitMatrix benefitMatrix,
                                      ItemList itemList,
                                      double epsilon,
                                      PriceVector priceVector,
                                      Assignment assignment,
                                      PersonQueue nonAssignedPersonQueue,
                                      Lock personQueueLock) {
            this.benefitMatrix = benefitMatrix;
            this.itemList = itemList;
            this.epsilon = epsilon;
            this.priceVector = priceVector;
            this.assignment = assignment;
            this.nonAssignedPersonQueue = nonAssignedPersonQueue;
            this.personQueueLock = personQueueLock;
        }

        @Override
        public void run() {
            // execute the loop until run out of peole
            while (true) {

                // select person
                Person person = null;
                personQueueLock.lock();
                {
                    person = nonAssignedPersonQueue.poll();
                }
                personQueueLock.unlock();
                if (person == null) {
                    break;
                }

                // find best item
                Item bestItem = null;
                double bestValue = -1.0;
                double secondBestValue = -1.0;
                for (Item item : itemList) {

                    int benefit;
                    double price;

                    // use read lock for reading info about particular item
                    Lock itemReadLock = item.getReadWriteLock().readLock();
                    itemReadLock.lock();
                    {
                        benefit = benefitMatrix.getBenefit(person, item);
                        price = priceVector.getPriceFor(item);
                    }
                    itemReadLock.unlock();

                    // calculate value and save it, if it the best so far
                    double value = benefit - price;
                    if (value > bestValue) {
                        bestItem = item;
                        bestValue = value;
                        secondBestValue = bestValue;
                    } else if (value > secondBestValue) {
                        secondBestValue = value;
                    }
                }

                // if found the best item, then make a bid for it and process it
                if (bestItem != null) {

                    final double bidValue = bestValue - secondBestValue + epsilon;

                    // using write lock to update information about item
                    Lock itemWriteLock = bestItem.getReadWriteLock().writeLock();
                    itemWriteLock.lock();
                    {
                        final Person oldOwner = assignment.getPersonForItem(bestItem);
                        if (oldOwner != Person.NO_PERSON) {
                            personQueueLock.lock();
                            {
                                nonAssignedPersonQueue.add(oldOwner);
                            }
                            personQueueLock.unlock();
                        }
                        priceVector.increasePrice(bestItem, bidValue);
                        assignment.setPersonForItem(bestItem, person);
                    }
                    itemWriteLock.unlock();
                }
            }
        }
    }
}
