package com.netcracker.assignment.algorithms.auction.auxillary.entities.aggregates;

import com.netcracker.assignment.algorithms.auction.auxillary.entities.basic.Item;
import com.netcracker.assignment.algorithms.auction.auxillary.entities.basic.Person;

import java.util.Arrays;

public class Assignment {

    public final static Assignment createInitialAssignment(int n) {
        return new Assignment(getFilledPersonArray(n, Person.NO_PERSON));
    }

    private final Person[] assignmentArray;

    public Assignment(Person[] assignmentArray) {
        this.assignmentArray = assignmentArray;
    }

    public Person getPersonForItem(Item item){
        return assignmentArray[item.getItemIndex()];
    }

    public void setPersonForItem(Item item, Person person){
        assignmentArray[item.getItemIndex()] = person;
    }

    /**
     * Returns array, where each element represent item.
     * Should be used only to get result.
     *
     * @return
     */
    private Person[] getItemAssignment() {
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
        return !arrayContains(assignmentArray, Person.NO_PERSON);
    }

    @Override
    public String toString() {
        return Arrays.toString(assignmentArray);
    }


    private static int[] getReversedAssignment(Person[] assignment) {
        final int n = assignment.length;
        final int[] reversedAssignment = new int[n];
        for (int i = 0; i < n; i++) {
            final Person value = assignment[i];
            reversedAssignment[value.getPersonIndex()] = i;
        }
        return reversedAssignment;
    }

    private static boolean arrayContains(Person[] array, Person value) {
        for (Person element : array) {
            if (element == value) {
                return true;
            }
        }
        return false;
    }

    private static Person[] getFilledPersonArray(int n, Person value) {
        Person[] array = new Person[n];
        Arrays.fill(array, value);
        return array;
    }
}
