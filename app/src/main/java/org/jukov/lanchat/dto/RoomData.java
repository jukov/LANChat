package org.jukov.lanchat.dto;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by jukov on 24.04.2016.
 */
public class RoomData extends MessagingData {

    public RoomData(String name, int uid) {
        setName(name);
        setUid(Integer.toString(uid));
    }

    public RoomData() {
    }

    @Override
    public String toString() {
        return getName();
    }

    public static Parcelable.Creator<? extends MessagingData> CREATOR = new Parcelable.Creator<RoomData>() {
        @Override
        public RoomData createFromParcel(Parcel source) {
            return new RoomData(source);
        }

        @Override
        public RoomData[] newArray(int size) {
            return new RoomData[0];
        }
    };

    private RoomData(Parcel parcel) {
        super(parcel);
    }

}
