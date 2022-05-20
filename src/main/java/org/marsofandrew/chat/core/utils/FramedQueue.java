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
    protected void preHandleOversize(int oversizeAmount) {
        if (size() >= oversizeAmount) {
            removeRange(0, oversizeAmount);
        } else {
            clear();
        }
    }

    @Override
    protected void postHandleOversize(int oversizeAmount) {
        removeRange(0, oversizeAmount);
    }
}
