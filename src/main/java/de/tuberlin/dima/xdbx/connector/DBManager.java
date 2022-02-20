package de.tuberlin.dima.xdbx.connector;

import de.tuberlin.dima.xdbx.node.XDBConnectionDetails;
import de.tuberlin.dima.xdbx.user.User;

import javax.annotation.CheckForNull;
import java.sql.SQLException;
import java.util.List;

/**
 * Manages all foreign tables/views/dbconnections for users
 * A Node also takes a {@link DBConnectionDetails} for the database it should manage since one node might manage a cluster
 * or array of databases.
 * The DBManager may transform the DBConnectionDetails how it sees fit and impose limits on what databases the node can access.
 * It also maintains the translation of the requested vs. assigned names in the database
 */
public interface DBManager {

    void cleanupUser(User user) throws Exception;

    void addForeignTable(String localName,
                         String foreignName,
                         User user,
                         DBConnectionDetails dbConnection,
                         DBConnectionDetails foreignDBConnection,
                         XDBConnectionDetails xdbConnectionDetails) throws SQLException;
    void addView(String localName, String query, User user, DBConnectionDetails dbConnection) throws SQLException;
    String translate(String requestedName, User user) throws NoSuchDBObject;
    List<ColumnInfo> getColumns(String assignedName, DBConnectionDetails connectionDetails) throws SQLException;

    @CheckForNull
    DBType getDBType(String address);

    public static class NoSuchDBObject extends Exception {
        private final String requestedName;
        private final User user;

        public NoSuchDBObject(String requestedName, User user) {
            this.requestedName = requestedName;
            this.user = user;
        }

        public String getRequestedName() {
            return requestedName;
        }

        public User getUser() {
            return user;
        }
    }
}
