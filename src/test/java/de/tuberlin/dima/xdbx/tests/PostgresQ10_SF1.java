package de.tuberlin.dima.xdbx.tests;


import de.tuberlin.dima.xdbx.client.Client;
import de.tuberlin.dima.xdbx.client.ClientFactory;
import de.tuberlin.dima.xdbx.client.SimpleClientFactory;
import de.tuberlin.dima.xdbx.connector.DBConnectionDetails;
import de.tuberlin.dima.xdbx.connector.DBType;
import de.tuberlin.dima.xdbx.connector.DefaultDBTypes;
import de.tuberlin.dima.xdbx.connector.JDBCConnectionDetails;
import de.tuberlin.dima.xdbx.connector.postgres.PostgresInstance;
import de.tuberlin.dima.xdbx.node.JDBCConnectionDetailFromGRPCFactory;
import de.tuberlin.dima.xdbx.node.SimpleXDBConnection;
import de.tuberlin.dima.xdbx.node.XDBConnectionDetails;
import de.tuberlin.dima.xdbx.user.SimpleUser;
import de.tuberlin.dima.xdbx.user.User;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.sql.*;
import java.util.HashMap;
import java.util.Map;

//These tests should be parametrized similar to Retrieval Tests so that they can be applied to all Connectors
public class PostgresQ10_SF1 {
    private XDBConnectionDetails client1connection;
    private XDBConnectionDetails client2connection;
    private XDBConnectionDetails client3connection;
    private DBConnectionDetails innerDB1connection = new JDBCConnectionDetails("pg-1", 5432, "bdapro_user", "bdapro_password", "bdapro_database");
    private DBConnectionDetails innerDB2connection = new JDBCConnectionDetails("pg-2", 5432, "bdapro_user", "bdapro_password", "bdapro_database");
    private DBConnectionDetails innerDB3connection = new JDBCConnectionDetails("pg-3", 5432, "bdapro_user", "bdapro_password", "bdapro_database");

    private Client client1;
    private Client client2;
    private Client client3;
    private TestInstance instance1;
    private TestInstance instance2;
    private TestInstance instance3;
    private ClientFactory clientFactory;
    private static Connection dbConn1;
    private static Connection dbConn2;
    private static Connection dbConn3;

    @BeforeAll
    public static void setupDB() throws SQLException {
        dbConn1 = DriverManager.getConnection("jdbc:postgresql://localhost:8080/bdapro_database", "bdapro_user", "bdapro_password");
        dbConn2 = DriverManager.getConnection("jdbc:postgresql://localhost:8081/bdapro_database", "bdapro_user", "bdapro_password");
        dbConn3 = DriverManager.getConnection("jdbc:postgresql://localhost:8082/bdapro_database", "bdapro_user", "bdapro_password");
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
        Map<User, String> users3 = new HashMap<>();
        users3.put(new SimpleUser("jan", 1, simpleUser -> {}, simpleUser -> {}), "xyz");
        users3.put(new SimpleUser("jon", 2, simpleUser -> {}, simpleUser -> {}), "fgh");

        Map<String, DBType> types1 = new HashMap<>();
        types1.put("localhost:8080", DefaultDBTypes.POSTGRES);
        Map<String, String> hostMappings1 = new HashMap<>();
        hostMappings1.put("pg-1:5432", "localhost:8080");

        Map<String, DBType> types2 = new HashMap<>();
        types2.put("localhost:8081", DefaultDBTypes.POSTGRES);
        Map<String, String> hostMappings2 = new HashMap<>();
        hostMappings2.put("pg-2:5432", "localhost:8081");

        Map<String, DBType> types3 = new HashMap<>();
        types3.put("localhost:8082", DefaultDBTypes.POSTGRES);
        Map<String, String> hostMappings3 = new HashMap<>();
        hostMappings3.put("pg-3:5432", "localhost:8082");

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
        instance3 = TestInstance.createInstanceWithSimpleImplementation(7002,
                users3,
                types3,
                hostMappings3,
                DefaultDBTypes.POSTGRES,
                new PostgresInstance(clientFactory),
                "localhost",
                8082,
                "bdapro_user",
                "bdapro_password",
                "bdapro_database");

        client1connection = new SimpleXDBConnection("localhost", 7000, "max", "123");
        client2connection = new SimpleXDBConnection("localhost", 7001, "tim", "abc");
        client3connection = new SimpleXDBConnection("localhost", 7002, "jan", "xyz");

        client1 = clientFactory.create(client1connection);
        client2 = clientFactory.create(client2connection);
        client3 = clientFactory.create(client3connection);

    }

    @AfterEach
    public void cleanup() throws InterruptedException {
        client1.close();
        client2.close();
        client3.close();
        instance1.stop();
        instance2.stop();
        instance3.stop();
    }


    @Test
    public void testCreateForeignTable() throws SQLException {

        long start = System.nanoTime();

        client2.createForeignTable("nation_localview", "sf1_nation",
                client3connection,
                instance2.getDbConnectionDetails(),
                innerDB3connection);

        client2.createView("n_o_c", "SELECT c_custkey, c_name, c_acctbal, c_address, c_phone, c_comment, o_orderkey, n_name FROM sf1_customer, sf1_orders, sf1_nation WHERE o_custkey = c_custkey AND c_nationkey = n_nationkey AND o_orderdate >= date '1993-10-01' AND o_orderdate < date '1994-01-01'", instance2.getDbConnectionDetails());

        client1.createForeignTable("n_o_c", "n_o_c",
                client2connection,
                instance1.getDbConnectionDetails(),
                innerDB2connection);

        client1.createView("tpchq10", "SELECT c_custkey, c_name, sum(l_extendedprice * (1 - l_discount)) AS revenue, c_acctbal, n_name, c_address, c_phone, c_comment FROM \"1_n_o_c\", sf1_lineitem WHERE l_orderkey = o_orderkey AND l_returnflag = 'R' GROUP BY c_custkey, c_name, c_acctbal, c_phone, n_name, c_address, c_comment ORDER BY revenue DESC LIMIT 20", instance1.getDbConnectionDetails());

        long middle = System.nanoTime();

        Statement stmt = dbConn1.createStatement();
        stmt.execute("SELECT * FROM \"1_tpchq10\"");

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
        stmt2.execute("DROP VIEW \"1_tpchq10\"");
        stmt2.close();
        dbConn1.createStatement().execute("DROP FOREIGN TABLE \"1_n_o_c\"");
        dbConn1.close();
        dbConn2.close();
        dbConn3.close();
    }

}
