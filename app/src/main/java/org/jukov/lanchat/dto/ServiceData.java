package org.jukov.lanchat.dto;


/**
 * Created by jukov on 07.04.2016.
 */
@SuppressWarnings("unused")
public class ServiceData extends Data {

    public enum MessageType {
        DELEGATION_SERVER_STATUS(0),
        NEW_NODE_ADDRESS(1);

        private final int value;
        MessageType(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }
    }

    private MessageType messageType;
    private String data;

    public ServiceData() {
    }

    public ServiceData(MessageType messageType) {
        this.messageType = messageType;
        data = null;
    }

    public ServiceData(String data, MessageType messageType) {
        this.data = data;
        this.messageType = messageType;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public MessageType getMessageType() {
        return messageType;
    }

    public void setMessageType(MessageType messageType) {
        this.messageType = messageType;
    }
}
