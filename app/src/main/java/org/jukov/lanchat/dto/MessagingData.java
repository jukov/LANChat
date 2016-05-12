package org.jukov.lanchat.dto;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.v7.preference.PreferenceManager;

import com.fasterxml.jackson.annotation.JsonSubTypes;

import org.jukov.lanchat.R;
import org.jukov.lanchat.util.Utils;

/**
 * Created by jukov on 22.02.2016.
 */
@JsonSubTypes({
        @JsonSubTypes.Type(value = PeopleData.class, name = "PeopleData"),
        @JsonSubTypes.Type(value = ChatData.class, name = "ChatData"),
        @JsonSubTypes.Type(value = RoomData.class, name = "RoomData")
})
@SuppressWarnings("unused")
public abstract class MessagingData extends Data implements Parcelable {

    private String name;
    private String uid;

    public MessagingData() {
    }

    public MessagingData(Context context) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        name = sharedPreferences.getString("name", context.getString(R.string.default_name));
        uid = Utils.getAndroidID(context);
    }

    public String getUid() {
        return uid;
    }

    void setUid(String uid) {
        this.uid = uid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof MessagingData) {
            return getUid().equals(((MessagingData) o).getUid());
        }
        return super.equals(o);
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(name);
        dest.writeString(uid);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    protected MessagingData(Parcel parcel) {
        name = parcel.readString();
        uid = parcel.readString();
    }

}
