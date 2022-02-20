package de.tuberlin.dima.xdbx.connector;

import de.tuberlin.dima.xdbx.user.User;

import java.sql.SQLException;

/**
 * Where the SQL statements should actually be send from.
 * These should be created by {@link DBConnector} implementations.
 */
public interface DBObject {
    String getAssignedName();

    void create() throws SQLException;
    void drop() throws Exception;
}
