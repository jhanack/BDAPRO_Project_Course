package de.tuberlin.dima.xdbx.connector.mariadb;

import de.tuberlin.dima.xdbx.client.ClientFactory;
import de.tuberlin.dima.xdbx.connector.DBConnectionDetails;
import de.tuberlin.dima.xdbx.connector.DBInstance;
import de.tuberlin.dima.xdbx.connector.RemoveReferenceCounter;
import de.tuberlin.dima.xdbx.user.User;
import de.tuberlin.dima.xdbx.util.ThrowingConsumer;
import de.tuberlin.dima.xdbx.util.UnregisterHook;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashSet;
import java.util.Set;

public class MariaDBInstance implements DBInstance<MariaDBConnector>, RemoveReferenceCounter<MariaDBConnector> {
    private final Connection adminConnection;
    private final ClientFactory clientFactory;
    private final Set<MariaDBConnector> connectors = new HashSet<>();

    public MariaDBInstance(Connection adminConnection, ClientFactory clientFactory) throws SQLException {
        this.adminConnection = adminConnection;
        this.clientFactory = clientFactory;

        Statement stmt = this.adminConnection.createStatement();
        stmt.execute("INSTALL SONAME 'ha_connect';");
        stmt.close();
    }

    @Override
    public MariaDBConnector createConnection(DBConnectionDetails connectionDetails, ThrowingConsumer<DBConnectionDetails, Exception> deregisterHook) throws SQLException {
        UnregisterHook<MariaDBConnector, MariaDBInstance> unregisterFromInstanceHook = new UnregisterHook<>(this);
        MariaDBConnector connector = new MariaDBConnector(connectionDetails, deregisterHook.andThen(unregisterFromInstanceHook), clientFactory);
        unregisterFromInstanceHook.setLazyMariaDBConnector(connector);
        connectors.add(connector);
        return connector;
    }

    @Override
    public String computeViewName(String requestedName, User user, DBConnectionDetails dbConnectionDetails) {
        return user.getUID() + "_" + requestedName;
    }

    @Override
    public String computeForeignTableName(String requestedName, String foreignName, User user, DBConnectionDetails dbConnectionDetails, DBConnectionDetails foreignDBConnectionDetails) {
        return user.getUID() + "_" + requestedName;
    }

    @Override
    public void removeReference(MariaDBConnector connector) throws SQLException {
        if (connectors.remove(connector) && connectors.isEmpty()){
            adminConnection.close();
        }

    }

}
