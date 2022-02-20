package de.tuberlin.dima.xdbx.node;

public interface XDBConnectionDetails {
    String getAddress();
    int getPort();
    String getUsername();
    String getAuthentication();
}
