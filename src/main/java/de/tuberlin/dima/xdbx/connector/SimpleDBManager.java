package de.tuberlin.dima.xdbx.connector;

import de.tuberlin.dima.xdbx.client.ClientFactory;
import de.tuberlin.dima.xdbx.node.XDBConnectionDetails;
import de.tuberlin.dima.xdbx.user.User;

import javax.annotation.CheckForNull;
import java.sql.SQLException;
import java.util.*;
import java.util.function.Supplier;

public class SimpleDBManager implements DBManager {

    //Maps User to a map where the requestedName is mapped to the created DBObjects
    private final Map<User, Map<String, DBObject>> dbObjects = Collections.synchronizedMap(new HashMap<>());
    //Connection Pool
    private final Map<DBConnectionDetails, DBConnector> dbConnectors = Collections.synchronizedMap(new HashMap<>());
    //TODO map instances to database addresses instead
    private final Map<DBType, DBInstance<?>> factories;
    private final ConnectionDetailFactory<?> connectionDetailFactory;
    //Maps database addresses to the DBTypes
    private final Map<String, DBType> dbTypes;
    private final Map<String, String> hostMappings;

    private final ClientFactory clientFactory;

    public SimpleDBManager(ClientFactory clientFactory,
                           Map<DBType, DBInstance<?>> factories,
                           ConnectionDetailFactory<?> connectionDetailFactory,
                           Map<String, DBType> dbTypes,
                           Map<String, String> hostMappings) {
        this.factories = factories;
        this.connectionDetailFactory = connectionDetailFactory;
        this.dbTypes = dbTypes;
        this.clientFactory = clientFactory;
        this.hostMappings = hostMappings;

    }


    /**
     * Does NOT close the Database Connection as multiple node users might share a DBConnector
     * @param user
     */
    @Override
    public void cleanupUser(User user) throws Exception {
        for (DBObject dbObject: this.dbObjects.getOrDefault(user, new HashMap<>()).values()){
            dbObject.drop();
        }
        dbObjects.remove(user);
    }

    @Override
    public void addForeignTable(String localName,
                                String foreignName,
                                User user,
                                DBConnectionDetails dbConnection,
                                DBConnectionDetails foreignDBConnection,
                                XDBConnectionDetails xdbConnectionDetails) throws SQLException {
        DBConnector connector = selectConnector(dbConnection);
        String assignedName = computeForeignTableName(localName, foreignName, user, dbConnection, foreignDBConnection);
        //TODO check if "_" in name is valid for all databases
        DBForeignTable foreignTable = connector.addForeignTable(assignedName,
                foreignName,
                foreignDBConnection,
                xdbConnectionDetails);
        foreignTable.create();
        dbObjects.computeIfAbsent(user, user1 -> new HashMap<>()).put(localName, foreignTable);
    }

    @Override
    public void addView(String localName, String query, User user, DBConnectionDetails dbConnection) throws SQLException {
        DBConnector connector = selectConnector(dbConnection);
        String assignedName = computeViewName(localName, user, dbConnection);
        DBView dbview = connector.addView(assignedName, query);
        dbview.create();
        dbObjects.computeIfAbsent(user, user1 -> new HashMap<>()).put(localName, dbview);
    }

    @Override
    public String translate(String requestedName, User user) throws NoSuchDBObject {
        DBObject foundObject = dbObjects.computeIfAbsent(user, user1 -> new HashMap<>()).get(requestedName);
        if (foundObject == null) throw new NoSuchDBObject(requestedName, user);

        return foundObject.getAssignedName();
    }

    @Override
    public List<ColumnInfo> getColumns(String assignedName, DBConnectionDetails dbConnection) throws SQLException {
        DBConnector connector = selectConnector(dbConnection);
        return connector.getColumns(assignedName);
    }

    @Override
    @CheckForNull
    public DBType getDBType(String address) {
        return dbTypes.get(hostMappings.getOrDefault(address, address));
    }

    private void deregisterConnector(DBConnectionDetails connectionDetails){
        dbConnectors.remove(connectionDetails);
    }

    private DBConnector selectConnector(DBConnectionDetails dbConnectionDetails) {
        DBConnector connector = dbConnectors.get(dbConnectionDetails);
        if (connector == null) {
            try {
                String address = dbConnectionDetails.getAddress() + ":" + dbConnectionDetails.getPort();
                address = hostMappings.getOrDefault(address, address);
                String newHost = address.split(":")[0];
                int newPort = Integer.parseInt(address.split(":")[1]);
                DBConnectionDetails newConnectionDetails = connectionDetailFactory.changeHostConnectionDetails(dbConnectionDetails, newHost, newPort);
//                System.out.println(newPort);
                connector = factories
                        .get(dbTypes.get(address))
                        .createConnection(newConnectionDetails, this::deregisterConnector);

            } catch (SQLException throwables) {
                throwables.printStackTrace();
            }
            if(connector == null){
                //TODO something went seriously wrong
            }
        }
        return connector;
    }

    private String computeViewName(String requestedName, User user, DBConnectionDetails dbConnectionDetails){
        String address = dbConnectionDetails.getAddress() + ":" + dbConnectionDetails.getPort();
        address = hostMappings.getOrDefault(address, address);
        return factories.get(dbTypes.get(address)).computeViewName(requestedName, user, dbConnectionDetails);
    }

    private String computeForeignTableName(String requestedName, String foreignName, User user, DBConnectionDetails dbConnectionDetails, DBConnectionDetails foreignDBConnectionDetails){
        String address = dbConnectionDetails.getAddress() + ":" + dbConnectionDetails.getPort();
        address = hostMappings.getOrDefault(address, address);
        return factories.get(dbTypes.get(address)).computeForeignTableName(requestedName, foreignName, user, dbConnectionDetails, foreignDBConnectionDetails);
    }

}
