package com.netcracker.algorithms.auction.auxillary.entities.tasks;

import com.netcracker.algorithms.auction.auxillary.entities.aggregates.BenefitMatrix;
import com.netcracker.algorithms.auction.auxillary.entities.aggregates.ItemList;
import com.netcracker.algorithms.auction.auxillary.entities.aggregates.PriceVector;
import com.netcracker.algorithms.auction.auxillary.entities.basic.Item;
import com.netcracker.algorithms.auction.auxillary.entities.basic.Person;
import com.netcracker.algorithms.auction.auxillary.entities.basic.SearchTaskResult;

import java.util.concurrent.Callable;

public class SearchTask implements Callable<SearchTaskResult> {

    private final BenefitMatrix benefitMatrix;
    private final PriceVector priceVector;
    private final Person person;
    private final ItemList itemList;

    public SearchTask(BenefitMatrix benefitMatrix,
                      PriceVector priceVector,
                      Person person,
                      ItemList itemList) {
        this.benefitMatrix = benefitMatrix;
        this.priceVector = priceVector;
        this.person = person;
        this.itemList = itemList;
    }

    public BenefitMatrix getBenefitMatrix() {
        return benefitMatrix;
    }

    public PriceVector getPriceVector() {
        return priceVector;
    }

    public Person getPerson() {
        return person;
    }

    public ItemList getItemList() {
        return itemList;
    }

    @Override
    public SearchTaskResult call() {
        Item currentBestItem = null;
        double currentBestValue = -1.0;
        double currentSecondBestValue = -1.0;
        for (Item item : itemList) {
            int benefit = benefitMatrix.getBenefit(person, item);
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
        return new SearchTaskResult(currentBestItem, currentBestValue, currentSecondBestValue);
    }

    @Override
    public String toString() {
        return "SearchTask{" +
                "person=" + person +
                ", itemList=" + itemList +
                '}';
    }
}
