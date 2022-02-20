package de.tuberlin.dima.xdbx.user;

public class NoLoginsException extends Exception{
    private User user;

    public NoLoginsException(User user){
        this.user = user;
    }

    public User getUser() {
        return user;
    }
}
