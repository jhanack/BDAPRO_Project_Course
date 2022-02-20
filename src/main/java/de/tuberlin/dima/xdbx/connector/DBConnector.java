package de.tuberlin.dima.xdbx.connector;

import de.tuberlin.dima.xdbx.node.XDBConnectionDetails;

import java.sql.SQLException;
import java.util.List;

/**
 * Direct interface to the database, only wraps around what is needed to manage a specific database and is created for every connection.
 * Manages the lifecycle of a {@link java.sql.Connection} and acts as a factory for the {@link DBObject} Interface.
 */
public interface DBConnector {

    DBView addView(String name, String query);

    DBForeignTable addForeignTable(String name, String foreignName, DBConnectionDetails foreignDBConnection, XDBConnectionDetails xdbConnectionDetails);

    List<ColumnInfo> getColumns(String name) throws SQLException;
}
