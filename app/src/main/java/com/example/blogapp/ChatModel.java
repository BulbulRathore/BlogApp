package com.example.blogapp;

public class ChatModel {
    private long date;
    private String message;
    private String type;

    public ChatModel(long date, String message, String type) {
        this.date = date;
        this.message = message;
        this.type = type;
    }

    public ChatModel(){}

    public long getDate() {
        return date;
    }

    public void setDate(long date) {
        this.date = date;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
