package org.jukov.lanchat.dto;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by jukov on 22.02.2016.
 */

public class PeopleData extends MessagingData {

    //TODO: transform constants to enum
    public enum ActionType {
        NONE(0),
        CONNECT(1),
        DISCONNECT(2),
        CHANGE_NAME(3);
        private int value;

        ActionType(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }

        public static ActionType fromInt(int input) {
            switch (input) {
                case 0:
                    return NONE;
                case 1:
                    return CONNECT;
                case 2:
                    return DISCONNECT;
                case 3:
                    return CHANGE_NAME;
            }
            return null;
        }
    }

    private ActionType actionType;

    public PeopleData() {
    }

    public PeopleData(Context context, ActionType actionType) {
        super(context);
        setAction(actionType);
    }

    public PeopleData(String name, String uid) {
        this(name, uid, ActionType.NONE);
    }

    public PeopleData(String name, String uid, ActionType actionType) {
        setName(name);
        setUid(uid);
        setAction(actionType);
    }

    public ActionType getAction() {
        return actionType;
    }

    public void setAction(ActionType actionType) {
        this.actionType = actionType;
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

    public static Parcelable.Creator<? extends MessagingData> CREATOR = new Parcelable.Creator<PeopleData>() {
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
