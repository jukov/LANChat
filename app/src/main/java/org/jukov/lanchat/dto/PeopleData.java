package org.jukov.lanchat.dto;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;

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

    public PeopleData(Context context, int action) {
        super(context);
        this.action = action;
    }

    public PeopleData(String name, String uid, int action) {
        setName(name);
        setUid(uid);
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
        if (o instanceof PeopleData) {
                return getUid().equals(((PeopleData) o).getUid());
        }
        return super.equals(o);
    }

    public static Parcelable.Creator<? extends Data> CREATOR = new Parcelable.Creator<PeopleData>() {
        @Override
        public PeopleData createFromParcel(Parcel source) {
            return new PeopleData(source);
        }

        @Override
        public PeopleData[] newArray(int size) {
            return new PeopleData[0];
        }
    };

    private PeopleData(Parcel parcel) {
        super(parcel);
    }
}
