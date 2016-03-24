package org.jukov.lanchat.dto;

import java.util.ArrayDeque;
import java.util.Collection;

/**
 * Created by jukov on 22.03.2016.
 */
public class DataBundle<E extends Data> extends ArrayDeque<E> {

    private int maxCapacity;

    public DataBundle(int maxCapacity) {
        this.maxCapacity = maxCapacity;
    }

    public DataBundle(Collection<? extends E> c, int maxCapacity) {
        super(c);
        this.maxCapacity = maxCapacity;
    }

    public DataBundle(int numElements, int maxCapacity) {
        super(numElements);
        this.maxCapacity = maxCapacity;
    }

    @Override
    public boolean add(E object) {
        if (size() >= maxCapacity) {
            removeFirst();
        }
        return super.add(object);
    }
}
