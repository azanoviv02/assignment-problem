package com.netcracker.algorithms.auction;

import com.netcracker.algorithms.AssignmentProblemSolver;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class RefactoredAuctionAlgorithm implements AssignmentProblemSolver {

    private final static double INITIAL_PRICE = 1.0;
    private final static int UNASSIGNED_VALUE = Integer.MAX_VALUE;

    @Override
    public int[] findMaxCostMatching(int[][] costMatrix) {
        
        final int n = costMatrix.length;

        double[] prices = getFilledDoubleArray(n, INITIAL_PRICE);
        int[] assignment = getFilledIntArray(n, UNASSIGNED_VALUE);

        for (double epsilon = 1.0; epsilon > 1.0 / n; epsilon *= .25) {
            assignment = getFilledIntArray(n, UNASSIGNED_VALUE);
            while (arrayContains(assignment, UNASSIGNED_VALUE)) {
                auctionRound(assignment, prices, costMatrix, epsilon);
            }
        }

        return assignment;
    }

    private void auctionRound(int[] assignment, double[] prices, int[][] costMatrix, double epsilon) {

        final int n = prices.length;

        List<Integer> tempBidded = new LinkedList<>();
        List<Double> tempBids = new LinkedList<>();
        List<Integer> nonAssigned = new LinkedList<>();

	    /* Bidding phase */
        for (int i = 0; i < n; i++) {

            if (assignment[i] == UNASSIGNED_VALUE) {
                nonAssigned.add(i);

                /*
                    Need the best and second best value of each object to this person
                    where value is calculated row_{j} - prices{j}
                */
                double bestValue = -UNASSIGNED_VALUE;
                double secondBestValue = -UNASSIGNED_VALUE;

                int bestObjectIndex = 0;

                for (int j = 0; j < n; j++) {
                    int cost = costMatrix[i][j];
                    double price = prices[j];
                    double value = cost - price;

                    if (value > bestValue) {
                        secondBestValue = bestValue;
                        bestValue = value;
                        bestObjectIndex = j;
                    } else if (value > secondBestValue) {
                        secondBestValue = value;
                    }
                }

			    /* Computes the highest reasonable bid for the best object for this person */
                double bid = bestValue - secondBestValue + epsilon;

			    /* Stores the bidding info for future use */
                tempBidded.add(bestObjectIndex);
                tempBids.add(bid);
            }
        }

        /* Assignment phase*/
        for (int j = 0; j < n; j++) {
            List<Integer> bidderIndexList = getIndicesWithValue(tempBidded, j);
            if (!bidderIndexList.isEmpty()) {

			    /* Need the highest bid for object j */
                double highestBid = -UNASSIGNED_VALUE;
                int highestBidderIndex = -1;
                for (int i = 0; i < bidderIndexList.size(); i++) {
                    double bid = tempBids.get(bidderIndexList.get(i));
                    if (bid > highestBid) {
                        highestBid = bid;
                        highestBidderIndex = bidderIndexList.get(i);
                    }
                }

			    /* Find the other person who has object j and make them unassigned */
                for (int i = 0; i < assignment.length; i++) {
                    if (assignment[i] == j) {
                        assignment[i] = UNASSIGNED_VALUE;
                        break;
                    }
                }

			    /* Assign object j to i_j and update the price array */
//                assignment.set(nonAssigned.get(i_j), j);
                assignment[nonAssigned.get(highestBidderIndex)] = j;
                prices[j] += highestBid;
            }
        }
    }

    private static int[] getFilledIntArray(int n, int value){
        int[] array = new int[n];
        Arrays.fill(array, value);
        return array;
    }

    private static double[] getFilledDoubleArray(int n, double value){
        double[] array = new double[n];
        Arrays.fill(array, value);
        return array;
    }

    private static boolean arrayContains(int[] array, int value){
        for(int element : array){
            if(element == value){
                return true;
            }
        }
        return false;
    }

    private static List<Integer> getIndicesWithValue(final List<Integer> list, final int value) {
        return IntStream
                .range(0, list.size())
                .filter(i -> list.get(i) == value)
                .boxed()
                .collect(Collectors.toList());
    }
}
