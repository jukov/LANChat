package org.jukov.lanchat.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.GregorianCalendar;

/**
 * Created by jukov on 09.02.2016.
 */
public class MessageDTO {

    @JsonProperty("Author")
    private String author;

    @JsonProperty("Text")
    private String text;

    @JsonProperty("Date")
    private String sendDate;

    public MessageDTO() {
    }

    public MessageDTO(String author, String text) {
        this.author = author;
        this.text = text;
        sendDate = new GregorianCalendar().toString();
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
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
