package com.netcracker.assignment.algorithms.auction.auxillary.logic.epsilonscaling;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * Default implementation of EpsilonSequenceProducer.
 * Creates geometrically decreasing sequence.
 */
public class DefaultEpsilonSequenceProducer implements EpsilonSequenceProducer {

    private final double epsilonInitial;
    private final double epsilonMultiplier;

    public DefaultEpsilonSequenceProducer(double epsilonInitial, double epsilonMultiplier) {
        this.epsilonInitial = epsilonInitial;
        this.epsilonMultiplier = epsilonMultiplier;
    }

    @Override
    public List<Double> getEpsilonSequence(int problemSize) {
        List<Double> epsilonSequence = new LinkedList<>();
        for (double epsilon = epsilonInitial; epsilon > 1.0 / problemSize; epsilon *= epsilonMultiplier) {
            epsilonSequence.add(epsilon);
        }
        return Collections.unmodifiableList(epsilonSequence);
    }
}
