package com.example.mytaste2.Model;

import java.util.Date;

public class Comment extends CommentId {

    private String comment ,user;
    private Date time;

    public Comment(){}

    public String getComment() {
        return comment;
    }

    public String getUser() {
        return user;
    }

    public Date getTime() {
        return time;
    }
}
