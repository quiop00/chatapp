package com.example.chatapp2;

public class Chat {
    private String sender;


    private String receiver;
    private String message;
    private boolean isseen;
    private String image;

    public boolean isIsseen() {
        return isseen;
    }

    public void setIsseen(boolean isseen) {
        this.isseen = isseen;
    }
//    public Chat(String sender, String receiver, String message) {
//        this.sender = sender;
//        this.receiver = receiver;
//        this.message = message;
//    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getReceiver() {
        return receiver;
    }

    public void setReceiver(String receiver) {
        this.receiver = receiver;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

}
