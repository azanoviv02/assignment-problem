package com.netcracker.algorithms.auction.auxillary.logic.epsilonscaling;

import java.util.List;

/**
 * Used for decoupling the logic of epsilon scaling from actual algorithm implementation.
 * It means:
 * - The same EpsilonSequenceProducer can be used for different problem solvers (i.e. both synchronous and
 * asynchronous auction algorithms) or even for different problems altogether (i.e. assignment and
 * transportation problem).
 * - The same problem solver can use different EpsilonSequenceProducer implementations (for example, which
 * produces exponentially instead of geometrically).
 */
public interface EpsilonSequenceProducer {
    List<Double> getEpsilonSequence(int problemSize);
}
