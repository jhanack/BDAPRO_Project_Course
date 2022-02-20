package de.tuberlin.dima.xdbx.connector;

public interface DBType {
    String getName();
    String formatURI(String host, int port, String db);
}
