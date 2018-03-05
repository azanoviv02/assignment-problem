package com.netcracker.algorithms.auction.auxillary.entities.aggregates;

import com.netcracker.algorithms.auction.auxillary.entities.basic.Person;

import java.util.*;
import java.util.function.Consumer;

public class PersonQueue implements Iterable<Person> {

    public static PersonQueue createFullPersonQueue(int n) {
        return new PersonQueue(getQueueOfRange(n));
    }

    private final Queue<Person> personQueue;

    private PersonQueue(Queue personQueue) {
        this.personQueue = personQueue;
    }

    public boolean add(Person person) {
        return personQueue.add(person);
    }

    public Person remove() {
        return personQueue.remove();
    }

    public Person poll() {
        return personQueue.poll();
    }

    public List<Person> removeSeveral(int numberOfPersonsToRemove) {
        final List<Person> removedPersonList = new ArrayList<>(numberOfPersonsToRemove);
        int alreadyRemoved = 0;
        while (alreadyRemoved < numberOfPersonsToRemove && !isEmpty()) {
            final Person person = remove();
            removedPersonList.add(person);
        }
        return removedPersonList;
    }

    public List<Person> removeAll(){
        return removeSeveral(personQueue.size());
    }

    public boolean isEmpty() {
        return personQueue.isEmpty();
    }

    public boolean containsDuplicates() {
        Set<Person> personSet = new HashSet<>(personQueue);
        return personSet.size() != personQueue.size();
    }

    @Override
    public Iterator<Person> iterator() {
        return personQueue.iterator();
    }

    @Override
    public void forEach(Consumer<? super Person> action) {
        personQueue.forEach(action);
    }

    @Override
    public String toString() {
        return personQueue.toString();
    }

    private static Queue<Person> getQueueOfRange(int toExclusive) {
        Queue<Person> rangeQueue = new ArrayDeque<>(toExclusive);
        for (int i = 0; i < toExclusive; i++) {
            rangeQueue.add(new Person(i));
        }
        return rangeQueue;
    }
}
