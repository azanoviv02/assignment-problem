package com.netcracker.algorithms.auction.auxillary.logic.relaxation;

import java.util.LinkedList;
import java.util.List;

public class EpsilonProducer {

    private final double epsilonInitial;
    private final double epsilonMultiplier;

    public EpsilonProducer(double epsilonInitial, double epsilonMultiplier) {
        this.epsilonInitial = epsilonInitial;
        this.epsilonMultiplier = epsilonMultiplier;
    }

    public List<Double> getEpsilonList(int n){
        List<Double> epsilonList = new LinkedList<>();
        for (double epsilon = epsilonInitial; epsilon > 1.0 / n; epsilon *= epsilonMultiplier) {
            epsilonList.add(epsilon);
        }
        return epsilonList;
    }
}
