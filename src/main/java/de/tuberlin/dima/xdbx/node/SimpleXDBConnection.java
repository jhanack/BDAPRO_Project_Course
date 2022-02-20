package de.tuberlin.dima.xdbx.node;

import de.tuberlin.dima.xdbx.proto.NodeCredentials;
import de.tuberlin.dima.xdbx.proto.XDBConnection;

public class SimpleXDBConnection implements XDBConnectionDetails{
    private final String address;
    private final int port;
    private final String username;
    private final String authentication;

    public SimpleXDBConnection(String address, int port, String username, String authentication) {
        this.address = address;
        this.port = port;
        this.username = username;
        this.authentication = authentication;
    }

    @Override
    public String getAddress() {
        return address;
    }

    @Override
    public int getPort() {
        return port;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public String getAuthentication() {
        return authentication;
    }

    public static XDBConnectionDetails createXDBConnectionDetailsFromGRPC(de.tuberlin.dima.xdbx.proto.XDBConnection xdbConnection){
        return new SimpleXDBConnection(xdbConnection.getAddress(),
                xdbConnection.getPort(),
                xdbConnection.getCredentials().getUser(),
                xdbConnection.getCredentials().getAuthentication());
    }

    public static XDBConnection createGRPCXDBConnectionDetails(XDBConnectionDetails connectionDetails){
        NodeCredentials credentials = NodeCredentials.newBuilder()
                .setUser(connectionDetails.getUsername())
                .setAuthentication(connectionDetails.getAuthentication())
                .build();

        return XDBConnection.newBuilder()
                .setAddress(connectionDetails.getAddress())
                .setPort(connectionDetails.getPort())
                .setCredentials(credentials)
                .build();
    }
}
