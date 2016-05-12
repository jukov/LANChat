package org.jukov.lanchat.dto;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import java.util.Date;

/**
 * Created by jukov on 09.02.2016.
 */
@SuppressWarnings({"SameParameterValue", "unused"})
public class ChatData extends MessagingData {

    public static final String TAG = ChatData.class.getSimpleName();

    public enum MessageType {
        PRIVATE(0),
        GLOBAL(1),
        ROOM(2);
        private int value;

        MessageType(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }

        public static MessageType fromInt(int input) {
            switch (input) {
                case 0:
                    return PRIVATE;
                case 1:
                    return GLOBAL;
                case 2:
                    return ROOM;
            }
            return null;
        }
    }
    
    private String text;
    private long sendDate;
    private String destinationUID;
    private MessageType messageType;

    public ChatData() {
    }
    
    public ChatData(Context context, MessageType messageType, String text) {
        super(context);
        this.messageType = messageType;
        this.text = text;
        sendDate = new Date().getTime();
    }

    public ChatData(Context context, MessageType messageType, String text, String destinationUID) {
        super(context);
        this.messageType = messageType;
        this.text = text;
        this.destinationUID = destinationUID;
        this.sendDate = new Date().getTime();
    }

    public ChatData(String name, String uid, MessageType messageType, String text, long sendDate, String destinationUID) {
        setName(name);
        setUid(uid);
        this.text = text;
        this.sendDate = sendDate;
        this.messageType = messageType;
        this.destinationUID = destinationUID;
    }

    public ChatData(String name, String uid, MessageType messageType, String text, long sendDate) {
        this(name, uid, messageType, text, sendDate, null);
    }

    public MessageType getMessageType() {
        return messageType;
    }

    public void setMessageType(MessageType messageType) {
        this.messageType = messageType;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public long getSendDate() {
        return sendDate;
    }

    public void setSendDate(long sendDate) {
        this.sendDate = sendDate;
    }

    public String getDestinationUID() {
        return destinationUID;
    }

    public void setDestinationUID(String destinationUID) {
        this.destinationUID = destinationUID;
    }

    @Override
    public String toString() {
        return getName() + ": " + text;
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof ChatData) {
            ChatData chatData = (ChatData) o;
//            if (chatData.getUid().equals(getUid()) &&
//                    chatData.getText().equals(getText()) &&
//                    chatData.getSendDate() == getSendDate()) {
            Log.d(TAG, Integer.toString(Long.valueOf(chatData.getSendDate()).compareTo(getSendDate())));
            if (chatData.getUid().equals(getUid()))
                if (chatData.getText().equals(getText()))
                    if (Long.valueOf(chatData.getSendDate()).compareTo(getSendDate()) == 0) {
                        return true;
            }
        } else return super.equals(o);
        return false;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeString(text);
        dest.writeLong(sendDate);
        dest.writeInt(messageType.getValue());
        dest.writeString(destinationUID);
    }

    public static Parcelable.Creator<? extends MessagingData> CREATOR = new Parcelable.Creator<ChatData>() {
        @Override
        public ChatData createFromParcel(Parcel source) {
            return new ChatData(source);
        }

        @Override
        public ChatData[] newArray(int size) {
            return new ChatData[0];
        }
    };

    private ChatData(Parcel parcel) {
        super(parcel);
        text = parcel.readString();
        sendDate = parcel.readLong();
        messageType = MessageType.values()[parcel.readInt()];
        destinationUID = parcel.readString();
    }
}
