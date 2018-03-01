package com.netcracker.algorithms.auction.auxillary.entities.aggregates;

import com.netcracker.algorithms.auction.auxillary.entities.basic.Item;
import com.netcracker.algorithms.auction.auxillary.entities.basic.Person;

public class BenefitMatrix {

    private final int[][] benefitMatrix;

    public BenefitMatrix(int[][] benefitMatrix) {
        this.benefitMatrix = benefitMatrix;
    }

    public int getBenefit(Person person, Item item){
        return benefitMatrix[person.getPersonIndex()][item.getItemIndex()];
    }

    public int size(){
        return benefitMatrix.length;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for(int[] line : benefitMatrix){
            for(int element : line){
                sb.append(String.format("%4d ", element));
            }
            sb.append("\n");
        }
        return sb.toString();
    }
}
