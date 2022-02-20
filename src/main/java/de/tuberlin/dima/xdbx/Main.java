package de.tuberlin.dima.xdbx;

import de.tuberlin.dima.xdbx.client.ClientFactory;
import de.tuberlin.dima.xdbx.client.SimpleClientFactory;
import de.tuberlin.dima.xdbx.connector.ConnectionDetailFactory;
import de.tuberlin.dima.xdbx.connector.DBManager;
import de.tuberlin.dima.xdbx.connector.SimpleDBManager;
import de.tuberlin.dima.xdbx.node.JDBCConnectionDetailFromGRPCFactory;
import de.tuberlin.dima.xdbx.node.NodeServiceGrpc;
import de.tuberlin.dima.xdbx.node.Server;
import de.tuberlin.dima.xdbx.proto.DBConnection;
import de.tuberlin.dima.xdbx.user.SimpleUserManager;
import de.tuberlin.dima.xdbx.user.UserManager;

import java.io.IOException;
import java.util.HashMap;

public class Main {

    public static void main(String[] args){
        ConnectionDetailFactory<DBConnection> connectionDetailFactory = new JDBCConnectionDetailFromGRPCFactory();
        ClientFactory clientFactory = new SimpleClientFactory(connectionDetailFactory);

        //TODO read configuration
        DBManager dbManager = new SimpleDBManager(clientFactory, null, connectionDetailFactory, null, new HashMap<>());
        UserManager userManager = new SimpleUserManager(new HashMap<>(), dbManager);
        NodeServiceGrpc nodeService = new NodeServiceGrpc(userManager, dbManager, connectionDetailFactory);

        Server server = new Server(5912, nodeService);
        try {
            server.start();
        } catch (IOException e) {
            e.printStackTrace();
        }

        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                userManager.stop();
            }
        });
    }
}
