package com.example.chatapp2;
/*
    class Chatlist include:
    -id
    -isseen: check if message which you had sent, have been seen by another user
    -receiver:equal userid
*/
public class Chatlist {
    private  String id;
    private  boolean isseen;
    private String receiver;
    public String getReceiver() {
        return receiver;
    }

    public void setReceiver(String receiver) {
        this.receiver = receiver;
    }



    public boolean isIsseen() {
        return isseen;
    }

    public void setIsseen(boolean isseen) {
        this.isseen = isseen;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
