package de.tuberlin.dima.xdbx.tests;

import de.tuberlin.dima.xdbx.client.ClientFactory;
import de.tuberlin.dima.xdbx.client.SimpleClientFactory;
import de.tuberlin.dima.xdbx.connector.*;
import de.tuberlin.dima.xdbx.connector.postgres.PostgresInstance;
import de.tuberlin.dima.xdbx.node.JDBCConnectionDetailFromGRPCFactory;
import de.tuberlin.dima.xdbx.node.NodeServiceGrpc;
import de.tuberlin.dima.xdbx.node.Server;
import de.tuberlin.dima.xdbx.proto.DBConnection;
import de.tuberlin.dima.xdbx.user.SimpleUserManager;
import de.tuberlin.dima.xdbx.user.User;
import de.tuberlin.dima.xdbx.user.UserManager;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class TestInstance {
    private final ConnectionDetailFactory<DBConnection> connectionDetailFactory;
    private final ClientFactory clientFactory;
    private final NodeServiceGrpc grpc;
    private final Server server;
    private final DBManager dbmanager;
    private final UserManager userManager;
    private final DBConnectionDetails dbConnectionDetails;

    private final Thread shutdownHook;

    public TestInstance(ConnectionDetailFactory<DBConnection> connectionDetailFactory,
                        ClientFactory clientFactory,
                        NodeServiceGrpc grpc,
                        Server server,
                        DBManager dbmanager,
                        UserManager userManager,
                        DBConnectionDetails dbConnectionDetails, Thread shutdownHook) {
        this.connectionDetailFactory = connectionDetailFactory;
        this.clientFactory = clientFactory;
        this.grpc = grpc;
        this.server = server;
        this.dbmanager = dbmanager;
        this.userManager = userManager;
        this.dbConnectionDetails = dbConnectionDetails;
        this.shutdownHook = shutdownHook;
    }

    public Thread getShutdownHook() {
        return shutdownHook;
    }

    public ConnectionDetailFactory<DBConnection> getConnectionDetailFactory() {
        return connectionDetailFactory;
    }

    public ClientFactory getClientFactory() {
        return clientFactory;
    }

    public NodeServiceGrpc getGrpc() {
        return grpc;
    }

    public Server getServer() {
        return server;
    }

    public DBManager getDbmanager() {
        return dbmanager;
    }

    public UserManager getUserManager() {
        return userManager;
    }

    public static TestInstance createInstanceWithSimpleImplementation(int port, Map<User, String> users,
                                                                      Map<String, DBType> types,
                                                                      Map<String, String> hostMappings,
                                                                      DBType dbType,
                                                                      DBInstance<?> dbInstance,
                                                                      String dbAddress,
                                                                      int dbPort,
                                                                      String dbUsername,
                                                                      String dbPassword,
                                                                      String database) throws IOException {
        ConnectionDetailFactory<DBConnection> connectionDetailFactory = new JDBCConnectionDetailFromGRPCFactory();
        ClientFactory clientFactory = new SimpleClientFactory(connectionDetailFactory);
        Map<DBType, DBInstance<?>> factories = new HashMap<>();
        factories.put(dbType, dbInstance);

        DBManager dbManager = new SimpleDBManager(clientFactory, factories, connectionDetailFactory, types, hostMappings);
        UserManager userManager = new SimpleUserManager(users, dbManager);
        NodeServiceGrpc grpc = new NodeServiceGrpc(userManager, dbManager, connectionDetailFactory);
        Server server = new Server(port, grpc);
        DBConnectionDetails dbConnectionDetails = new JDBCConnectionDetails(dbAddress, dbPort, dbUsername, dbPassword, database);

        server.start();
        Thread shutdownHook = new Thread(() -> {
            try {
                server.stop();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });
        Runtime.getRuntime().addShutdownHook(shutdownHook);

        return new TestInstance(connectionDetailFactory, clientFactory, grpc, server, dbManager, userManager, dbConnectionDetails, shutdownHook);
    }

    public void stop() throws InterruptedException {
        server.stop();
        Runtime.getRuntime().removeShutdownHook(shutdownHook);
    }

    public DBConnectionDetails getDbConnectionDetails() {
        return dbConnectionDetails;
    }
}
