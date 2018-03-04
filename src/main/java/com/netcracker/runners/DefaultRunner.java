package com.netcracker.runners;

import com.netcracker.algorithms.AssignmentProblemSolver;

import java.util.*;
import java.util.stream.Collectors;

import static com.netcracker.utils.io.MatrixReader.readMatricesFromFile;
import static com.netcracker.utils.io.ResultsPrinter.printResults;
import static com.netcracker.utils.SolverSupplier.createSolverMap;
import static com.netcracker.utils.Validator.assignmentsMatch;
import static com.netcracker.utils.Validator.containsDuplicates;

public class DefaultRunner {

    public static void run() {

        // Input cost matrices
        List<int[][]> matrixList = readMatricesFromFile(getFileNames());

        // Available assignment problem solvers
        Map<String, AssignmentProblemSolver> solverMap = createSolverMap();

        // Solution for each cost matrix and each solver
        Map<int[][], Map<String, List<Integer>>> allResults = new LinkedHashMap<>();
        for (int[][] matrix : matrixList) {
            Map<String, List<Integer>> resultsForMatrix = new LinkedHashMap<>();
            for (Map.Entry<String, AssignmentProblemSolver> solverEntry : solverMap.entrySet()) {
                String solverName = solverEntry.getKey();
                AssignmentProblemSolver solver = solverEntry.getValue();
                List<Integer> assignment = convertArrayToList(solver.findMaxCostMatching(matrix));
                assert !containsDuplicates(assignment);
                resultsForMatrix.put(solverName, assignment);
            }
            assert assignmentsMatch(resultsForMatrix);
            allResults.put(matrix, resultsForMatrix);
        }

        // Output
        printResults(allResults);
    }

    public static List<String> getFileNames() {
        List<String> fileNames = new ArrayList<>();

        fileNames.add("/matrices/util10.txt");
        fileNames.add("/matrices/util20.txt");
        fileNames.add("/matrices/util40.txt");
        fileNames.add("/matrices/util60.txt");

        return fileNames;
    }

    public static List<Integer> convertArrayToList(int[] array) {
        return Arrays
                .stream(array)
                .boxed()
                .collect(Collectors.toList());
    }
}
