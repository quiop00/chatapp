package com.example.chatapp2;

public class Blocklist {
    private boolean blocked;
    private boolean beBlocked;

    public boolean isBlocked() {
        return blocked;
    }

    public void setBlocked(boolean blocked) {
        this.blocked = blocked;
    }

    public boolean isBeBlocked() {
        return beBlocked;
    }

    public void setBeBlocked(boolean beBlocked) {
        this.beBlocked = beBlocked;
    }
}
