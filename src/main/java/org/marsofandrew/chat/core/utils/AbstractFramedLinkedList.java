package org.marsofandrew.chat.core.utils;

import java.util.Collection;
import java.util.LinkedList;

public abstract class AbstractFramedLinkedList<E> extends LinkedList<E> {
    protected final Integer limit;

    @Override
    public void addFirst(E e) {
        runUsualCheck();
        super.addFirst(e);
    }

    @Override
    public void addLast(E e) {
        runUsualCheck();
        super.addLast(e);
    }

    @Override
    public boolean add(E e) {
        runUsualCheck();
        return super.add(e);
    }

    @Override
    public boolean addAll(Collection<? extends E> c) {
        if ((limit != null) && (size() + c.size() > limit)) {
            handleOversize(c.size());
        }
        return super.addAll(c);
    }

    @Override
    public boolean addAll(int index, Collection<? extends E> c) {
        if ((limit != null) && (index + c.size() > limit)) {
            handleOversize(c.size() + index - 1);
        }
        return super.addAll(index, c);
    }

    @Override
    public void add(int index, E element) {
        if ((limit != null) && (index >= limit)) {
            handleOversize(1);
        }
        super.add(index, element);
    }

    @Override
    public boolean offer(E e) {
        runUsualCheck();
        return super.offer(e);
    }

    @Override
    public boolean offerFirst(E e) {
        runUsualCheck();
        return super.offerFirst(e);
    }

    @Override
    public boolean offerLast(E e) {
        runUsualCheck();
        return super.offerLast(e);
    }

    @Override
    public void push(E e) {
        runUsualCheck();
        super.push(e);
    }

    public AbstractFramedLinkedList(Integer limit) {
        this.limit = limit;
    }

    protected AbstractFramedLinkedList(Integer limit, Collection<? extends E> c) {
        this(limit);
        addAll(c);
    }

    protected final void runUsualCheck() {
        if ((limit != null) && size() >= limit) {
            handleOversize(1);
        }
    }

    protected abstract void handleOversize(int oversizeAmount);
}
