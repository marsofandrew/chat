package org.marsofandrew.chat.core.utils;

import lombok.Getter;

import java.util.Collection;
import java.util.LinkedList;
import java.util.function.BiFunction;
import java.util.function.BooleanSupplier;

public abstract class AbstractFramedLinkedList<E> extends LinkedList<E> {
    @Getter
    protected final Integer limit;

    @Override
    public void addFirst(E e) {
        addSingle(() -> super.addFirst(e));
    }

    @Override
    public void addLast(E e) {
        addSingle(() -> super.addLast(e));
    }

    @Override
    public boolean add(E e) {
        return addSingle(() -> super.add(e));
    }

    @Override
    public boolean addAll(Collection<? extends E> c) {
        return checkedAdd(() -> super.addAll(c), size(), (s, ign) -> s + c.size() - limit, (s, ign) -> s - limit);
    }

    @Override
    public boolean addAll(int index, Collection<? extends E> c) {
        return checkedAdd(() -> super.addAll(index, c), index, (s, ind) -> ind + c.size() - limit, (s, ign) -> s - limit);
    }

    @Override
    public void add(int index, E element) {
        addSingle(() -> super.add(index, element));
    }

    @Override
    public boolean offer(E e) {
        return addSingle(() -> super.offer(e));
    }

    @Override
    public boolean offerFirst(E e) {
        return addSingle(() -> super.offerFirst(e));
    }

    @Override
    public boolean offerLast(E e) {
        return addSingle(() -> super.offerLast(e));
    }

    @Override
    public void push(E e) {
        addSingle(() -> super.push(e));
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
            preHandleOversize(1);
        }
    }

    protected final void addSingle(Runnable action) {
        addSingle(() -> {
            action.run();
            return true;
        });
    }

    protected final boolean addSingle(BooleanSupplier action) {
        return checkedAdd(action, -1, (s, ign) -> s + 1 - limit, (s, ign) -> 0);
    }

    protected abstract void preHandleOversize(int oversizeAmount);

    protected void postHandleOversize(int oversizeAmount) {

    }

    protected final boolean checkedAdd(
            BooleanSupplier action, int addIndex, BiFunction<Integer, Integer, Integer> getPreOversize,
            BiFunction<Integer, Integer, Integer> getPostOversize) {
        if ((limit != null) && (getPreOversize.apply(size(), addIndex) > 0)) {
            preHandleOversize(getPreOversize.apply(size(), addIndex));
        }
        var res = action.getAsBoolean();
        if ((limit != null) && (getPostOversize.apply(size(), addIndex) > 0)) {
            preHandleOversize(getPostOversize.apply(size(), addIndex));
        }
        return res;
    }
}
