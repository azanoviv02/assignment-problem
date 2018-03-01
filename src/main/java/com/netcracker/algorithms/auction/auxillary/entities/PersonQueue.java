package com.netcracker.algorithms.auction.auxillary.entities;

import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Queue;
import java.util.Set;
import java.util.Spliterator;
import java.util.function.Consumer;
import java.util.stream.Stream;

public class PersonQueue implements Iterable<Integer> {

    public static final PersonQueue getInitialPersonQueue(int n) {
        return new PersonQueue(getQueueOfRange(n));
    }


    private final Queue<Integer> personQueue;

    private PersonQueue(Queue personQueue) {
        this.personQueue = personQueue;
    }

    public boolean add(int personIndex) {
        return personQueue.add(personIndex);
    }

    public int remove() {
        return personQueue.remove();
    }

    public boolean isEmpty() {
        return personQueue.isEmpty();
    }

    public int size() {
        return personQueue.size();
    }

    public void clear() {
        personQueue.clear();
    }

    public boolean containsDuplicates() {
        Set<Integer> personSet = new HashSet<>(personQueue);
        return personSet.size() != personQueue.size();
    }

    @Override
    public Iterator<Integer> iterator() {
        return personQueue.iterator();
    }

    @Override
    public Spliterator<Integer> spliterator() {
        return personQueue.spliterator();
    }

    public Stream<Integer> stream() {
        return personQueue.stream();
    }

    public Stream<Integer> parallelStream() {
        return personQueue.parallelStream();
    }

    @Override
    public void forEach(Consumer<? super Integer> action) {
        personQueue.forEach(action);
    }

    private static Queue<Integer> getQueueOfRange(int toExclusive) {
        Queue<Integer> rangeQueue = new ArrayDeque<>(toExclusive);
        for (int i = 0; i < toExclusive; i++) {
            rangeQueue.add(i);
        }
        return rangeQueue;
    }
}
