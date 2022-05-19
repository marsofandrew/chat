package org.marsofandrew.chat.core.utils;

import java.util.Collection;

public class FramedQueue<E> extends AbstractFramedLinkedList<E> {


    public FramedQueue(Integer limit) {
        super(limit);
    }

    protected FramedQueue(Integer limit, Collection<? extends E> c) {
        super(limit, c);
    }

    @Override
    protected void handleOversize(int oversizeAmount) {
       removeRange(0, oversizeAmount);
    }
}
