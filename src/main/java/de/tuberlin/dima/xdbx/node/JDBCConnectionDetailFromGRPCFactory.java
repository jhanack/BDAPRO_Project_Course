package de.tuberlin.dima.xdbx.node;

import de.tuberlin.dima.xdbx.connector.ConnectionDetailFactory;
import de.tuberlin.dima.xdbx.connector.DBConnectionDetails;
import de.tuberlin.dima.xdbx.connector.JDBCConnectionDetails;
import de.tuberlin.dima.xdbx.proto.DBAddress;
import de.tuberlin.dima.xdbx.proto.DBConnection;
import de.tuberlin.dima.xdbx.proto.DBCredentials;

public class JDBCConnectionDetailFromGRPCFactory implements ConnectionDetailFactory<DBConnection> {
    @Override
    public DBConnectionDetails createConnectionDetails(DBConnection grpcDetails) {
        return new JDBCConnectionDetails(grpcDetails.getDbAddress().getAddress(), grpcDetails.getDbAddress().getPort(),
                grpcDetails.getCredentials().getUser(),
                grpcDetails.getCredentials().getAuthentication(),
                grpcDetails.getDb());
    }

    @Override
    public DBConnection convertConnectionDetails(DBConnectionDetails dbConnectionDetails) {
        DBAddress address = DBAddress.newBuilder()
                .setAddress(dbConnectionDetails.getAddress())
                .setPort(dbConnectionDetails.getPort())
                .build();
        DBCredentials credentials = DBCredentials.newBuilder()
                .setUser(dbConnectionDetails.getUserName())
                .setAuthentication(dbConnectionDetails.getPassword())
                .build();
        DBConnection connection = DBConnection.newBuilder()
                .setDbAddress(address)
                .setCredentials(credentials)
                .setDb(dbConnectionDetails.getDatabase())
                .build();
        return connection;
    }

    @Override
    public DBConnectionDetails changeHostConnectionDetails(DBConnectionDetails oldConnectionDetails, String newAddress, int newPort) {
        return new JDBCConnectionDetails(newAddress,
                newPort,
                oldConnectionDetails.getUserName(),
                oldConnectionDetails.getPassword(),
                oldConnectionDetails.getDatabase());
    }
}
