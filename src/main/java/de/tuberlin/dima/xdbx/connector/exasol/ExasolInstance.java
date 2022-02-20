package de.tuberlin.dima.xdbx.connector.exasol;

import de.tuberlin.dima.xdbx.client.ClientFactory;
import de.tuberlin.dima.xdbx.connector.DBConnectionDetails;
import de.tuberlin.dima.xdbx.connector.DBInstance;
import de.tuberlin.dima.xdbx.connector.DefaultDBTypes;
import de.tuberlin.dima.xdbx.connector.RemoveReferenceCounter;
import de.tuberlin.dima.xdbx.user.User;
import de.tuberlin.dima.xdbx.util.ThrowingConsumer;
import de.tuberlin.dima.xdbx.util.UnregisterHook;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class ExasolInstance implements DBInstance<ExasolConnector>, RemoveReferenceCounter<ExasolConnector> {

    private final ClientFactory clientFactory;

    private final Connection adminConnection;
    private final Set<ExasolConnector> connectors = new HashSet<>();
    private final Map<DBConnectionDetails, String> assignedNames = new HashMap<>();

    public ExasolInstance(ClientFactory clientFactory, Connection adminConnection) throws SQLException {
        this.clientFactory = clientFactory;
        this.adminConnection = adminConnection;

        Statement statement = adminConnection.createStatement();
        String createSchema = "CREATE SCHEMA IF NOT EXISTS ADAPTER";
        String createAdepter = "CREATE OR REPLACE JAVA ADAPTER SCRIPT ADAPTER.JDBC_ADAPTER AS\n" +
                "%scriptclass com.exasol.adapter.RequestDispatcher;\n" +
                "%jar /buckets/bfsdefault/default/virtualschemas/virtual-schema-dist-9.0.3-generic-2.0.0.jar;\n" +
                "%jar /buckets/bfsdefault/default/virtualschemas/virtual-schema-dist-9.0.1-hive-2.0.0.jar;\n" +
                "%jar /buckets/bfsdefault/default/virtualschemas/virtual-schema-dist-9.0.1-mysql-2.0.0.jar;\n" +
                "%jar /buckets/bfsdefault/default/virtualschemas/virtual-schema-dist-9.0.1-postgresql-2.0.0.jar;\n" +
                //"%jar /buckets/bfsdefault/default/virtualschemas/virtual-schema-dist-9.0.3-exasol-5.0.4.jar;\n" +
                "%jar /buckets/bfsdefault/default/drivers/jdbc/mariadb/mariadb-java-client-2.7.1.jar;\n" +
                "%jar /buckets/bfsdefault/default/drivers/jdbc/postgres/postgresql-42.2.18.jar;\n" +
                "%jar /buckets/bfsdefault/default/drivers/jdbc/hive/HiveJDBC41.jar;\n" +
                "/";
        statement.execute(createSchema);
        statement.execute(createAdepter);
        statement.close();

    }

    @Override
    public ExasolConnector createConnection(DBConnectionDetails connectionDetails, ThrowingConsumer<DBConnectionDetails, Exception> deregisterHook) throws SQLException {
        UnregisterHook<ExasolConnector, ExasolInstance> unregisterHook = new UnregisterHook<>(this);
        ExasolConnector connector = new ExasolConnector(connectionDetails, deregisterHook.andThen(unregisterHook), clientFactory);
        unregisterHook.setLazyMariaDBConnector(connector);
        connectors.add(connector);
        return connector;
    }

    @Override
    public String computeViewName(String requestedName, User user, DBConnectionDetails dbConnectionDetails) {
        return user.getUID() + "_" + requestedName;
    }

    @Override
    public String computeForeignTableName(String requestedName, String foreignName, User user, DBConnectionDetails dbConnectionDetails, DBConnectionDetails foreignDBConnectionDetails) {
        //TODO maybe transfer ownership of connectors from DBManager to Instances and use timestamps instead, would require a hook for deregistering foreignDBS though
        String foreignAddress = DefaultDBTypes.EXASOL.formatURI(foreignDBConnectionDetails.getAddress(), foreignDBConnectionDetails.getPort(), foreignDBConnectionDetails.getDatabase());
        String[] split = foreignName.split("\\.");
        if (split.length == 2){
            foreignAddress += ":" + split[0];
            split[0] = split[1];
        }
        return "\"" + foreignAddress.hashCode() + "\"." + split[0];
    }

    @Override
    public void removeReference(ExasolConnector connector) throws Exception {
        if (connectors.remove(connector) && connectors.isEmpty()){
            adminConnection.close();
        }
    }
}
