package com.netcracker;

import com.netcracker.algorithms.AssignmentProblemSolver;
import com.netcracker.algorithms.auction.AuctionAlgorithm;
import com.netcracker.algorithms.auction.SynchronousJacobiAlgorithm;
import com.netcracker.algorithms.hungarian.HungarianAlgorithm;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

import static com.netcracker.utils.MatrixReader.readMatricesFromFile;
import static java.util.Map.Entry;

public class Main {

    public static void main(String[] args) throws IOException {
        // Input cost matrices
        List<int[][]> matrixList = readMatricesFromFile(getFileNames());

        // Available assignment problem solvers
        Map<String, AssignmentProblemSolver> solverMap = new LinkedHashMap<>();
        solverMap.put("Hungarian", new HungarianAlgorithm());
        solverMap.put("Auction", new AuctionAlgorithm());
        solverMap.put("SynchronousJacobi", new SynchronousJacobiAlgorithm(4));

        // Solution for each cost matrix and each solver
        Map<int[][], Map<String, int[]>> allResults = new LinkedHashMap<>();
        for (int[][] matrix : matrixList) {
            Map<String, int[]> resultsForMatrix = new LinkedHashMap<>();
            for (Entry<String, AssignmentProblemSolver> solverEntry : solverMap.entrySet()) {
                System.out.println("Solver: "+solverEntry.getKey());

                String solverName = solverEntry.getKey();
                AssignmentProblemSolver solver = solverEntry.getValue();

                int[] result = solver.findMaxCostMatching(matrix);

                if (containsDuplicates(result)) {
                    throw new IllegalStateException("Duplicate assignments are not allowed: "+Arrays.toString(result));
                }
                resultsForMatrix.put(solverName, result);
            }
            allResults.put(matrix, resultsForMatrix);
        }

        // Output
        for (Entry<int[][], Map<String, int[]>> entry : allResults.entrySet()) {
            int[][] matrix = entry.getKey();
            Map<String, int[]> resultsForMatrix = entry.getValue();

            System.out.println("Size: " + matrix.length);
            for (Entry<String, int[]> entry1 : resultsForMatrix.entrySet()) {
                String solverName = entry1.getKey();
                int[] matching = entry1.getValue();

                int totalWeight = findWeigthForMatching(matrix, matching);
                System.out.println(String.format("(%s) Total weight: %d", solverName, totalWeight));
                System.out.println(Arrays.toString(matching));
            }
            System.out.println();
        }

    }

    public static List<String> getFileNames() {
        List<String> fileNames = new ArrayList<>();

        fileNames.add("/matrices/util10.txt");
        fileNames.add("/matrices/util20.txt");
        fileNames.add("/matrices/util40.txt");
        fileNames.add("/matrices/util60.txt");

        return fileNames;
    }

    public static boolean containsDuplicates(int[] array) {
        Set<Integer> set = Arrays
                .stream(array)
                .boxed()
                .collect(Collectors.toSet());
        return set.size() != array.length;
    }

    public static int findWeigthForMatching(int[][] matrix, int[] matching) {
        int totalWeight = 0;
        for (int i = 0; i < matching.length; i++) {
            totalWeight += matrix[i][matching[i]];
        }
        return totalWeight;
    }
}
