package com.netcracker.algorithms.auction.implementation.asynchronous;

import com.netcracker.algorithms.auction.auxillary.entities.aggregates.*;
import com.netcracker.algorithms.auction.auxillary.entities.basic.Item;
import com.netcracker.algorithms.auction.auxillary.entities.basic.SearchTaskResult;
import com.netcracker.algorithms.auction.auxillary.utils.ConcurrentUtils;
import com.netcracker.algorithms.auction.implementation.AuctionImplementation;
import com.netcracker.utils.io.logging.Logger;
import com.netcracker.utils.io.logging.SystemOutLogger;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingDeque;

import static com.netcracker.algorithms.auction.auxillary.entities.aggregates.PersonQueue.createFullPersonQueue;
import static com.netcracker.algorithms.auction.auxillary.utils.ConcurrentUtils.await;

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

        final PersonQueue nonAssignedPersonQueue = createFullPersonQueue(
                n,
                new LinkedBlockingDeque<>(n)
        );
        final ItemList itemList = ItemList.createFullItemList(n);
        final Assignment assignment = Assignment.createInitialAssignment(n);

        ExecutorService executorService = ConcurrentUtils.createExecutorService(numberOfThreads);
        List<Runnable> runnableList = new ArrayList<>(numberOfThreads);
        for (ItemList itemListPart : itemList.split(numberOfThreads)) {
            Runnable runnable = () -> {
                while (!nonAssignedPersonQueue.isEmpty()) {
                    await(startSearchBarrier);
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
                await(finishSearchBarrier);
            };
            runnableList.add(runnable);
        }

        return assignment;
    }

}
