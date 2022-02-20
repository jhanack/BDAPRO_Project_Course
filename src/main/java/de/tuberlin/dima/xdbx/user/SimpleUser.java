package de.tuberlin.dima.xdbx.user;

import de.tuberlin.dima.xdbx.util.ThrowingConsumer;

import java.time.Instant;
import java.util.function.Consumer;

public class SimpleUser implements User{
    private final String username;
    private final Integer UID;
    private boolean isLoggedIn;
    private Instant lastLoginTime = null;

    private final Consumer<SimpleUser> loginHook;
    private final ThrowingConsumer<SimpleUser, Exception> logoutHook;

    public SimpleUser(String username, Integer UID, Consumer<SimpleUser> loginHook, ThrowingConsumer<SimpleUser, Exception> logoutHook){
        this.username = username;
        this.UID = UID;
        this.loginHook = loginHook;
        this.logoutHook = logoutHook;
    }

    @Override
    public String getName() {
        return username;
    }

    @Override
    public synchronized Instant getLastLoginTime() throws NoLoginsException {
        if (lastLoginTime == null){
            throw new NoLoginsException(this);
        }

        return lastLoginTime;
    }

    @Override
    public synchronized boolean isLoggedIn() {
        return isLoggedIn;
    }

    @Override
    public synchronized void login() {
        isLoggedIn = true;
        lastLoginTime = Instant.now();
        this.loginHook.accept(this);
    }

    @Override
    public synchronized void logout() throws Exception {
        isLoggedIn = false;
        this.logoutHook.accept(this);
    }

    @Override
    public Integer getUID() {
        return this.UID;
    }

}
