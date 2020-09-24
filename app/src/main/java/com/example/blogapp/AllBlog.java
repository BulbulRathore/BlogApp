package com.example.blogapp;

public class AllBlog {

    String user_id;
    String blog_title;
    String blog_desc;
    String blog_image;
    String blog_id;
    long date;

    public AllBlog(){}

    public AllBlog(String user_id, String blog_title, String blog_desc, String blog_image, long date,String blog_id) {
        this.user_id = user_id;
        this.blog_title = blog_title;
        this.blog_desc = blog_desc;
        this.blog_image = blog_image;
        this.date = date;
        this.blog_id = blog_id;
    }

    public String getBlog_id() {
        return blog_id;
    }

    public void setBlog_id(String blog_id) {
        this.blog_id = blog_id;
    }

    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }

    public String getBlog_title() {
        return blog_title;
    }

    public void setBlog_title(String blog_title) {
        this.blog_title = blog_title;
    }

    public String getBlog_desc() {
        return blog_desc;
    }

    public void setBlog_desc(String blog_desc) {
        this.blog_desc = blog_desc;
    }

    public String getBlog_image() {
        return blog_image;
    }

    public void setBlog_image(String blog_image) {
        this.blog_image = blog_image;
    }

    public long getDate() {
        return date;
    }

    public void setDate(long date) {
        this.date = date;
    }
}
