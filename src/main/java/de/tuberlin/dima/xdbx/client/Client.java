package de.tuberlin.dima.xdbx.client;

import de.tuberlin.dima.xdbx.connector.ColumnInfo;
import de.tuberlin.dima.xdbx.connector.DBConnectionDetails;
import de.tuberlin.dima.xdbx.connector.DBType;
import de.tuberlin.dima.xdbx.node.XDBConnectionDetails;
import de.tuberlin.dima.xdbx.user.Credentials;

import java.util.List;

public interface Client {
    List<ColumnInfo> getColumns(String requestedName, DBConnectionDetails dbConnectionDetails);
    DBType getDBType(String address, int port);

    String translate(String requestedName);

    int createView(String name, String query, DBConnectionDetails dbConnectionDetails);
    int createForeignTable(String localName,
                            String foreignName,
                            XDBConnectionDetails foreignXDBConn,
                            DBConnectionDetails localDBConnection,
                            DBConnectionDetails foreignDBConnection);
    void close();

}
