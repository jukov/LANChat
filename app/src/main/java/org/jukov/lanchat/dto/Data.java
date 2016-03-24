package org.jukov.lanchat.dto;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.v7.preference.PreferenceManager;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import org.jukov.lanchat.R;
import org.jukov.lanchat.util.Utils;

/**
 * Created by jukov on 22.02.2016.
 */
@JsonTypeInfo(
    use = JsonTypeInfo.Id.CLASS,
    include = JsonTypeInfo.As.PROPERTY,
    property = "type"
)
@JsonSubTypes({
    @JsonSubTypes.Type(value = PeopleData.class, name = "PeopleData"),
    @JsonSubTypes.Type(value = ChatData.class, name = "ChatData")
})
public abstract class Data implements Parcelable {

    private String name;
    private String uid;

    public Data() {
    }

    public Data(Context context) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        name = sharedPreferences.getString("name", context.getString(R.string.default_name));
        uid = Utils.getAndroidID(context);
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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

    protected Data(Parcel parcel) {
        name = parcel.readString();
        uid = parcel.readString();
    }

}
