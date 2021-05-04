package com.example.mytaste2.Model;

import java.util.Date;

public class Post extends PostId {

    private String image, caption, user , postId;
    private Date time;

    public Post(){}

    public String getImage() {
        return image;
    }

    public String getCaption() {
        return caption;
    }

    public String getUser() {
        return user;
    }

    public Date getTime() {
        return time;
    }
}
