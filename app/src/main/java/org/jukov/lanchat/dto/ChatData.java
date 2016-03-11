package org.jukov.lanchat.dto;

import android.content.Context;

import org.jukov.lanchat.service.ServiceHelper;

import java.util.Date;

/**
 * Created by jukov on 09.02.2016.
 */
public class ChatData extends Data {

    private String text;
    private String sendDate;
    private ServiceHelper.MessageType messageType;

    public ChatData() {
    }

    public ChatData(Context context, String text, ServiceHelper.MessageType messageType) {
        super(context);
        this.text = text;
        this.messageType = messageType;
        sendDate = new Date().toString();
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

}
