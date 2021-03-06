package com.netcracker.assignment.runners;

import com.netcracker.assignment.algorithms.AssignmentProblemSolver;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.netcracker.assignment.utils.AssertionMaker.makeAssertion;
import static com.netcracker.assignment.utils.GeneralUtils.convertArrayToList;
import static com.netcracker.assignment.utils.GeneralUtils.toLinkedMap;
import static com.netcracker.assignment.utils.SolverSupplier.createSolverMap;
import static com.netcracker.assignment.utils.Validator.containsDuplicates;
import static com.netcracker.assignment.utils.io.MatrixReader.readMatricesFromFile;
import static com.netcracker.assignment.utils.io.ResultPrinter.printResults;
import static java.lang.Thread.sleep;

public class DefaultRunner {

    public static void run() {

        // Input cost matrices
        List<int[][]> matrixList = readMatricesFromFile(getFileNames());

        // Available assignment problem solvers
        Map<String, AssignmentProblemSolver> solverMap = createSolverMap();

        // Find assignment for each cost matrix using each solver
        Map<int[][], Map<String, List<Integer>>> allAssignments = findAssignmentForEveryMatrix(matrixList, solverMap);
//        //todo remove loop
//        for(int i = 0; i < 300; i++){
//            Map<int[][], Map<String, List<Integer>>> assignmentForEveryMatrix = findAssignmentForEveryMatrix(matrixList, solverMap);
//            System.out.println(i+": "+assignmentForEveryMatrix);
//        }

        // Make pause in order to clearly separate execution output and result output
        makePause(2000);

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
        // todo replace with assertion for same total weight
//        makeAssertion(assignmentsAreSame(assignmentsForMatrix));
        return assignmentsForMatrix;
    }

    public static List<Integer> findAssignmentUsingOneSolver(int[][] matrix,
                                                             AssignmentProblemSolver solver) {
        final int[] assignmentArray = solver.findMaxCostAssignment(matrix);
        final List<Integer> assignmentList = convertArrayToList(assignmentArray);
        makeAssertion(!containsDuplicates(assignmentList));
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

    private static void makePause(int duration) {
        try {
            sleep(duration);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
