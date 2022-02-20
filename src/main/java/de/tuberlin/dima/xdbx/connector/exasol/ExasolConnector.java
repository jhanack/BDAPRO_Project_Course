package de.tuberlin.dima.xdbx.connector.exasol;

import de.tuberlin.dima.xdbx.client.Client;
import de.tuberlin.dima.xdbx.client.ClientFactory;
import de.tuberlin.dima.xdbx.connector.*;
import de.tuberlin.dima.xdbx.connector.postgres.PostgresConnector;
import de.tuberlin.dima.xdbx.node.XDBConnectionDetails;
import de.tuberlin.dima.xdbx.util.ThrowingConsumer;

import java.sql.*;
import java.time.Instant;
import java.util.*;
import java.util.function.Consumer;

//Doesn't work because Exasol sucks
public class ExasolConnector extends ConnectorStub<ExasolConnector> implements DBConnector {

    /**
     * Maps the assigned names of foreign servers to all foreign tables that use this server
     */
    private final Map<String, Set<ExaForeignTable>> foreignTableUsage = new HashMap<>();

    //URI objects don't work well for jdbc connections
    /**
     * Maps URI-like strings to the assigned names of a foreign server
     */
    private final Map<String, String> foreignServerNames = new HashMap<>();
    public ExasolConnector(DBConnectionDetails connectionDetails, ThrowingConsumer<DBConnectionDetails, Exception> deregisterHook, ClientFactory clientFactory) throws SQLException {
        super(connectionDetails, deregisterHook, clientFactory, DefaultDBTypes.EXASOL);
    }

    @Override
    protected DBView createViewOnDB(String name, String query) {
        return new ExaDBView(name, query);
    }

    @Override
    protected DBForeignTable createForeignTableOnDB(String name, String foreignName, DBConnectionDetails foreignDBConnection, XDBConnectionDetails xdbConnectionDetails) {
        return new ExaForeignTable(name, xdbConnectionDetails, foreignDBConnection, foreignName);
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
    private class ExaDBView extends DBObjectStub implements DBView {
        private final String query;

        public ExaDBView(String assignedName, String query) {
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

    private class ExaForeignTable extends DBObjectStub implements DBForeignTable {
        private final XDBConnectionDetails xdbConnectionDetails;
        private final DBConnectionDetails foreignDBConnectionDetails;
        private final String foreignRequestedName;
        private String foreignServer;


        public ExaForeignTable(String assignedName, XDBConnectionDetails xdbConnectionDetails, DBConnectionDetails foreignDBConnectionDetails, String foreignName) {
            super(assignedName);
            this.xdbConnectionDetails = xdbConnectionDetails;
            this.foreignDBConnectionDetails = foreignDBConnectionDetails;
            this.foreignRequestedName = foreignName;
        }

        @Override
        public void createOnDB() throws SQLException {
            String localSchema = getAssignedName().split("\\.")[0];
            String foreignTableDrop = "DROP VIRTUAL SCHEMA IF EXISTS " + localSchema;
            Client client = ExasolConnector.this.getClientFactory().create(xdbConnectionDetails);
            String foreignAssignedName = client.translate(foreignRequestedName);
            DBType foreignDBType = client.getDBType(foreignDBConnectionDetails.getAddress(), foreignDBConnectionDetails.getPort());

            this.foreignServer = ExasolConnector.this.createForeignDBConnection(foreignDBConnectionDetails, foreignDBType, this);
            String[] schemaAndTable = foreignAssignedName.split("\\.");
            String foreignSchema = null;
            String foreignTable = null;
            if (schemaAndTable.length == 2){
                foreignSchema = schemaAndTable[0];
                foreignTable = schemaAndTable[1];
            } else if (foreignDBType.equals(DefaultDBTypes.POSTGRES)) {
                foreignSchema = "public";
                foreignTable = foreignAssignedName;
            } else if (foreignDBType.equals(DefaultDBTypes.MARIADB)) {
                foreignSchema = foreignDBConnectionDetails.getDatabase();
                foreignTable = foreignAssignedName;
            }

            StringBuilder sb = new StringBuilder();
            sb.append("CREATE VIRTUAL SCHEMA ").append(localSchema)
                    .append(" USING ADAPTER.jdbc_adapter WITH CONNECTION_NAME= '")
                    .append(foreignServer)
                    .append("' SCHEMA_NAME= '")
                    .append(foreignSchema)
                    .append("'");
            if (foreignDBType.equals(DefaultDBTypes.POSTGRES)){
                sb.append(" POSTGRESQL_IDENTIFIER_MAPPING= 'PRESERVE_ORIGINAL_CASE'");
            }
            sb.append(";");
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

            ExasolConnector.this.removeForeignServer(this.foreignServer, this);
        }

        @Override
        public DBConnectionDetails getForeignDBConnection() {
            return this.foreignDBConnectionDetails;
        }
    }

    private String createForeignDBConnection(DBConnectionDetails foreignDB, DBType type, ExaForeignTable usingTable) throws SQLException {
        Statement stmt = createStatement();
        String uri = type.formatURI(foreignDB.getAddress(), foreignDB.getPort(), foreignDB.getDatabase());
        synchronized (foreignServerNames){
            String assignedName = foreignServerNames.get(uri);
            if (assignedName == null){
                assignedName = "\"" + Instant.now().toEpochMilli() + "_" + foreignDB.hashCode() + "\"";
                foreignServerNames.put(uri, assignedName);

                String dropQuery = "DROP CONNECTION IF EXISTS " + assignedName;
                StringBuilder queryBuilder = new StringBuilder("CREATE CONNECTION " + assignedName + " TO '");
                queryBuilder.append(type.formatURI(foreignDB.getAddress(), foreignDB.getPort(), foreignDB.getDatabase()))
                        .append("' USER '")
                        .append(foreignDB.getUserName())
                        .append("' IDENTIFIED BY '")
                        .append(foreignDB.getPassword())
                        .append("';");

                stmt.execute(dropQuery);
                stmt.execute(queryBuilder.toString());
                stmt.close();
            }
            foreignTableUsage.computeIfAbsent(assignedName, s -> new HashSet<>()).add(usingTable);
        }
        return foreignServerNames.get(uri);
    }

    private void removeForeignServer(String assignedName, ExaForeignTable usingTables) throws SQLException {
        Set<ExaForeignTable> users = foreignTableUsage.get(assignedName);
        if (users.remove(usingTables) && users.isEmpty()) {
            synchronized (foreignServerNames) {
                String dropForeignServerQuery = "DROP CONNECTION IF EXISTS " + assignedName;

                Statement stmt = createStatement();
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
}
