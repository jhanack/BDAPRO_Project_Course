package de.tuberlin.dima.xdbx.connector;


import de.tuberlin.dima.xdbx.client.ClientFactory;
import de.tuberlin.dima.xdbx.user.User;
import de.tuberlin.dima.xdbx.util.ThrowingConsumer;

import java.sql.SQLException;
import java.util.function.Consumer;

/**
 * Supplies the {@link DBConnector} Implementations for a database type
 * @param <T>
 */
public interface DBInstance<T extends DBConnector> {
    T createConnection(DBConnectionDetails connectionDetails, ThrowingConsumer<DBConnectionDetails, Exception> deregisterHook) throws SQLException;
    String computeViewName(String requestedName, User user, DBConnectionDetails dbConnectionDetails);
    String computeForeignTableName(String requestedName, String foreignName, User user, DBConnectionDetails dbConnectionDetails, DBConnectionDetails foreignDBConnectionDetails);

}
