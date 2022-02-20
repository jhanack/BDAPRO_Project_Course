package de.tuberlin.dima.xdbx.node;
import com.google.protobuf.MessageOrBuilder;
import de.tuberlin.dima.xdbx.connector.ConnectionDetailFactory;
import de.tuberlin.dima.xdbx.connector.DBConnectionDetails;
import de.tuberlin.dima.xdbx.connector.DBManager;
import de.tuberlin.dima.xdbx.connector.DBType;
import de.tuberlin.dima.xdbx.proto.*;
import de.tuberlin.dima.xdbx.user.*;
import io.grpc.stub.StreamObserver;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public class NodeServiceGrpc extends NodeGrpc.NodeImplBase {

    private final UserManager userManager;
    private final ConnectionDetailFactory<DBConnection> connectionDetailFactory;
    private final DBManager dbManager;

    public NodeServiceGrpc(UserManager userManager,
                           DBManager dbManager,
                           ConnectionDetailFactory<DBConnection> connectionDetailFactory) {
        this.userManager = userManager;
        this.dbManager = dbManager;
        this.connectionDetailFactory = connectionDetailFactory;
    }

    @Override
    public void createForeignTable(ForeignTableRequest request, StreamObserver<StandardResponse> responseObserver) {
        User user = authenticate(request.getCredential(), responseObserver, Function.identity());
        if (user == null) return;

        //Parse the rest of the request
        String localName = request.getLocalName();
        String foreignName = request.getForeignName();
        DBConnectionDetails localConnectionDetails = connectionDetailFactory.createConnectionDetails(request.getDbconn());
        DBConnectionDetails foreignConnectionDetails = connectionDetailFactory.createConnectionDetails(request.getForeignDBConn());
        XDBConnectionDetails xdbConnectionDetails = SimpleXDBConnection.createXDBConnectionDetailsFromGRPC(request.getXdbconn());

        StandardResponse.Builder builder = StandardResponse.newBuilder();
        try {
            this.dbManager.addForeignTable(localName, foreignName, user, localConnectionDetails, foreignConnectionDetails, xdbConnectionDetails);
            builder.setSuccessCode(0);
        } catch (SQLException e) {
            builder.setSuccessCode(3);
            e.printStackTrace();
        }

        responseObserver.onNext(builder.build());
        responseObserver.onCompleted();
    }

    @Override
    public void createView(CreateViewRequest request, StreamObserver<StandardResponse> responseObserver) {
        User user = authenticate(request.getCredentials(), responseObserver, Function.identity());
        if (user == null) return;

        String localName = request.getLocalName();
        String query = request.getQuery();
        DBConnectionDetails localConnectionDetails = connectionDetailFactory.createConnectionDetails(request.getDbConn());

        StandardResponse.Builder builder = StandardResponse.newBuilder();

        try {
            dbManager.addView(localName, query, user, localConnectionDetails);
            builder.setSuccessCode(0);
        } catch (SQLException throwables) {
            builder.setSuccessCode(3);
        }

        responseObserver.onNext(builder.build());
        responseObserver.onCompleted();
    }

    @Override
    public void translate(TranslateRequest request, StreamObserver<TranslateResponse> responseObserver) {
        User user = authenticate(request.getCredentials(),
                responseObserver,
                standardResponse -> TranslateResponse.newBuilder().setResponseCode(standardResponse).clearAssignedName().build());
        if (user == null) return;

        String assignedName;
        StandardResponse.Builder standardResponseBuilder = StandardResponse.newBuilder();
        TranslateResponse.Builder responseBuilder = TranslateResponse.newBuilder();
        try {
            assignedName = dbManager.translate(request.getRequestedName(), user);
            standardResponseBuilder.setSuccessCode(0);
            responseBuilder.setAssignedName(assignedName);
        } catch (DBManager.NoSuchDBObject noSuchDBObject) {
            standardResponseBuilder.setSuccessCode(2);
            responseBuilder.setAssignedName(request.getRequestedName());
        }

        responseBuilder.setResponseCode(standardResponseBuilder.build());
        responseObserver.onNext(responseBuilder.build());
        responseObserver.onCompleted();
    }

    @Override
    public void getColumnInfoRequest(GetColumnInfoRequest request, StreamObserver<ColumnInfo> responseObserver) {
        User user = authenticate(request.getCredentials(),
                responseObserver,
                standardResponse -> ColumnInfo.newBuilder().setResponseCode(standardResponse).clearName().clearType().build());
        if (user == null) return;

        DBConnectionDetails connectionDetails = connectionDetailFactory.createConnectionDetails(request.getDbConn());
        List<de.tuberlin.dima.xdbx.connector.ColumnInfo> columns;
        StandardResponse.Builder standardBuilder = StandardResponse.newBuilder();
        try {
            columns = dbManager.getColumns(request.getAssignedName(), connectionDetails);
        } catch (SQLException throwables) {
            columns = null;
        }

        if (columns == null){
            standardBuilder.setSuccessCode(3);
            ColumnInfo failed = ColumnInfo.newBuilder().clear().setResponseCode(standardBuilder).build();
            responseObserver.onNext(failed);
            responseObserver.onCompleted();
            return;
        }
        standardBuilder.setSuccessCode(0);
        StandardResponse success = standardBuilder.build();

        for (de.tuberlin.dima.xdbx.connector.ColumnInfo columnInfo: columns){
            ColumnInfo.Builder builder = ColumnInfo.newBuilder();
            builder.setResponseCode(success);
            builder.setName(columnInfo.getName());
            builder.setType(columnInfo.getType());
            builder.setColumnWidth(columnInfo.getColumnWidth());
            ColumnInfo columnInfoMessage = builder.build();
            responseObserver.onNext(columnInfoMessage);
        }

        responseObserver.onCompleted();
    }

    @Override
    public void getDBType(DBTypeRequest request, StreamObserver<DBTypeResponse> responseObserver) {
        User user = authenticate(request.getCredentials(),
                responseObserver,
                standardResponse -> DBTypeResponse.newBuilder().clear().setResponseCode(standardResponse).build());
        if (user == null) return;

        String address = request.getDbAddress().getAddress() + ":" + request.getDbAddress().getPort();
        DBType type = dbManager.getDBType(address);
        DBTypeResponse response;
        if (type == null){
            response = DBTypeResponse.newBuilder().clear()
                    .setResponseCode(StandardResponse.newBuilder().setSuccessCode(2).build())
                    .build();
        } else {
            response = DBTypeResponse.newBuilder()
                    .setDbtype(type.getName())
                    .setResponseCode(StandardResponse.newBuilder().setSuccessCode(0).build())
                    .build();
        }

        responseObserver.onNext(response);
        responseObserver.onCompleted();

    }

    private <T extends MessageOrBuilder> User authenticate(NodeCredentials nodeCredentials, StreamObserver<T> responseObserver, Function<StandardResponse, T> factory){
        Credentials credentials = new SimpleCredentials(nodeCredentials.getUser(), nodeCredentials.getAuthentication());
        User user = null;

        boolean authenticated = userManager.authenticateUser(credentials);
        if (authenticated){
            try {
                user = userManager.getUser(nodeCredentials.getUser());
                user.login();
            } catch (UserNotFoundException e) {
                authenticated = false;
            }
        }

        //Authentication failed or user was not found (Telling the client this would be bad praxis as it would allow easy brute force attacks)
        if (!authenticated){
            StandardResponse standardResponse = StandardResponse.newBuilder().setSuccessCode(1).build();
            responseObserver.onNext(factory.apply(standardResponse));
            responseObserver.onCompleted();
            return null;
        } else return user;
    }
}
