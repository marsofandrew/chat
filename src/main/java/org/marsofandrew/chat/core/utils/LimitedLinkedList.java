package org.marsofandrew.chat.core.utils;

import org.marsofandrew.chat.core.utils.exception.OversizeException;

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
    protected void preHandleOversize(int ign) {
        throw new OversizeException(limit);
    }
}
