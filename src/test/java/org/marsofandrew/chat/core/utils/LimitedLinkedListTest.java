package org.marsofandrew.chat.core.utils;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.marsofandrew.chat.core.utils.exception.OversizeException;

import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertThrows;

public class LimitedLinkedListTest {
    private static final int MAX_SIZE = 2;

    private LimitedLinkedList<Integer> target;

    @BeforeEach
    void before() {
        target = new LimitedLinkedList<>(MAX_SIZE);
    }

    @Test()
    void testAddMoreThanMaxThrowsException() {
        var list = IntStream.range(0, MAX_SIZE).boxed().toList();
        target.addAll(list);

        assertThrows(OversizeException.class, () -> target.add(MAX_SIZE));
    }

    @Test()
    void testOfferMoreThanMaxThrowsException() {
        var list = IntStream.range(0, MAX_SIZE).boxed().toList();
        target.addAll(list);

        assertThrows(OversizeException.class, () -> target.offer(MAX_SIZE));
    }

    @Test()
    void testAddLastMoreThanMaxThrowsException() {
        var list = IntStream.range(0, MAX_SIZE).boxed().toList();
        target.addAll(list);

        assertThrows(OversizeException.class, () -> target.addLast(MAX_SIZE));
    }

    @Test
    void testAddAllLastMoreThanMaxThrowsException() {
        var list = IntStream.range(0, MAX_SIZE * 2).boxed().toList();
        assertThrows(OversizeException.class, () -> target.addAll(list));
    }
}
