package de.tuberlin.dima.xdbx.user;

import javax.annotation.Nonnull;

public interface UserManager {

    boolean authenticateUser(Credentials credentials);
    @Nonnull User getUser(String username) throws UserNotFoundException;

    void stop();



}
