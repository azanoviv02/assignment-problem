package com.netcracker.algorithms.auction.auxillary.entities;

import java.util.Arrays;

public class Assignment {

    public final static Assignment getInitialAssignment(int n){
        return new Assignment(getFilledIntArray(n, UNASSIGNED_VALUE));
    }

    public final static int UNASSIGNED_VALUE = -1;

    private final int[] assignment;

    public Assignment(int[] assignment) {
        this.assignment = assignment;
    }

    /**
     * Returns array, where each element represent object
     *
     * @return
     */
    public int[] getObjectAssignment() {
        return assignment;
    }

    public int[] getPersonAssignment() {
        if (isComplete()) {
            return getReversedAssignment(assignment);
        } else {
            throw new IllegalStateException("Unable to revert incomplete assignment");
        }
    }

    public boolean isComplete() {
        return !arrayContains(assignment, UNASSIGNED_VALUE);
    }

    @Override
    public String toString() {
        return Arrays.toString(assignment);
    }

    private static int[] getReversedAssignment(int[] assignment) {
        final int n = assignment.length;
        final int[] reversedAssignment = new int[n];
        for (int i = 0; i < n; i++) {
            final int value = assignment[i];
            reversedAssignment[value] = i;
        }
        return reversedAssignment;
    }

    private static boolean arrayContains(int[] array, int value) {
        for (int element : array) {
            if (element == value) {
                return true;
            }
        }
        return false;
    }

    private static int[] getFilledIntArray(int n, int value) {
        int[] array = new int[n];
        Arrays.fill(array, value);
        return array;
    }
}
