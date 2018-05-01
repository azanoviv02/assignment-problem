package com.netcracker.assignment.utils.io;

import org.junit.Test;

import java.util.List;

import static com.netcracker.assignment.utils.io.ResultPrinter.findTotalWeightForAssignment;
import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;

public class ResultPrinterTest {

    private final static int[][] BENEFIT_MATRIX = {
            {13, 2, 4, 8, 2, 19, 6, 18, 6, 4, 15, 17, 4, 19, 6, 3, 0, 15, 3, 19},
            {2, 0, 3, 19, 19, 9, 4, 12, 13, 0, 3, 10, 7, 0, 9, 9, 11, 15, 10, 1},
            {4, 15, 9, 0, 1, 4, 15, 0, 12, 4, 9, 3, 12, 16, 12, 14, 0, 0, 17, 6},
            {1, 0, 3, 5, 11, 3, 9, 6, 1, 14, 4, 14, 6, 17, 15, 12, 1, 3, 4, 7},
            {18, 14, 6, 4, 3, 14, 13, 12, 18, 19, 10, 8, 9, 16, 9, 6, 12, 1, 15, 17},
            {11, 1, 2, 2, 10, 16, 0, 6, 18, 3, 15, 2, 7, 15, 11, 5, 13, 8, 2, 5},
            {7, 14, 17, 19, 0, 12, 6, 2, 1, 7, 8, 2, 7, 14, 14, 12, 0, 12, 13, 10},
            {3, 13, 9, 2, 2, 18, 8, 0, 7, 7, 13, 14, 11, 19, 6, 2, 16, 5, 1, 2},
            {16, 2, 11, 4, 7, 13, 3, 5, 19, 12, 7, 14, 0, 19, 6, 6, 3, 0, 12, 0},
            {5, 18, 2, 1, 7, 7, 1, 5, 2, 2, 9, 18, 9, 13, 2, 10, 9, 2, 7, 9},
            {19, 15, 14, 18, 4, 9, 17, 12, 19, 7, 15, 16, 5, 19, 10, 11, 9, 17, 19, 18},
            {5, 9, 19, 6, 2, 9, 16, 1, 0, 4, 6, 0, 8, 12, 18, 4, 17, 19, 12, 7},
            {2, 6, 19, 11, 0, 7, 17, 8, 3, 14, 6, 19, 11, 16, 18, 1, 2, 6, 7, 0},
            {11, 12, 18, 10, 7, 17, 18, 15, 14, 9, 7, 16, 13, 2, 16, 10, 16, 3, 13, 14},
            {2, 10, 8, 9, 13, 19, 8, 5, 8, 11, 2, 16, 2, 19, 16, 14, 5, 1, 15, 1},
            {3, 15, 1, 3, 16, 17, 4, 8, 19, 12, 3, 8, 12, 11, 6, 2, 0, 13, 13, 10},
            {13, 1, 11, 18, 7, 15, 8, 9, 8, 14, 19, 4, 6, 17, 13, 6, 6, 16, 0, 3},
            {16, 13, 7, 17, 9, 10, 4, 14, 11, 4, 13, 1, 5, 19, 10, 10, 2, 6, 8, 11},
            {7, 6, 9, 16, 11, 5, 6, 11, 7, 0, 9, 2, 1, 13, 16, 9, 5, 4, 16, 1},
            {18, 14, 18, 7, 0, 17, 3, 1, 8, 6, 9, 5, 11, 0, 12, 14, 17, 12, 19, 1}
    };

    private final static List<Integer> ASSIGNMENT_ONE = asList(19, 4, 15, 11, 9, 8, 3, 16, 13, 1, 0, 17, 2, 6, 5, 12, 10, 7, 14, 18);
    private final static List<Integer> ASSIGNMENT_TWO = asList(7, 4, 15, 14, 9, 8, 3, 16, 0, 1, 19, 17, 11, 6, 5, 12, 10, 13, 18, 2);

    @Test
    public void findTotalWeightForAssignmentTest() throws Exception {
        int totalWeightOne = findTotalWeightForAssignment(BENEFIT_MATRIX, ASSIGNMENT_ONE);
        int totalWeightTwo = findTotalWeightForAssignment(BENEFIT_MATRIX, ASSIGNMENT_TWO);
        assertEquals(totalWeightOne, totalWeightTwo);
    }

}