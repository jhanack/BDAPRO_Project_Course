package de.tuberlin.dima.xdbx.connector.postgres;

import de.tuberlin.dima.xdbx.client.ClientFactory;
import de.tuberlin.dima.xdbx.connector.DBConnectionDetails;
import de.tuberlin.dima.xdbx.connector.DBInstance;
import de.tuberlin.dima.xdbx.user.User;
import de.tuberlin.dima.xdbx.util.ThrowingConsumer;

import java.sql.SQLException;
import java.util.function.Consumer;

public class PostgresInstance implements DBInstance<PostgresConnector> {
    //TODO add Injection/Config system for these values
    private final ClientFactory clientFactory;

    public PostgresInstance(ClientFactory clientFactory) {
        this.clientFactory = clientFactory;
    }

    @Override
    public PostgresConnector createConnection(DBConnectionDetails connectionDetails, ThrowingConsumer<DBConnectionDetails, Exception> deregisterHook) throws SQLException {
        PostgresConnector connector = new PostgresConnector(connectionDetails, deregisterHook, clientFactory);
        return connector;
    }

    @Override
    public String computeViewName(String requestedName, User user, DBConnectionDetails dbConnectionDetails) {
        return "\"" + user.getUID() + "_" + requestedName + "\"";
    }

    @Override
    public String computeForeignTableName(String requestedName, String foreignName, User user, DBConnectionDetails dbConnectionDetails, DBConnectionDetails foreignDBConnectionDetails) {
        return "\"" + user.getUID() + "_" + requestedName + "\"";
    }


}
