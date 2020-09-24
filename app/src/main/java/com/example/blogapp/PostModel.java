package com.example.blogapp;

import java.util.Map;

public class PostModel {
    private String post_image;
    private String post_title;
    private String post_desc;
    private long date;

    public long getDate() {
        return date;
    }

    public void setDate(long date) {
        this.date = date;
    }

    public PostModel(){}

    public PostModel(String post_image, String post_title, String post_desc,long date) {
        this.post_image = post_image;
        this.post_title = post_title;
        this.post_desc = post_desc;
        this.date = date;
    }

    public String getPost_image() {
        return post_image;
    }

    public void setPost_image(String post_image) {
        this.post_image = post_image;
    }

    public String getPost_title() {
        return post_title;
    }

    public void setPost_title(String post_title) {
        this.post_title = post_title;
    }

    public String getPost_desc() {
        return post_desc;
    }

    public void setPost_desc(String post_desc) {
        this.post_desc = post_desc;
    }

}
