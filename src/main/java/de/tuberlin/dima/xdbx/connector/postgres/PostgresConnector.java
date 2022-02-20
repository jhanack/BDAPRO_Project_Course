package de.tuberlin.dima.xdbx.connector.postgres;

import de.tuberlin.dima.xdbx.client.Client;
import de.tuberlin.dima.xdbx.client.ClientFactory;
import de.tuberlin.dima.xdbx.connector.*;
import de.tuberlin.dima.xdbx.node.XDBConnectionDetails;
import de.tuberlin.dima.xdbx.util.ThrowingConsumer;

import java.sql.*;
import java.time.Instant;
import java.util.*;
import java.util.function.Consumer;

public class PostgresConnector extends ConnectorStub<PostgresConnector> {
    //Would be better if this was managed at a user level in the DBManager or PostgresInstance
    //This would enable sharing a server between multiple Connectors, but it would require making sure there is always
    //one connector left that has the permission to delete the Foreign Server
    //Also, the current approach only allows one-to-one mapping of DB users
    /**
     * Maps the assigned names of foreign servers to all foreign tables that use this server
     */
    private final Map<String, Set<PostgresForeignTable>> foreignTableUsage = new HashMap<>();

    //URI objects don't work well for jdbc connections
    /**
     * Maps URI-like strings to the assigned names of a foreign server
     */
    private final Map<String, String> foreignServerNames = new HashMap<>();


    public PostgresConnector(DBConnectionDetails connectionDetails, ThrowingConsumer<DBConnectionDetails, Exception> deregisterHook, ClientFactory clientFactory) throws SQLException {
        super(connectionDetails, deregisterHook, clientFactory, DefaultDBTypes.POSTGRES);

        Statement stmt = createStatement();
        stmt.execute("CREATE EXTENSION IF NOT EXISTS postgres_fdw");
        stmt.execute("CREATE EXTENSION IF NOT EXISTS jdbc_fdw");
        stmt.execute("CREATE EXTENSION IF NOT EXISTS odbc_fdw");
        stmt.close();
    }

    @Override
    public DBView createViewOnDB(String name, String query) {
        return new PostgresDBView(name, query);
    }

    @Override
    public DBForeignTable createForeignTableOnDB(String name,
                                                 String foreignName,
                                                 DBConnectionDetails foreignDBConnection,
                                                 XDBConnectionDetails xdbConnectionDetails) {
        return new PostgresForeignTable(name, xdbConnectionDetails, foreignDBConnection, foreignName);
    }

    @Override
    public List<ColumnInfo> getColumns(String name) throws SQLException{
        List<ColumnInfo> columns = new LinkedList<>();
        Statement stmt = createStatement();
        String schemaQuery = "SELECT * FROM " + name + " where 1=0";
        //System.out.println("Schema Query:" + schemaQuery);
        ResultSet rs = stmt.executeQuery(schemaQuery);

        ResultSetMetaData rsmd = rs.getMetaData();
        rs.next();

        for (int i = 1; i < rsmd.getColumnCount() + 1; i++) {
            String columnName = rsmd.getColumnName(i);
            String typeName = JDBCType.valueOf(rsmd.getColumnType(i)).getName();
            int width = 0;

            //dirty fixes
            //if (typeName.equals("DECIMAL"))
            //    typeName = "DOUBLE";
            if (typeName.equals("NUMERIC"))
                typeName = "DECIMAL";

            if (typeName.equals("CHAR") || typeName.equals("VARCHAR")) {
                width = 255;
                //TODO smaller than correct, why not larger than
                if (rsmd.getColumnDisplaySize(i) < 255)
                    width = rsmd.getColumnDisplaySize(i);
            }
            columns.add(new ColumnInfo(columnName, typeName, width));
        }

        return columns;
    }


    private class PostgresDBView extends DBObjectStub implements DBView {
        private final String query;

        public PostgresDBView(String assignedName, String query) {
            super(assignedName);
            this.query = query;
        }

        @Override
        public void createOnDB() throws SQLException {
            String dropView = "DROP VIEW IF EXISTS " + getAssignedName() + " CASCADE";
            String localView = "CREATE VIEW " + getAssignedName() + " AS " + query;
            Statement stmt = createStatement();
            stmt.execute(dropView);
            stmt.execute(localView);
            stmt.close();
        }

        @Override
        public void dropOnDB() throws SQLException {
            String dropView = "DROP VIEW IF EXISTS " + getAssignedName() + " CASCADE";
            Statement stmt = createStatement();
            stmt.execute(dropView);
            stmt.close();
        }

        @Override
        public String getQuery() {
            return query;
        }
    }

    private class PostgresForeignTable extends DBObjectStub implements DBForeignTable {
        private final XDBConnectionDetails xdbConnectionDetails;
        private final DBConnectionDetails foreignDBConnectionDetails;
        private final String foreignRequestedName;
        private String foreignServer;


        public PostgresForeignTable(String assignedName, XDBConnectionDetails xdbConnectionDetails, DBConnectionDetails foreignDBConnectionDetails, String foreignName) {
            super(assignedName);
            this.xdbConnectionDetails = xdbConnectionDetails;
            this.foreignDBConnectionDetails = foreignDBConnectionDetails;
            this.foreignRequestedName = foreignName;
        }

        @Override
        public void createOnDB() throws SQLException {
            String foreignTableDrop = "DROP FOREIGN TABLE IF EXISTS " + getAssignedName() + " CASCADE";
            Client client = PostgresConnector.this.getClientFactory().create(xdbConnectionDetails);
            String foreignAssignedName = client.translate(foreignRequestedName);
            List<ColumnInfo> columns = client.getColumns(foreignAssignedName, foreignDBConnectionDetails);
            foreignAssignedName = foreignAssignedName.replace("\"", "");
            DBType foreignDBType = client.getDBType(foreignDBConnectionDetails.getAddress(), foreignDBConnectionDetails.getPort());

            this.foreignServer = PostgresConnector.this.createForeignDBConnection(foreignDBConnectionDetails, foreignDBType, this);

            StringBuilder sb = new StringBuilder();
            sb.append("CREATE FOREIGN TABLE ").append(getAssignedName()).append(" (");

            for (ColumnInfo columnInfo : columns){
                sb.append(columnInfo.getName());
                sb.append(" ");
                sb.append(columnInfo.getType());
                if (columnInfo.getType().equals("CHAR") || columnInfo.getType().equals("VARCHAR")) {
                    sb.append("(");
                    sb.append(columnInfo.getColumnWidth());
                    sb.append(")");
                }
                sb.append(", ");
            }
            sb.replace(sb.length() - 2, sb.length(), ")");
            sb.append(" SERVER " + foreignServer);
            if (foreignDBType.equals(DefaultDBTypes.POSTGRES)){
                sb.append(" OPTIONS (table_name '" + foreignAssignedName + "')");
            } else if(foreignDBType.equals(DefaultDBTypes.HIVE)){
                sb.append(" OPTIONS (query 'SELECT * FROM " + foreignAssignedName + "')");
            } else {
                sb.append(" OPTIONS (odbc_DATABASE '" + foreignDBConnectionDetails.getDatabase() + "',");
                sb.append(" table '" + foreignAssignedName + "')");
            }
            String query = sb.toString();
            Statement stmt = createStatement();
            stmt.executeUpdate(foreignTableDrop);
            stmt.executeUpdate(query);
            stmt.close();

        }

        @Override
        public void dropOnDB() throws SQLException {
            String foreignTableDrop = "DROP FOREIGN TABLE IF EXISTS " + getAssignedName() + " CASCADE";
            Statement stmt = createStatement();
            stmt.execute(foreignTableDrop);
            stmt.close();

            PostgresConnector.this.removeForeignServer(this.foreignServer, this);
        }

        @Override
        public DBConnectionDetails getForeignDBConnection() {
            return this.foreignDBConnectionDetails;
        }
    }

    private String createForeignDBConnection(DBConnectionDetails foreignDB, DBType type, PostgresForeignTable user) throws SQLException {
        Statement stmt = createStatement();
        String uri = type.formatURI(foreignDB.getAddress(), foreignDB.getPort(), foreignDB.getDatabase());
        synchronized (foreignServerNames){
            String assignedName = foreignServerNames.get(uri);
            if (assignedName == null){
                assignedName = "\"" + Instant.now().toEpochMilli() + "_" + foreignDB.hashCode() + "\"";
                foreignServerNames.put(uri, assignedName);

                String dropQuery = "DROP SERVER IF EXISTS " + assignedName + " CASCADE";
                String query = "CREATE SERVER " + assignedName + " FOREIGN DATA WRAPPER ";
                String mapUserQuery;
                if (type == DefaultDBTypes.POSTGRES) {
                    query += "postgres_fdw ";
                    query += "OPTIONS( " +
                            "host '" + foreignDB.getAddress() + "'," +
                            "port '" + foreignDB.getPort() + "'," +
                            "dbname '" + foreignDB.getDatabase() + "'," +
                            "fetch_size '250000'," +
                            "use_remote_estimate 'true'" +
                            //"fdw_startup_cost '0'," +
                            //"fdw_tuple_cost '0'" +
                            ")";
                    mapUserQuery = "CREATE USER MAPPING FOR CURRENT_USER " +
                            "SERVER " + assignedName + " " +
                            "OPTIONS(user '" + foreignDB.getUserName() + "', " +
                            "password '" + foreignDB.getPassword() + "')";
                } else if (type.equals(DefaultDBTypes.HIVE)){
                    query += "jdbc_fdw ";
                    query += "OPTIONS( " +
                            "drivername '" + selectDriver(type) + "'," +
                            "url '" + type.formatURI(foreignDB.getAddress(), foreignDB.getPort(), foreignDB.getDatabase()) + "'," +
                            "querytimeout '360'," +
                            "jarfile '" + selectJDBCDriverJar(type) + "'," +
                            "maxheapsize '5000'" +
                            ")";
                    mapUserQuery = "CREATE USER MAPPING FOR CURRENT_USER " +
                            "SERVER " + assignedName + " " +
                            "OPTIONS(username '" + foreignDB.getUserName() + "', " +
                            "password '" + foreignDB.getPassword() + "')";
                } else {
                    query += "odbc_fdw " +
                            "OPTIONS ( " +
                            "odbc_DRIVER '" + selectODBCDriver(type) + "', " +
                            "odbc_SERVER '" + foreignDB.getAddress() + "'," +
                            "odbc_PORT '" + foreignDB.getPort() + "'" +
                            ");";
                    mapUserQuery = "CREATE USER MAPPING FOR CURRENT_USER " +
                            "SERVER " + assignedName + " " +
                            "OPTIONS(odbc_UID '" + foreignDB.getUserName() + "', " +
                            "odbc_PWD '" + foreignDB.getPassword() + "')";
                }


                stmt.execute(dropQuery);
                stmt.execute(query);
                stmt.execute(mapUserQuery);
                stmt.close();
            }
            foreignTableUsage.computeIfAbsent(assignedName, s -> new HashSet<>()).add(user);
        }
        return foreignServerNames.get(uri);
    }

    private void removeForeignServer(String assignedName, PostgresForeignTable user) throws SQLException {
        Set<PostgresForeignTable> users = foreignTableUsage.get(assignedName);
        if (users.remove(user) && users.isEmpty()) {
            synchronized (foreignServerNames) {
                String dropUserMappingQuery = "DROP USER MAPPING IF EXISTS FOR CURRENT_USER SERVER " + assignedName;
                String dropForeignServerQuery = "DROP SERVER IF EXISTS " + assignedName + " CASCADE";

                Statement stmt = createStatement();
                stmt.execute(dropUserMappingQuery);
                stmt.execute(dropForeignServerQuery);
                stmt.close();
                String uri = foreignServerNames
                        .entrySet().stream()
                        .filter(stringStringEntry -> stringStringEntry.getValue().equals(assignedName))
                        .findFirst().get().getValue();
                foreignServerNames.remove(uri);
            }
        }
    }

    private String selectDriver(DBType type){
        //TODO add all drivers
        if (type == DefaultDBTypes.POSTGRES){
            return "org.postgresql.driver";
        } else if (type == DefaultDBTypes.MARIADB){
            return "org.mariadb.jdbc.Driver";
        } else return null;
    }

    private String selectJDBCDriverJar(DBType type){
        return null;
    }

    private String selectODBCDriver(DBType type){
        if (type.equals(DefaultDBTypes.MARIADB)){
            return "MariaDB";
        } else return null;
    }
}
