package org.marsofandrew.chat.core.utils;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class FramedQueueTest {

    private static final int MAX_SIZE = 2;

    private FramedQueue<Integer> target;

    @BeforeEach
    void before() {
        target = new FramedQueue<>(MAX_SIZE);
    }

    @Test
    void testAddNotIncreasesSizeMoreThanMax() {
        for (int i = 0; i < MAX_SIZE * 2; i++) {
            target.add(i);
        }

        assertEquals(MAX_SIZE, target.size());
    }

    @Test
    void testOfferNotIncreasesSizeMoreThanMax() {
        for (int i = 0; i < MAX_SIZE * 2; i++) {
            target.offer(i);
        }

        assertEquals(MAX_SIZE, target.size());
    }

    @Test
    void testAddLastNotIncreasesSizeMoreThanMax() {
        for (int i = 0; i < MAX_SIZE * 2; i++) {
            target.addLast(i);
        }

        assertEquals(MAX_SIZE, target.size());
    }

    @Test
    void testAddAllNotIncreasesSizeMoreThanMax() {
        var list = IntStream.range(0, MAX_SIZE * 2).boxed().toList();

        target.addAll(list);

        assertEquals(MAX_SIZE, target.size());
    }

    @Test
    void testAddOverLimitRemovesFirstElement() {
        var list = IntStream.range(0, MAX_SIZE).boxed().toList();
        var expectedList = IntStream.range(1, MAX_SIZE + 1).boxed().toList();
        target.addAll(list);
        target.add(MAX_SIZE);

        assertEquals(expectedList, target);
    }

    @Test
    void testOfferOverLimitRemovesFirstElement() {
        var list = IntStream.range(0, MAX_SIZE).boxed().toList();
        var expectedList = IntStream.range(1, MAX_SIZE + 1).boxed().toList();
        target.addAll(list);
        target.offer(MAX_SIZE);

        assertEquals(expectedList, target);
    }
}
