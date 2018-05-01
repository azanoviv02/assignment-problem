package com.netcracker.assignment.utils.io;

import com.netcracker.assignment.utils.io.logging.Logger;
import com.netcracker.assignment.utils.io.logging.SystemOutLogger;

import java.util.List;
import java.util.Map;

public class ResultPrinter {

    private final static Logger logger = new SystemOutLogger(true);

    public static void printResults(Map<int[][], Map<String, List<Integer>>> allResults) {
        allResults.forEach((matrix, allAssignmentsForMatrix) -> {
            logger.info("Assignments for size: %d", matrix.length);
            allAssignmentsForMatrix.forEach((solverName, assignment) -> {
                final int totalWeight = findTotalWeightForAssignment(matrix, assignment);
                logger.info("(%s) Total weight: %d", solverName, totalWeight);
                logger.info("%s", assignment);
            });
            logger.info("\n");
        });
    }

    public static int findTotalWeightForAssignment(int[][] matrix, List<Integer> assignment) {
        int totalWeight = 0;
        for (int i = 0; i < assignment.size(); i++) {
            totalWeight += matrix[i][assignment.get(i)];
        }
        return totalWeight;
    }
}
