package com.netcracker.algorithms.auction.auxillary.entities;

import java.util.Arrays;

public class Assignment {

    public final static int UNASSIGNED_VALUE = -1;

    public final static Assignment getInitialAssignment(int n) {
        return new Assignment(getFilledIntArray(n, UNASSIGNED_VALUE));
    }


    private final int[] assignmentArray;

    public Assignment(int[] assignmentArray) {
        this.assignmentArray = assignmentArray;
    }

    public int getPersonForObject(int objectIndex){
        return assignmentArray[objectIndex];
    }

    public void setPersonForObject(int objectIndex, int personIndex){
        assignmentArray[objectIndex] = personIndex;
    }

    /**
     * Returns array, where each element represent object.
     * Should be used only to get result.
     *
     * @return
     */
    private int[] getObjectAssignment() {
        return assignmentArray;
    }

    /**
     * Returns array, where each element represent person.
     * Used only to get final result
     *
     * @return
     */
    public int[] getPersonAssignment() {
        if (isComplete()) {
            return getReversedAssignment(assignmentArray);
        } else {
            throw new IllegalStateException("Unable to revert incomplete assignmentArray");
        }
    }


    public boolean isComplete() {
        return !arrayContains(assignmentArray, UNASSIGNED_VALUE);
    }

    @Override
    public String toString() {
        return Arrays.toString(assignmentArray);
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
