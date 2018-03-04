package com.netcracker.utils;

import com.netcracker.algorithms.AssignmentProblemSolver;
import com.netcracker.algorithms.auction.AuctionAlgorithm;
import com.netcracker.algorithms.auction.implementation.AuctionImplementation;
import com.netcracker.algorithms.auction.implementation.synchronous.SynchronousGaussSeidel;
import com.netcracker.algorithms.auction.implementation.synchronous.SynchronousHybrid;
import com.netcracker.algorithms.auction.implementation.synchronous.SynchronousJacobi;
import com.netcracker.algorithms.hungarian.HungarianAlgorithm;

import java.util.LinkedHashMap;
import java.util.Map;

public class SolverSupplier {

    public static Map<String, AssignmentProblemSolver> createSolverMap() {
        Map<String, AssignmentProblemSolver> solverMap = new LinkedHashMap<>();
        solverMap.put("Hungarian", new HungarianAlgorithm());
        for (Map.Entry<String, AuctionImplementation> entry : createAuctionImplementationMap().entrySet()) {
            final String implementationName = entry.getKey();
            final AuctionImplementation implementation = entry.getValue();
            solverMap.put(implementationName, new AuctionAlgorithm(implementation));
        }
        return solverMap;
    }

    public static Map<String, AuctionImplementation> createAuctionImplementationMap() {
        Map<String, AuctionImplementation> auctionImplementationMap = new LinkedHashMap<>();
        auctionImplementationMap.put("SynchronousJacobi", new SynchronousJacobi(4, 4));
        auctionImplementationMap.put("SynchronousGaussSeidel", new SynchronousGaussSeidel(4, 4));
        auctionImplementationMap.put("SynchronousHybrid", new SynchronousHybrid(4, 2, 2));
        return auctionImplementationMap;
    }
}
