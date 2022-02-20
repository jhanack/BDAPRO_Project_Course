package de.tuberlin.dima.xdbx.connector.hive;

import de.tuberlin.dima.xdbx.connector.DBConnectionDetails;
import de.tuberlin.dima.xdbx.connector.DBInstance;
import de.tuberlin.dima.xdbx.user.User;
import de.tuberlin.dima.xdbx.util.ThrowingConsumer;

import java.sql.SQLException;
import java.util.function.Consumer;

public class HiveInstance implements DBInstance<HiveConnector> {

    @Override
    public HiveConnector createConnection(DBConnectionDetails connectionDetails, ThrowingConsumer<DBConnectionDetails, Exception> deregisterHook) throws SQLException {
        return null;
    }

    @Override
    public String computeViewName(String requestedName, User user, DBConnectionDetails dbConnectionDetails) {
        return null;
    }

    @Override
    public String computeForeignTableName(String requestedName, String foreignName, User user, DBConnectionDetails dbConnectionDetails, DBConnectionDetails foreignDBConnectionDetails) {
        return null;
    }
}
