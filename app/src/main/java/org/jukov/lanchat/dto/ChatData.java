package org.jukov.lanchat.dto;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;

import org.jukov.lanchat.service.ServiceHelper;

import java.util.Date;

/**
 * Created by jukov on 09.02.2016.
 */
public class ChatData extends MessagingData {

    private String text;
    private String sendDate;
    private String destinationUID;
    private ServiceHelper.MessageType messageType;

    public ChatData() {
    }

    public ChatData(Context context, ServiceHelper.MessageType messageType, String text) {
        super(context);
        this.messageType = messageType;
        this.text = text;
        sendDate = new Date().toString();
    }

    public ChatData(Context context, ServiceHelper.MessageType messageType, String text, String destinationUID) {
        super(context);
        this.messageType = messageType;
        this.text = text;
        this.destinationUID = destinationUID;
        this.sendDate = new Date().toString();
    }

    public ServiceHelper.MessageType getMessageType() {
        return messageType;
    }

    public void setMessageType(ServiceHelper.MessageType messageType) {
        this.messageType = messageType;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getSendDate() {
        return sendDate;
    }

    public void setSendDate(String sendDate) {
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
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeString(text);
        dest.writeString(sendDate);
        dest.writeInt(messageType.getValue());
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
        sendDate = parcel.readString();
        messageType = ServiceHelper.MessageType.values()[parcel.readInt()];
    }
}
