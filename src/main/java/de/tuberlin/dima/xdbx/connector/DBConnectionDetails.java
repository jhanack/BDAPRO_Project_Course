package de.tuberlin.dima.xdbx.connector;

import java.net.URI;
import java.net.URISyntaxException;

public interface DBConnectionDetails {
    String getAddress();
    int getPort();
    String getUserName();
    String getPassword();
    String getDatabase();
}
