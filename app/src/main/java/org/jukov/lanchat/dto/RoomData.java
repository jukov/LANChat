package org.jukov.lanchat.dto;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by jukov on 24.04.2016.
 */
public class RoomData extends MessagingData {

    private List<PeopleData> participants;

    public RoomData(String name, String uid) {
        this(name, uid, null);
    }

    public RoomData(String name, String uid, List<PeopleData> participantUIDs) {
        setName(name);
        setUid(uid);
        setParticipants(participantUIDs);
    }

    public RoomData() {
    }

    public List<PeopleData> getParticipants() {
        return participants;
    }

    public void setParticipants(List<PeopleData> participants) {
        this.participants = participants;
    }

    @Override
    public String toString() {
        return getName();
    }

    public static Parcelable.Creator<RoomData> CREATOR = new Parcelable.Creator<RoomData>() {
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
        dest.writeTypedList(participants);
    }

    private RoomData(Parcel parcel) {
        super(parcel);
        List<PeopleData> participants = new ArrayList<>();
        parcel.readTypedList(participants, PeopleData.CREATOR);
        setParticipants(participants);
    }

}
