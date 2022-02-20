package de.tuberlin.dima.xdbx.client;

import de.tuberlin.dima.xdbx.connector.*;
import de.tuberlin.dima.xdbx.connector.ColumnInfo;
import de.tuberlin.dima.xdbx.node.SimpleXDBConnection;
import de.tuberlin.dima.xdbx.node.XDBConnectionDetails;
import de.tuberlin.dima.xdbx.proto.*;
import de.tuberlin.dima.xdbx.user.Credentials;
import io.grpc.Channel;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;

import java.util.*;
import java.util.concurrent.TimeUnit;

public class SimpleClient implements Client {

    private final XDBConnectionDetails connectionDetails;
    private final ConnectionDetailFactory<DBConnection> connectionFactory;
    private final NodeGrpc.NodeBlockingStub stub;
    private final NodeCredentials credentials;
    private final ManagedChannel managedChannel;


    public SimpleClient(XDBConnectionDetails connectionDetails, ConnectionDetailFactory<DBConnection> connectionFactory) {
        this.connectionDetails = connectionDetails;
        this.connectionFactory = connectionFactory;
        managedChannel = ManagedChannelBuilder
                .forAddress(connectionDetails.getAddress(), connectionDetails.getPort())
                .usePlaintext()
                .build();

        this.stub = NodeGrpc.newBlockingStub(managedChannel);
        credentials = NodeCredentials.newBuilder()
                .setUser(connectionDetails.getUsername())
                .setAuthentication(connectionDetails.getAuthentication())
                .build();
        Runtime.getRuntime().addShutdownHook(new Thread(this::close));

    }


    @Override
    public List<ColumnInfo> getColumns(String requestedName, DBConnectionDetails dbConnectionDetails) {
        TranslateRequest translateRequest = TranslateRequest.newBuilder()
                .setCredentials(credentials)
                .setRequestedName(requestedName)
                .build();

        TranslateResponse translateResponse = stub.translate(translateRequest);
        //TODO Error handling
        String assignedName = translateResponse.getAssignedName();
        DBConnection dbConnection = connectionFactory.convertConnectionDetails(dbConnectionDetails);

        GetColumnInfoRequest request = GetColumnInfoRequest.newBuilder()
                .setAssignedName(assignedName)
                .setCredentials(credentials)
                .setDbConn(dbConnection)
                .build();
        Iterator<de.tuberlin.dima.xdbx.proto.ColumnInfo> returnedColumns = stub.getColumnInfoRequest(request);

        List<ColumnInfo> columns = new LinkedList<>();
        while(returnedColumns.hasNext()){
            de.tuberlin.dima.xdbx.proto.ColumnInfo protoColumnInfo = returnedColumns.next();
            ColumnInfo dbColumnInfo = new ColumnInfo(protoColumnInfo.getName(), protoColumnInfo.getType(), protoColumnInfo.getColumnWidth());
            columns.add(dbColumnInfo);
        }

        return columns;
    }

    @Override
    public DBType getDBType(String address, int port) {
        DBAddress dbAddress = DBAddress.newBuilder()
                .setAddress(address)
                .setPort(port)
                .build();

        DBTypeRequest dbTypeRequest = DBTypeRequest.newBuilder()
                .setCredentials(credentials)
                .setDbAddress(dbAddress)
                .build();

        DBTypeResponse response = stub.getDBType(dbTypeRequest);
        //TODO Error handling
        return DefaultDBTypes.fromName(response.getDbtype());
    }

    @Override
    public String translate(String requestedName) {
        TranslateRequest request = TranslateRequest
                .newBuilder()
                .setCredentials(credentials)
                .setRequestedName(requestedName)
                .build();
        TranslateResponse response = stub.translate(request);
        return response.getAssignedName();
    }

    @Override
    public int createView(String name, String query, DBConnectionDetails dbConnectionDetails) {
        DBConnection dbConnection = connectionFactory.convertConnectionDetails(dbConnectionDetails);
        CreateViewRequest request = CreateViewRequest.newBuilder()
                .setCredentials(credentials)
                .setDbConn(dbConnection)
                .setLocalName(name)
                .setQuery(query)
                .build();
        return stub.createView(request).getSuccessCode();
    }

    @Override
    public int createForeignTable(String localName, String foreignName, XDBConnectionDetails foreignXDBConn, DBConnectionDetails localDBConnection, DBConnectionDetails foreignDBConnection) {
        DBConnection localConnection = connectionFactory.convertConnectionDetails(localDBConnection);
        DBConnection foreignConection = connectionFactory.convertConnectionDetails(foreignDBConnection);
        XDBConnection xdbConnection = SimpleXDBConnection.createGRPCXDBConnectionDetails(foreignXDBConn);


        ForeignTableRequest request = ForeignTableRequest.newBuilder()
                .setCredential(credentials)
                .setDbconn(localConnection)
                .setForeignDBConn(foreignConection)
                .setXdbconn(xdbConnection)
                .setLocalName(localName)
                .setForeignName(foreignName)
                .build();
        return stub.createForeignTable(request).getSuccessCode();
    }

    @Override
    public void close() {
        try {
            managedChannel.shutdown();
            managedChannel.awaitTermination(5000, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
