package de.tuberlin.dima.xdbx.user;

import de.tuberlin.dima.xdbx.connector.DBManager;

import javax.annotation.Nonnull;
import java.sql.SQLException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class SimpleUserManager implements UserManager {

    private static final int TIME_TO_LIVE_SECONDS = 60;

    private final Map<User, String> usersWithPasswords = new HashMap<>();
    private final Map<String, User> registeredUsers = new HashMap<>();
    private final Deque<User> loggedInUsers = new LinkedList<>();
    private final AtomicInteger currentUID;

    private final DBManager dbManager;
    private final CleanupRunnable cleaner;
    private final Thread cleanupThread;

    public SimpleUserManager(Map<User, String> usersWithPasswords, DBManager dbManager){
        this.dbManager = dbManager;

        currentUID = new AtomicInteger(0);
        for (Map.Entry<User, String> entry: usersWithPasswords.entrySet()){
            User oldUser = entry.getKey();
            String password = entry.getValue();
            User newUser = new SimpleUser(oldUser.getName(), oldUser.getUID(), this::loginUser, this::logoutUser);
            this.usersWithPasswords.put(newUser, password);
            this.registeredUsers.put(newUser.getName(), newUser);
            if (newUser.getUID() > currentUID.get()) currentUID.set(newUser.getUID());
        }
        cleaner = new CleanupRunnable();
        cleanupThread = new Thread(cleaner);
        cleanupThread.start();
        Runtime.getRuntime().addShutdownHook(new Thread(this::stop));
    }

    @Override
    public boolean authenticateUser(Credentials credentials) {
        try {
            User user = getUser(credentials.getUsername());
            return usersWithPasswords.get(user).equals(credentials.getPasswort());
        } catch (UserNotFoundException e){
            return false;
        }
    }

    @Override
    public @Nonnull User getUser(String username) throws UserNotFoundException{
        User user = registeredUsers.get(username);
        if (user == null){
            throw new UserNotFoundException(username);
        }

        return user;
    }

    @Override
    public void stop() {
        cleaner.stop();
    }

    private void loginUser(User user) {
        loggedInUsers.remove(user);
        loggedInUsers.addLast(user);
    }

    private boolean logoutUser(User user) throws Exception {
        boolean loggedOut = loggedInUsers.remove(user);
        dbManager.cleanupUser(user);
        return loggedOut;
    }

    private class CleanupRunnable implements Runnable{
        private AtomicBoolean running = new AtomicBoolean(true);

        @Override
        public void run() {
            while(running.get()){
                User oldestUser = loggedInUsers.peek();

                //Logout all users that are exceeding the TTL
                while (oldestUser != null) {
                    try {
                        if (Instant.now().minus(TIME_TO_LIVE_SECONDS, ChronoUnit.SECONDS).isAfter(oldestUser.getLastLoginTime())) {
                            try {
                                oldestUser.logout();
                            } catch (Exception e) {
                                System.out.println("Something went wrong when logging out a user: " + e.getMessage());
                            }
                            oldestUser = loggedInUsers.peek();
                        } else {
                            oldestUser = null;
                        }
                    } catch (NoLoginsException e) {
                        System.out.println("Something went wrong, supposedly logged in user \"" + oldestUser.getName() + "\" had no login time");
                    }
                }

                //Wait for one second before trying again
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }

        public void stop(){
            running.set(false);
        }

    }
}
