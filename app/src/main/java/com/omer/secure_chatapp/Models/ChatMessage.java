package com.omer.secure_chatapp.Models;
import java.util.Date;

public class ChatMessage {
    private String senderId,receiverId, message, dateTime;
    private Date date;
    private String convertsationId,conversationName,conversationImage;

    public String getConversationName() {
        return conversationName;
    }

    public void setConversationName(String conversationName) {
        this.conversationName = conversationName;
    }

    public String getConvertsationId() {
        return convertsationId;
    }

    public void setConvertsationId(String convertsationId) {
        this.convertsationId = convertsationId;
    }

    public String getConversationImage() {
        return conversationImage;
    }

    public void setConversationImage(String conversationImage) {
        this.conversationImage = conversationImage;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public String getSenderId() {
        return senderId;
    }

    public void setSenderId(String senderId) {
        this.senderId = senderId;
    }

    public String getReceiverId() {
        return receiverId;
    }

    public void setReceiverId(String receiverId) {
        this.receiverId = receiverId;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getDateTime() {
        return dateTime;
    }

    public void setDateTime(String dateTime) {
        this.dateTime = dateTime;
    }
}
