package de.tuberlin.dima.xdbx.user;

public class UserNotFoundException extends Exception {
    private final String usernameRequested;

    public UserNotFoundException(String usernameRequested){
        this.usernameRequested = usernameRequested;
    }

    public String getRequestedUsername(){
        return usernameRequested;
    }
}
