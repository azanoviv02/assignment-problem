package com.netcracker.algorithms.auction.auxillary.logic.relaxation;

import java.util.LinkedList;
import java.util.List;

public class EpsilonProducer {

    public static List<Double> getEpsilonList(double epsilonInitial,
                                       double epsilonMultiplier,
                                       int n){
        List<Double> epsilonList = new LinkedList<>();
        for (double epsilon = epsilonInitial; epsilon > 1.0 / n; epsilon *= epsilonMultiplier) {
            epsilonList.add(epsilon);
        }
        return epsilonList;
    }
}
