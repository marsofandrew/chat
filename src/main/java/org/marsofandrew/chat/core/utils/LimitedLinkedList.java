package org.marsofandrew.chat.core.utils;

import java.util.Collection;
import java.util.LinkedList;

public class LimitedLinkedList<E> extends AbstractFramedLinkedList<E> {

    public LimitedLinkedList(Integer limit) {
        super(limit);
    }

    protected LimitedLinkedList(Integer limit, Collection<? extends E> c) {
        super(limit, c);
    }

    @Override
    protected void handleOversize(int ign) {
        throw new IndexOutOfBoundsException(); //TODO: refactor
    }
}
