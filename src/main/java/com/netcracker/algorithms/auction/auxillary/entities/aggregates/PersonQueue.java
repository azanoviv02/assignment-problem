package com.netcracker.algorithms.auction.auxillary.entities.aggregates;

import com.netcracker.algorithms.auction.auxillary.entities.basic.Person;

import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;
import java.util.Spliterator;
import java.util.function.Consumer;
import java.util.stream.Stream;

public class PersonQueue implements Iterable<Person> {

    public static PersonQueue createFullPersonQueue(int n) {
        return new PersonQueue(getQueueOfRange(n));
    }

    public static PersonQueue createEmptyPersonQueue() {
        return new PersonQueue(new LinkedList());
    }


    private final Queue<Person> personQueue;

    private PersonQueue(Queue personQueue) {
        this.personQueue = personQueue;
    }

    public boolean add(Person person) {
        return personQueue.add(person);
    }

    public boolean addAll(PersonQueue anotherPersonQueue) {
        return personQueue.addAll(anotherPersonQueue.personQueue);
    }

    public Person remove() {
        return personQueue.remove();
    }

    public boolean isEmpty() {
        return personQueue.isEmpty();
    }

    public int size() {
        return personQueue.size();
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
    public Spliterator<Person> spliterator() {
        return personQueue.spliterator();
    }

    public Stream<Person> stream() {
        return personQueue.stream();
    }

    public Stream<Person> parallelStream() {
        return personQueue.parallelStream();
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
