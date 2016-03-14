package org.jukov.lanchat.dto;

import android.content.Context;

/**
 * Created by jukov on 22.02.2016.
 */

public class PeopleData extends Data {

    public static final int ACTION_NONE = 0;
    public static final int ACTION_CONNECT = 1;
    public static final int ACTION_DISCONNECT = 2;
    public static final int ACTION_CHANGE_NAME = 3;

    private int action;

    public PeopleData() {
    }

    public PeopleData(Context context, String uid, int action) {
        super(context, uid);
        this.action = action;
    }

    public PeopleData(String name, String uid, int action) {
        setName(name);
        this.action = action;
    }

    public int getAction() {
        return action;
    }

    public void setAction(int action) {
        this.action = action;
    }

    @Override
    public String toString() {
        return getName();
    }

    @Override
    public boolean equals(Object o) {
        if (o.getClass().getName().equals(PeopleData.class.getName()))
            return getUid().equals(((PeopleData) o).getUid());
        else
            return super.equals(o);
    }
}
