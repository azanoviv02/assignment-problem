package com.netcracker.runners;

import com.netcracker.algorithms.AssignmentProblemSolver;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.netcracker.utils.GeneralUtils.convertArrayToList;
import static com.netcracker.utils.GeneralUtils.toLinkedMap;
import static com.netcracker.utils.SolverSupplier.createSolverMap;
import static com.netcracker.utils.Validator.assignmentsAreSame;
import static com.netcracker.utils.Validator.containsDuplicates;
import static com.netcracker.utils.io.MatrixReader.readMatricesFromFile;
import static com.netcracker.utils.io.ResultPrinter.printResults;

public class DefaultRunner {

    public static void run() {

        // Input cost matrices
        List<int[][]> matrixList = readMatricesFromFile(getFileNames());

        // Available assignment problem solvers
        Map<String, AssignmentProblemSolver> solverMap = createSolverMap();

        // Find assignment for each cost matrix using each solver
        Map<int[][], Map<String, List<Integer>>> allAssignments = findAssignmentForEveryMatrix(matrixList, solverMap);

        // Output
        printResults(allAssignments);
    }

    public static Map<int[][], Map<String, List<Integer>>> findAssignmentForEveryMatrix(List<int[][]> matrixList,
                                                                                        Map<String, AssignmentProblemSolver> solverMap) {
        return matrixList
                .stream()
                .collect(toLinkedMap(
                        matrix -> matrix,
                        matrix -> findAssignmentUsingMultipleSolvers(matrix, solverMap)
                ));
    }

    public static Map<String, List<Integer>> findAssignmentUsingMultipleSolvers(int[][] matrix,
                                                                                Map<String, AssignmentProblemSolver> solverMap) {
        final Map<String, List<Integer>> assignmentsForMatrix = solverMap
                .entrySet()
                .stream()
                .collect(toLinkedMap(
                        Map.Entry::getKey,
                        solverEntry -> findAssignmentUsingOneSolver(matrix, solverEntry.getValue())
                ));
        assert assignmentsAreSame(assignmentsForMatrix);
        return assignmentsForMatrix;
    }

    public static List<Integer> findAssignmentUsingOneSolver(int[][] matrix,
                                                             AssignmentProblemSolver solver) {
        final int[] assignmentArray = solver.findMaxCostAssignment(matrix);
        final List<Integer> assignmentList = convertArrayToList(assignmentArray);
        assert !containsDuplicates(assignmentList);
        return assignmentList;
    }

    public static List<String> getFileNames() {
        List<String> fileNames = new ArrayList<>();

        fileNames.add("/matrices/util10.txt");
        fileNames.add("/matrices/util20.txt");
        fileNames.add("/matrices/util40.txt");
        fileNames.add("/matrices/util60.txt");

        return fileNames;
    }


}
