package de.tuberlin.dima.xdbx.connector;

import java.util.Objects;

public class JDBCConnectionDetails implements DBConnectionDetails{
    private final String address;
    private final int port;
    private final String username;
    private final String password;
    private final String database;


    public JDBCConnectionDetails(String address, int port, String username, String password, String database) {
        this.address = address;
        this.port = port;
        this.username = username;
        this.password = password;
        this.database = database;
    }

    @Override
    public String getAddress(){
        return address;
    }

    @Override
    public int getPort() {
        return port;
    }

    @Override
    public String getUserName() {
        return username;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getDatabase() {
        return database;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        JDBCConnectionDetails that = (JDBCConnectionDetails) o;
        return port == that.port && Objects.equals(address, that.address) && Objects.equals(username, that.username) && Objects.equals(password, that.password) && Objects.equals(database, that.database);
    }

    @Override
    public int hashCode() {
        return Objects.hash(address, port, username, password, database);
    }
}
