package de.tuberlin.dima.xdbx.client;

import de.tuberlin.dima.xdbx.connector.ConnectionDetailFactory;
import de.tuberlin.dima.xdbx.node.XDBConnectionDetails;
import de.tuberlin.dima.xdbx.proto.DBConnection;

public class SimpleClientFactory implements ClientFactory{
    private final ConnectionDetailFactory<DBConnection> connectionDetailFactory;

    public SimpleClientFactory(ConnectionDetailFactory<DBConnection> connectionDetailFactory) {
        this.connectionDetailFactory = connectionDetailFactory;
    }

    @Override
    public Client create(XDBConnectionDetails xdbConnectionDetails) {
        return new SimpleClient(xdbConnectionDetails, connectionDetailFactory);
    }
}
