package de.tuberlin.dima.xdbx.tests;


import de.tuberlin.dima.xdbx.client.Client;
import de.tuberlin.dima.xdbx.client.ClientFactory;
import de.tuberlin.dima.xdbx.client.SimpleClientFactory;
import de.tuberlin.dima.xdbx.connector.*;
import de.tuberlin.dima.xdbx.connector.postgres.PostgresInstance;
import de.tuberlin.dima.xdbx.node.JDBCConnectionDetailFromGRPCFactory;
import de.tuberlin.dima.xdbx.node.SimpleXDBConnection;
import de.tuberlin.dima.xdbx.node.XDBConnectionDetails;
import de.tuberlin.dima.xdbx.user.SimpleUser;
import de.tuberlin.dima.xdbx.user.User;
import org.junit.jupiter.api.*;

import java.io.IOException;
import java.sql.*;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;

//These tests should be parametrized similar to Retrieval Tests so that they can be applied to all Connectors
public class PostgresQ3_SF1 {
    private XDBConnectionDetails client1connection;
    private XDBConnectionDetails client2connection;
    private DBConnectionDetails innerDB1connection = new JDBCConnectionDetails("pg-1", 5432, "bdapro_user", "bdapro_password", "bdapro_database");
    private DBConnectionDetails innerDB2connection = new JDBCConnectionDetails("pg-2", 5432, "bdapro_user", "bdapro_password", "bdapro_database");

    private Client client1;
    private Client client2;
    private TestInstance instance1;
    private TestInstance instance2;
    private ClientFactory clientFactory;
    private static Connection dbConn1;
    private static Connection dbConn2;

    @BeforeAll
    public static void setupDB() throws SQLException {
        dbConn1 = DriverManager.getConnection("jdbc:postgresql://localhost:8080/bdapro_database", "bdapro_user", "bdapro_password");
        dbConn2 = DriverManager.getConnection("jdbc:postgresql://localhost:8081/bdapro_database", "bdapro_user", "bdapro_password");
    }

    @BeforeEach
    public void setup() throws IOException {
        clientFactory = new SimpleClientFactory(new JDBCConnectionDetailFromGRPCFactory());

        Map<User, String> users1 = new HashMap<>();
        users1.put(new SimpleUser("max", 1, simpleUser -> {}, simpleUser -> {}), "123");
        users1.put(new SimpleUser("moritz", 2, simpleUser -> {}, simpleUser -> {}), "456");
        Map<User, String> users2 = new HashMap<>();
        users2.put(new SimpleUser("tim", 1, simpleUser -> {}, simpleUser -> {}), "abc");
        users2.put(new SimpleUser("tom", 2, simpleUser -> {}, simpleUser -> {}), "def");

        Map<String, DBType> types1 = new HashMap<>();
        types1.put("localhost:8080", DefaultDBTypes.POSTGRES);
        //types1.put("pg-1:8080", DefaultDBTypes.POSTGRES);

        Map<String, String> hostMappings1 = new HashMap<>();
        hostMappings1.put("pg-1:5432", "localhost:8080");

        Map<String, DBType> types2 = new HashMap<>();
        types2.put("localhost:8081", DefaultDBTypes.POSTGRES);
        //types2.put("pg-2:8081", DefaultDBTypes.POSTGRES);
        Map<String, String> hostMappings2 = new HashMap<>();
        hostMappings2.put("pg-2:5432", "localhost:8081");

        instance1 = TestInstance.createInstanceWithSimpleImplementation(7000,
                users1,
                types1,
                hostMappings1,
                DefaultDBTypes.POSTGRES,
                new PostgresInstance(clientFactory),
                "localhost",
                8080,
                "bdapro_user",
                "bdapro_password",
                "bdapro_database");
        instance2 = TestInstance.createInstanceWithSimpleImplementation(7001,
                users2,
                types2,
                hostMappings2,
                DefaultDBTypes.POSTGRES,
                new PostgresInstance(clientFactory),
                "localhost",
                8081,
                "bdapro_user",
                "bdapro_password",
                "bdapro_database");

        client1connection = new SimpleXDBConnection("localhost", 7000, "max", "123");
        client2connection = new SimpleXDBConnection("localhost", 7001, "tim", "abc");

        client1 = clientFactory.create(client1connection);
        client2 = clientFactory.create(client2connection);

    }

    @AfterEach
    public void cleanup() throws InterruptedException {
        client1.close();
        client2.close();
        instance1.stop();
        instance2.stop();
    }


    @Test
    public void testCreateForeignTable() throws SQLException {

        int success = client1.createForeignTable("remote",
                "schema_b",
                client2connection,
                instance1.getDbConnectionDetails(),
                innerDB2connection);

        long start = System.nanoTime();


        client2.createView("customer_localview", "SELECT c_custkey FROM sf1_customer WHERE c_mktsegment = 'BUILDING'", instance2.getDbConnectionDetails());

        client1.createForeignTable("customer_localview", "customer_localview",
                client2connection,
                instance1.getDbConnectionDetails(),
                innerDB2connection);

        client1.createView("tpchq3"
                ,"SELECT l_orderkey, sum(l_extendedprice * (1 - l_discount)) AS revenue, o_orderdate, o_shippriority FROM \"1_customer_localview\", sf1_orders, sf1_lineitem WHERE c_custkey = o_custkey AND l_orderkey = o_orderkey AND o_orderdate < date '1995-03-15' AND l_shipdate > date '1995-03-15' GROUP BY l_orderkey, o_orderdate, o_shippriority ORDER BY revenue desc, o_orderdate LIMIT 10"
                ,instance1.getDbConnectionDetails());

        long middle = System.nanoTime();

        Statement stmt = dbConn1.createStatement();
        stmt.execute("SELECT * FROM \"1_tpchq3\"");

        ResultSet resultSet = stmt.getResultSet();
        resultSet.next();
        String retrievedName = resultSet.getString(1);
        System.out.println(retrievedName);

        stmt.close();

        long finish = System.nanoTime();

        System.out.println("Registration finished in: " + (middle - start) / 1000000 + " ms");
        System.out.println("Execution finished in: " + (finish - middle) / 1000000 + " ms");
        System.out.println("Total: " + (finish - start) / 1000000 + " ms");

        Statement stmt2  = dbConn1.createStatement();
        stmt2.execute("DROP VIEW \"1_tpchq3\"");
        stmt2.close();
        dbConn1.createStatement().execute("DROP FOREIGN TABLE \"1_customer_localview\"");
        dbConn1.close();
        dbConn2.close();
    }

}
