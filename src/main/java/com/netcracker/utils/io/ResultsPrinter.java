package com.netcracker.utils.io;

import com.netcracker.utils.io.logging.Logger;
import com.netcracker.utils.io.logging.SystemOutLogger;

import java.util.List;
import java.util.Map;

public class ResultsPrinter {

    private final static Logger logger = new SystemOutLogger(true);

    public static void printResults(Map<int[][], Map<String, List<Integer>>> allResults) {
        for (Map.Entry<int[][], Map<String, List<Integer>>> entry : allResults.entrySet()) {
            int[][] matrix = entry.getKey();
            Map<String, List<Integer>> resultsForMatrix = entry.getValue();

            logger.info("Assignments for size: %d", matrix.length);
            for (Map.Entry<String, List<Integer>> entry1 : resultsForMatrix.entrySet()) {
                String solverName = entry1.getKey();
                List<Integer> matching = entry1.getValue();

                int totalWeight = findWeigthForMatching(matrix, matching);
                logger.info("(%s) Total weight: %d", solverName, totalWeight);
                logger.info("%s", matching);
            }
            logger.info("\n");
        }
    }

    public static int findWeigthForMatching(int[][] matrix, List<Integer> matching) {
        int totalWeight = 0;
        for (int i = 0; i < matching.size(); i++) {
            totalWeight += matrix[i][matching.get(i)];
        }
        return totalWeight;
    }
}
