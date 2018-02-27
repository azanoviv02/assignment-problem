package com.netcracker.algorithms.auction;

import com.netcracker.algorithms.AssignmentProblemSolver;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static java.lang.Integer.MAX_VALUE;

public class AuctionAlgorithm implements AssignmentProblemSolver {

    @Override
    public int[] findMaxCostMatching(int[][] costMatrix) {
        
        final int n = costMatrix.length;

        double[] prices = new double[n];
        Arrays.fill(prices, 1.0);

        List<Integer> assignment = getFilledList(n, MAX_VALUE);

        for (double epsilon = 1.0; epsilon > 1.0 / n; epsilon *= .25) {
            assignment = getFilledList(n, MAX_VALUE);
            while (assignment.contains(MAX_VALUE)) {
                auctionRound(assignment, prices, costMatrix, epsilon);
            }
        }

        return assignment
                .stream()
                .mapToInt(i -> i)
                .toArray();
    }

    private void auctionRound(List<Integer> assignment, double[] prices, int[][] costMatrix, double epsilon) {

        final int n = prices.length;

        List<Integer> tempBidded = new LinkedList<>();
        List<Double> tempBids = new LinkedList<>();
        List<Integer> nonAssigned = new LinkedList<>();

	    /* Compute the bids of each unassigned individual and store them in temp */
        for (int i = 0; i < n; i++) {
            if (assignment.get(i) == MAX_VALUE) {
                nonAssigned.add(i);

                /*
                    Need the best and second best value of each object to this person
                    where value is calculated row_{j} - prices{j}
                */
                double optValForI = -MAX_VALUE;
                double secOptValForI = -MAX_VALUE;
                int optObjForI = 0;
                for (int j = 0; j < n; j++) {
                    int cost = costMatrix[i][j];
                    double price = prices[j];
                    double curVal = cost - price;
                    if (curVal > optValForI) {
                        secOptValForI = optValForI;
                        optValForI = curVal;
                        optObjForI = j;
                    } else if (curVal > secOptValForI) {
                        secOptValForI = curVal;
                    }
                }

			    /* Computes the highest reasonable bid for the best object for this person */
                double bidForI = optValForI - secOptValForI + epsilon;

			    /* Stores the bidding info for future use */
                tempBidded.add(optObjForI);
                tempBids.add(bidForI);
            }
        }

        /*
            Each object which has received a bid determines the highest bidder and
            updates its price accordingly
        */
        for (int j = 0; j < n; j++) {
            List<Integer> indexList = getIndicesWithValue(tempBidded, j);
            if (indexList.size() != 0) {

			    /* Need the highest bid for object j */
                double highestBidForJ = -MAX_VALUE;
                int i_j = -1;
                for (int i = 0; i < indexList.size(); i++) {
                    double curVal = tempBids.get(indexList.get(i));
                    if (curVal > highestBidForJ) {
                        highestBidForJ = curVal;
                        i_j = indexList.get(i);
                    }
                }

			    /* Find the other person who has object j and make them unassigned */
                for (int i = 0; i < assignment.size(); i++) {
                    if (assignment.get(i) == j) {
                        assignment.set(i, MAX_VALUE);
                        break;
                    }
                }

			    /* Assign object j to i_j and update the price array */
                assignment.set(nonAssigned.get(i_j), j);
                prices[j] += highestBidForJ;
            }
        }
    }

    private static List<Integer> getFilledList(int n, int value){
        return new ArrayList<>(Collections.nCopies(n, value));
    }

    private static List<Integer> getIndicesWithValue(final List<Integer> list, final int value) {
        return IntStream
                .range(0, list.size())
                .filter(i -> list.get(i) == value)
                .boxed()
                .collect(Collectors.toList());
    }
}
