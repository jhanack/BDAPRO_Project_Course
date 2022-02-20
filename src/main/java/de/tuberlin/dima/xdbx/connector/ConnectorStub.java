package de.tuberlin.dima.xdbx.connector;

import de.tuberlin.dima.xdbx.client.ClientFactory;
import de.tuberlin.dima.xdbx.node.XDBConnectionDetails;
import de.tuberlin.dima.xdbx.util.ThrowingConsumer;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;
import java.util.function.Consumer;

public abstract class ConnectorStub<T extends DBConnector> implements DBConnector, RemoveReferenceCounter<DBObject>{
    private final DBConnectionDetails connectionDetails;
    private final Connection connection;
    private final ThrowingConsumer<DBConnectionDetails, Exception> deregisterHook;
    private final Set<DBObject> created = new HashSet<>();
    private final ClientFactory clientFactory;

    public ConnectorStub(DBConnectionDetails connectionDetails,
                         ThrowingConsumer<DBConnectionDetails, Exception> deregisterHook,
                         ClientFactory clientFactory,
                         DBType dbType) throws SQLException {
        this.connectionDetails = connectionDetails;
        this.deregisterHook = deregisterHook;
        this.clientFactory = clientFactory;

        Properties properties = new Properties();
        properties.setProperty("user", connectionDetails.getUserName());
        properties.setProperty("password", connectionDetails.getPassword());
        String url = dbType.formatURI(connectionDetails.getAddress(), connectionDetails.getPort(), connectionDetails.getDatabase());
        this.connection = DriverManager.getConnection(url, properties);
    }

    protected abstract DBView createViewOnDB(String name, String query);
    protected abstract DBForeignTable createForeignTableOnDB(String name,
                                                           String foreignName,
                                                           DBConnectionDetails foreignDBConnection,
                                                           XDBConnectionDetails xdbConnectionDetails);

    @Override
    public DBView addView(String name, String query) {
        DBView view = createViewOnDB(name, query);
        created.add(view);
        return view;
    }

    @Override
    public DBForeignTable addForeignTable(String name, String foreignName, DBConnectionDetails foreignDBConnection, XDBConnectionDetails xdbConnectionDetails) {
        DBForeignTable foreignTable = createForeignTableOnDB(name, foreignName, foreignDBConnection, xdbConnectionDetails);
        created.add(foreignTable);
        return foreignTable;
    }

    @Override
    public void removeReference(DBObject object) throws Exception {
        if (created.remove(object) && created.isEmpty()){
            this.deregisterHook.accept(this.connectionDetails);
            this.connection.close();
        }
    }

    protected DBConnectionDetails getConnectionDetails() {
        return connectionDetails;
    }

    //protected Connection getConnection() { return connection; }

    protected Statement createStatement() throws SQLException {
        return connection.createStatement();
    }

    protected ThrowingConsumer<DBConnectionDetails, Exception> getDeregisterHook() {
        return deregisterHook;
    }

    protected Set<DBObject> getCreated() {
        return created;
    }

    protected ClientFactory getClientFactory() {
        return clientFactory;
    }

    /**
     * Stub that can be used for implementing DBObjects.
     * This stub may change in the future to implement features that all DBObject implementations should share.
     */
    public abstract class DBObjectStub implements DBObject{
        private final String assignedName;

        public DBObjectStub(String assignedName) {
            this.assignedName = assignedName;
        }

        @Override
        public void create() throws SQLException {
            createOnDB();
        }

        @Override
        public void drop() throws Exception {
            dropOnDB();
            removeReference(this);
        }

        protected abstract void createOnDB() throws SQLException;
        protected abstract void dropOnDB() throws SQLException;



        @Override
        public String getAssignedName() {
            return assignedName;
        }
    }
}
