package com.netcracker.algorithms.auction.auxillary.entities.tasks;

import com.netcracker.algorithms.auction.auxillary.entities.aggregates.BenefitMatrix;
import com.netcracker.algorithms.auction.auxillary.entities.aggregates.ItemList;
import com.netcracker.algorithms.auction.auxillary.entities.aggregates.PriceVector;
import com.netcracker.algorithms.auction.auxillary.entities.basic.Bid;
import com.netcracker.algorithms.auction.auxillary.entities.basic.Person;
import com.netcracker.algorithms.auction.auxillary.entities.basic.SearchTaskResult;
import com.netcracker.algorithms.auction.auxillary.utils.ConcurrentUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;

import static com.netcracker.algorithms.auction.auxillary.logic.bids.BidMaker.makeBid;

public class ParallelBidTask extends BidTask {

    private final ExecutorService executorService;
    private final int numberOfSearchTasksPerPerson;

    public ParallelBidTask(BenefitMatrix benefitMatrix,
                           PriceVector priceVector,
                           Person person,
                           ItemList itemList,
                           double epsilon,
                           ExecutorService executorService,
                           int numberOfSearchTasksPerPerson) {
        this(
                benefitMatrix,
                priceVector,
                Collections.singletonList(person),
                itemList,
                epsilon,
                executorService,
                numberOfSearchTasksPerPerson
        );
    }

    public ParallelBidTask(BenefitMatrix benefitMatrix,
                           PriceVector priceVector,
                           List<Person> personList,
                           ItemList itemList,
                           double epsilon,
                           ExecutorService executorService,
                           int numberOfSearchTasksPerPerson) {
        super(benefitMatrix, priceVector, personList, itemList, epsilon);
        this.executorService = executorService;
        this.numberOfSearchTasksPerPerson = numberOfSearchTasksPerPerson;
    }

    @Override
    public List<Bid> call() {
        List<ItemList> itemListPartList = getItemList().split(numberOfSearchTasksPerPerson);
        Collection<Person> personCollection = getPersonCollection();
        List<Bid> bidList = new ArrayList<>(personCollection.size());
        for (Person person : getPersonCollection()) {
            List<SearchTask> searchTaskList = new ArrayList<>(numberOfSearchTasksPerPerson);
            for (ItemList itemListPart : itemListPartList) {
                SearchTask searchTask = new SearchTask(
                        getBenefitMatrix(),
                        getPriceVector(),
                        person,
                        itemListPart
                );
                searchTaskList.add(searchTask);
            }
            List<SearchTaskResult> resultList = ConcurrentUtils.executeCallableList(searchTaskList, executorService);
            SearchTaskResult mergedResult = SearchTaskResult.mergeResults(resultList);
            Bid bid = makeBid(person, mergedResult, getEpsilon());
            bidList.add(bid);
        }
        return bidList;
    }
}
