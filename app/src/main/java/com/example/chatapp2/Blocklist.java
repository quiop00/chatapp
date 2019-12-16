package com.example.chatapp2;
/*create class Blocklist include :
   -beblocked- current user have been block by another user
   -block -current user block another user*/
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
