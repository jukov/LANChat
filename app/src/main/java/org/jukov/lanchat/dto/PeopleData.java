package org.jukov.lanchat.dto;

import android.content.Context;

/**
 * Created by jukov on 22.02.2016.
 */

public class PeopleData extends Data {

    private String uid;

    public PeopleData() {
    }

    public PeopleData(Context context, String uid) {
        super(context);
        this.uid = uid;
    }

    public PeopleData(String name, String uid) {
        setName(name);
        this.uid = uid;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    @Override
    public String toString() {
        return getName();
    }

    @Override
    public boolean equals(Object o) {
        if (o.getClass().getName().equals(PeopleData.class.getName()))
            return uid.equals(((PeopleData) o).getUid());
        else
            return super.equals(o);
    }
}
