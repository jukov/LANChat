package org.jukov.lanchat.dto;

import java.util.List;

/**
 * Created by jukov on 24.02.2016.
 */
public abstract class DataBundle<T> {

    protected List<T> bundle;

    public DataBundle() {
    }

    public DataBundle(List<T> bundle) {
        this.bundle = bundle;
    }

    public List<T> getBundle() {
        return bundle;
    }

    public void setBundle(List<T> bundle) {
        this.bundle = bundle;
    }

    public abstract void addMessage(T data);

}
