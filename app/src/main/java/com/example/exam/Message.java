package com.example.exam;

import java.util.Date;

public class Message {
    public String userName;
    public String textMessage;
    public long messageTime;

    public Message(){}
    public Message(String UserName, String TextMessage){
        this.userName = UserName;
        this.textMessage = TextMessage;
        this.messageTime = new Date().getTime();


    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getTextMessage() {
        return textMessage;
    }


    public void setTextMessage(String textMessage) {
        this.textMessage = textMessage;
    }


    public Long getMessageTime() {
        return messageTime;
    }


    public void setMessageTime(Long messageTime) {
        this.messageTime = messageTime;
    }

}
