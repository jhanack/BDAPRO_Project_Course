package de.tuberlin.dima.xdbx.user;

import java.time.Instant;

public interface User {
    String getName();

    Instant getLastLoginTime() throws NoLoginsException;
    boolean isLoggedIn();

    void login();
    void logout() throws Exception;
    Integer getUID();
}
