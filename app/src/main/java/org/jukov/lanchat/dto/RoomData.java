package org.jukov.lanchat.dto;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.List;

/**
 * Created by jukov on 24.04.2016.
 */
public class RoomData extends MessagingData {

    private List<String> participantUIDs;

    public RoomData(String name, String uid) {
        this(name, uid, null);
    }

    public RoomData(String name, String uid, List<String> participantUIDs) {
        setName(name);
        setUid(uid);
        setParticipantUIDs(participantUIDs);
    }

    public RoomData() {
    }

    public List<String> getParticipantUIDs() {
        return participantUIDs;
    }

    public void setParticipantUIDs(List<String> participantUIDs) {
        this.participantUIDs = participantUIDs;
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
            return new RoomData[size];
        }
    };

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeList(participantUIDs);
    }

    private RoomData(Parcel parcel) {
        super(parcel);
        setParticipantUIDs(parcel.readArrayList(null));
    }

}
