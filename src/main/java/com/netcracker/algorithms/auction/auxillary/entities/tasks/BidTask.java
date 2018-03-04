package com.netcracker.algorithms.auction.auxillary.entities.tasks;

import com.netcracker.algorithms.auction.auxillary.entities.aggregates.BenefitMatrix;
import com.netcracker.algorithms.auction.auxillary.entities.aggregates.ItemList;
import com.netcracker.algorithms.auction.auxillary.entities.aggregates.PriceVector;
import com.netcracker.algorithms.auction.auxillary.entities.basic.Bid;
import com.netcracker.algorithms.auction.auxillary.entities.basic.Person;
import com.netcracker.algorithms.auction.auxillary.entities.basic.SearchTaskResult;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Callable;

import static com.netcracker.algorithms.auction.auxillary.logic.bids.BidMaker.makeBid;

public class BidTask implements Callable<List<Bid>> {

    private final BenefitMatrix benefitMatrix;
    private final PriceVector priceVector;
    private final Collection<Person> personCollection;
    private final ItemList itemList;
    private final double epsilon;

    public BidTask(BenefitMatrix benefitMatrix,
                   PriceVector priceVector,
                   Collection<Person> personCollection,
                   ItemList itemList,
                   double epsilon) {
        this.benefitMatrix = benefitMatrix;
        this.priceVector = priceVector;
        this.personCollection = personCollection;
        this.itemList = itemList;
        this.epsilon = epsilon;
    }

    public BenefitMatrix getBenefitMatrix() {
        return benefitMatrix;
    }

    public PriceVector getPriceVector() {
        return priceVector;
    }

    public Collection<Person> getPersonCollection() {
        return personCollection;
    }

    public ItemList getItemList() {
        return itemList;
    }

    public double getEpsilon() {
        return epsilon;
    }

    @Override
    public List<Bid> call() {
        List<Bid> bidList = new LinkedList<>();
        for (Person person : personCollection) {
            SearchTask searchTask = new SearchTask(
                    benefitMatrix,
                    priceVector,
                    person,
                    itemList
            );
            SearchTaskResult result = searchTask.call();
            Bid bid = makeBid(person, result, epsilon);
            bidList.add(bid);
        }
        return bidList;
    }
}
