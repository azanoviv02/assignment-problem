package com.netcracker.assignment.algorithms.auction.auxillary.entities.basic;

public class Person {

    public static final Person NO_PERSON = new Person(-1);

    private final int personIndex;

    public Person(int personIndex) {
        this.personIndex = personIndex;
    }

    public int getPersonIndex() {
        return personIndex;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Person person = (Person) o;

        return getPersonIndex() == person.getPersonIndex();
    }

    @Override
    public int hashCode() {
        return getPersonIndex();
    }

    @Override
    public String toString() {
        return Integer.toString(personIndex);
    }
}
