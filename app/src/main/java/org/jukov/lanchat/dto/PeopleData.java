package org.jukov.lanchat.dto;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.os.Parcel;
import android.os.Parcelable;

import com.fasterxml.jackson.annotation.JsonIgnore;

import org.jukov.lanchat.util.Base64Converter;

import java.io.File;

/**
 * Created by jukov on 22.02.2016.
 */

public class PeopleData extends MessagingData {

    //TODO: transform constants to enum
    public enum ActionType {
        NONE(0),
        CONNECT(1),
        DISCONNECT(2),
        CHANGE_PROFILE(3);
        private final int value;

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
                    return CHANGE_PROFILE;
            }
            return null;
        }
    }

    @JsonIgnore
    private Bitmap profilePicture;

    private ActionType actionType;
    private String encodedProfilePicture;

    public PeopleData() {
    }

    public PeopleData(Context context) {
        super(context);
        setAction(ActionType.NONE);

        File pictureFile = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator +
                "Android" + File.separator +
                "data" + File.separator +
                context.getPackageName() + File.separator +
                "files" + File.separator +
                "profile_pictures" + File.separator +
                "profile_picture.jpg");
        if (pictureFile.exists() && !pictureFile.isDirectory()) {
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inPreferredConfig = Bitmap.Config.ARGB_8888;
            profilePicture = BitmapFactory.decodeFile(Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator +
                    "Android" + File.separator +
                    "data" + File.separator +
                    context.getPackageName() + File.separator +
                    "files" + File.separator +
                    "profile_pictures" + File.separator +
                    "profile_picture.jpg", options);

            encodedProfilePicture = Base64Converter.bitmapToString(profilePicture);
        }
    }

    public PeopleData(String name, String uid) {
        this(name, uid, ActionType.NONE);
    }

    public PeopleData(String name, String uid, ActionType actionType) {
        setName(name);
        setUid(uid);
        setAction(actionType);
    }

    public PeopleData(String name, String uid, ActionType actionType, Bitmap bitmap) {
        this(name, uid, actionType);
        profilePicture = bitmap;
        encodedProfilePicture = Base64Converter.bitmapToString(bitmap);
    }

    public void tryCreateBitmap() {
        if (encodedProfilePicture != null)
            profilePicture = Base64Converter.getBitmapFromString(encodedProfilePicture);
    }

    public ActionType getAction() {
        return actionType;
    }

    public void setAction(ActionType actionType) {
        this.actionType = actionType;
    }

    public Bitmap getProfilePicture() {
        return profilePicture;
    }

    public void setProfilePicture(Bitmap profilePicture) {
        this.profilePicture = profilePicture;
    }

    public String getEncodedProfilePicture() {
        return encodedProfilePicture;
    }

    public void setEncodedProfilePicture(String encodedProfilePicture) {
        this.encodedProfilePicture = encodedProfilePicture;
    }

    @Override
    public String toString() {
        return getName();
    }

    @SuppressWarnings("CanBeFinal")
    public static Parcelable.Creator<PeopleData> CREATOR = new Parcelable.Creator<PeopleData>() {
        @Override
        public PeopleData createFromParcel(Parcel source) {
            return new PeopleData(source);
        }

        @Override
        public PeopleData[] newArray(int size) {
            return new PeopleData[0];
        }
    };

    @Override
    public int describeContents() {
        return 2;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeInt(actionType.getValue());
        dest.writeParcelable(profilePicture, flags);
        dest.writeString(encodedProfilePicture);
    }

    private PeopleData(Parcel parcel) {
        super(parcel);
        setAction(ActionType.fromInt(parcel.readInt()));
        setProfilePicture((Bitmap) parcel.readParcelable(Bitmap.class.getClassLoader()));
        setEncodedProfilePicture(parcel.readString());
    }
}
