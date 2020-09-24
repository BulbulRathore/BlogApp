package com.example.blogapp;

public class CommentModel {

    String userId;
    String comment;
    long date;

    public CommentModel(String userId, String comment, long date) {
        this.userId = userId;
        this.comment = comment;
        this.date = date;
    }

    public CommentModel(){}

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public long getDate() {
        return date;
    }

    public void setDate(long date) {
        this.date = date;
    }
}
