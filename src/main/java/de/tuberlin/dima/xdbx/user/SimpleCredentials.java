package de.tuberlin.dima.xdbx.user;

public class SimpleCredentials implements Credentials {
    private final String username;
    private final String password;

    public SimpleCredentials(String username, String password) {
        this.username = username;
        this.password = password;
    }


    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public String getPasswort() {
        return password;
    }
}
