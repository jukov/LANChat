package org.jukov.lanchat.dto;

import android.content.Context;

import java.util.Date;

/**
 * Created by jukov on 09.02.2016.
 */
public class ChatData extends Data {

    private String text;

    private String sendDate;

    public ChatData() {
    }

    public ChatData(Context context, String text) {
        super(context);
        this.text = text;
        sendDate = new Date().toString();
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
