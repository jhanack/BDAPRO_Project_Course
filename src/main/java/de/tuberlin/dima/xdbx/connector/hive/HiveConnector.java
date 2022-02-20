package de.tuberlin.dima.xdbx.connector.hive;

import de.tuberlin.dima.xdbx.client.Client;
import de.tuberlin.dima.xdbx.client.ClientFactory;
import de.tuberlin.dima.xdbx.connector.*;
import de.tuberlin.dima.xdbx.node.XDBConnectionDetails;
import de.tuberlin.dima.xdbx.util.ThrowingConsumer;

import java.sql.*;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Consumer;

public class HiveConnector extends ConnectorStub<HiveConnector> {
    public HiveConnector(DBConnectionDetails connectionDetails, ThrowingConsumer<DBConnectionDetails, Exception> deregisterHook, ClientFactory clientFactory) throws SQLException {
        super(connectionDetails, deregisterHook, clientFactory, DefaultDBTypes.HIVE);
    }

    @Override
    protected DBView createViewOnDB(String name, String query) {
        return new HiveDBView(name, query);
    }

    @Override
    protected DBForeignTable createForeignTableOnDB(String name, String foreignName, DBConnectionDetails foreignDBConnection, XDBConnectionDetails xdbConnectionDetails) {
        return new HiveDBForeignTable(name, xdbConnectionDetails, foreignDBConnection, foreignName);
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
            if (typeName.equals("DECIMAL"))
                typeName = "DOUBLE";
            if (typeName.equals("DATE"))
                typeName = "STRING";

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

    private class HiveDBView extends DBObjectStub implements DBView {
        private final String query;

        public HiveDBView(String assignedName, String query) {
            super(assignedName.replace("\"", ""));
            this.query = query;
        }

        @Override
        public void createOnDB() throws SQLException {
            String dropView = "DROP VIEW IF EXISTS " + getAssignedName();
            String localView = "CREATE VIEW " + getAssignedName() + " AS " + query;
            Statement stmt = createStatement();
            stmt.execute(dropView);
            stmt.execute(localView);
            stmt.close();
        }

        @Override
        public void dropOnDB() throws SQLException {
            String dropView = "DROP VIEW IF EXISTS " + getAssignedName();
            Statement stmt = createStatement();
            stmt.execute(dropView);
            stmt.close();
        }

        @Override
        public String getQuery() {
            return query;
        }
    }


    private class HiveDBForeignTable extends DBObjectStub implements DBForeignTable {
        private final XDBConnectionDetails xdbConnectionDetails;
        private final DBConnectionDetails foreignDBConnectionDetails;
        private final String foreignRequestedName;


        public HiveDBForeignTable(String assignedName, XDBConnectionDetails xdbConnectionDetails, DBConnectionDetails foreignDBConnectionDetails, String foreignName) {
            super(assignedName.replace("\"", ""));
            this.xdbConnectionDetails = xdbConnectionDetails;
            this.foreignDBConnectionDetails = foreignDBConnectionDetails;
            this.foreignRequestedName = foreignName;
        }

        @Override
        public void createOnDB() throws SQLException {
            String foreignTableDrop = "DROP TABLE IF EXISTS " + getAssignedName() + " CASCADE";
            Client client = HiveConnector.this.getClientFactory().create(xdbConnectionDetails);
            String foreignAssignedName = client.translate(foreignRequestedName);
            List<ColumnInfo> columns = client.getColumns(foreignAssignedName, foreignDBConnectionDetails);
            DBType foreignDBType = client.getDBType(foreignDBConnectionDetails.getAddress(), foreignDBConnectionDetails.getPort());

            StringBuilder sb = new StringBuilder();
            sb.append("CREATE EXTERNAL TABLE ").append(getAssignedName()).append(" (");

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
            sb.append("STORED BY 'org.apache.hive.storage.jdbc.JdbcStorageHandler'\n")
//                    .append("TBLPROPERTIES (\n")
//                    .append("\"hive.sql.database.type\" = \"" +  + "\",\n")
//                    .append( "\"hive.sql.jdbc.url\" = \"" + fjdbcProperties.getUrl() + "\",\n")
//                    .append("\"hive.sql.dbcp.username\" = \"" + fjdbcProperties.getUser() + "\",\n")
//                    .append("\"hive.sql.dbcp.password\" = \"" + fjdbcProperties.getPassword() + "\",\n")
//                    .append("\"hive.sql.jdbc.driver\" = \"" + fjdbcProperties.getDriverName() + "\",\n")
//                    .append("\"hive.sql.query\" = \"SELECT * FROM " + tableName + "\",\n")
//                    .append("\"hive.sql.jdbc.fetch.size\" = \"200000\",\n")
//                    .append("\"hive.sql.dbcp.maxActive\" = \"1\"\n")
//                    .append(")")
                    .append(";");
            String query = sb.toString();
            Statement stmt = createStatement();
            stmt.executeUpdate(foreignTableDrop);
            stmt.executeUpdate(query);
            stmt.close();

        }

        @Override
        public void dropOnDB() throws SQLException {
            String foreignTableDrop = "DROP TABLE IF EXISTS " + getAssignedName() + " CASCADE";
            Statement stmt = createStatement();
            stmt.execute(foreignTableDrop);
            stmt.close();
        }

        @Override
        public DBConnectionDetails getForeignDBConnection() {
            return this.foreignDBConnectionDetails;
        }
    }

    private static String getDBTypeName(DBType dbType){
        return "";
    }
}
