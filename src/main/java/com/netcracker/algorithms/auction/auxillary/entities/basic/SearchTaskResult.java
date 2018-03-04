package com.netcracker.algorithms.auction.auxillary.entities.basic;

import java.util.List;

public class SearchTaskResult {

    private final Item bestItem;
    private final double bestValue;
    private final double secondBestValue;

    public SearchTaskResult(Item bestItem, double bestValue, double secondBestValue) {
        this.bestItem = bestItem;
        this.bestValue = bestValue;
        this.secondBestValue = secondBestValue;
    }

    public Item getBestItem() {
        return bestItem;
    }

    public double getBestValue() {
        return bestValue;
    }

    public double getSecondBestValue() {
        return secondBestValue;
    }

    @Override
    public String toString() {
        return "SearchTaskResult{" +
                "bestItem=" + bestItem +
                ", bestValue=" + bestValue +
                ", secondBestValue=" + secondBestValue +
                '}';
    }

    public static SearchTaskResult mergeResults(List<SearchTaskResult> resultList) {
        if (resultList.isEmpty()) {
            throw new IllegalStateException("searchTaskResultList is empty");
        }

        SearchTaskResult firstResult = resultList.iterator().next();
        Item currentBestItem = firstResult.getBestItem();
        double currentBestValue = firstResult.getBestValue();
        double currentSecondBestValue = firstResult.getSecondBestValue();

        for (SearchTaskResult result : resultList) {
            if (result.getBestValue() > currentBestValue) {
                currentBestItem = result.getBestItem();
                currentBestValue = result.getBestValue();
                currentSecondBestValue = currentBestValue;
            } else if (result.getBestValue() > currentSecondBestValue) {
                currentSecondBestValue = result.getBestValue();
            }
        }

        return new SearchTaskResult(currentBestItem, currentBestValue, currentSecondBestValue);
    }
}
