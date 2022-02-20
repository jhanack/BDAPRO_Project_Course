package de.tuberlin.dima.xdbx.tests;


import com.palantir.docker.compose.DockerComposeExtension;
import com.palantir.docker.compose.configuration.ShutdownStrategy;
import de.tuberlin.dima.xdbx.client.Client;
import de.tuberlin.dima.xdbx.client.ClientFactory;
import de.tuberlin.dima.xdbx.client.SimpleClientFactory;
import de.tuberlin.dima.xdbx.connector.*;
import de.tuberlin.dima.xdbx.connector.mariadb.MariaDBInstance;
import de.tuberlin.dima.xdbx.connector.postgres.PostgresInstance;
import de.tuberlin.dima.xdbx.node.JDBCConnectionDetailFromGRPCFactory;
import de.tuberlin.dima.xdbx.node.SimpleXDBConnection;
import de.tuberlin.dima.xdbx.node.XDBConnectionDetails;
import de.tuberlin.dima.xdbx.user.SimpleUser;
import de.tuberlin.dima.xdbx.user.User;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.RegisterExtension;

import java.io.IOException;
import java.sql.*;
import java.util.*;

import static org.junit.Assert.assertEquals;

//These tests should be parametrized similar to Retrieval Tests so that they can be applied to all Connectors
public class PostgresTests {
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


    @RegisterExtension
    public static DockerComposeExtension dockerCompose = DockerComposeExtension.builder()
            .file("src/test/resources/docker-compose.yml")
            .shutdownStrategy(ShutdownStrategy.KILL_DOWN)
            .build();

    @BeforeAll
    public static void setupDB() throws SQLException {
        dbConn1 = DriverManager.getConnection("jdbc:postgresql://localhost:8080/bdapro_database", "bdapro_user", "bdapro_password");
        dbConn2 = DriverManager.getConnection("jdbc:postgresql://localhost:8081/bdapro_database", "bdapro_user", "bdapro_password");
    }

    @AfterAll
    public static void teardownDB() throws SQLException {
        Statement stmt = dbConn1.createStatement();
        stmt.executeUpdate("DROP TABLE schema_a CASCADE");
        stmt.close();
        stmt = dbConn2.createStatement();
        stmt.executeUpdate("DROP TABLE schema_b CASCADE");
        stmt.close();

        dbConn1.close();
        dbConn2.close();
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
        assertEquals(0, success);
        Statement stmt  = dbConn1.createStatement();
        stmt.execute("DROP FOREIGN TABLE \"1_remote\"");
        stmt.close();
    }

    @Test
    public void testCreateView() throws SQLException {
        int success = client2.createView("someView", "SELECT name, region FROM schema_b WHERE sales > 100", instance2.getDbConnectionDetails());

        assertEquals(0, success);
        Statement stmt  = dbConn2.createStatement();
        stmt.execute("DROP VIEW \"1_someView\"");
        stmt.close();
    }


    @Test
    public void testRetrieveFromForeignTable() throws SQLException {
        client1.createForeignTable("remote",
                "schema_b",
                client2connection,
                instance1.getDbConnectionDetails(),
                innerDB2connection);
        Statement stmt = dbConn1.createStatement();
        stmt.execute("SELECT * FROM \"1_remote\"");
        ResultSet resultSet = stmt.getResultSet();
        resultSet.next();
        String retrievedName = resultSet.getString(1);
        System.out.println(retrievedName);
        resultSet.next();
        retrievedName = resultSet.getString(1);
        System.out.println(retrievedName);
        resultSet.next();
        retrievedName = resultSet.getString(1);
        System.out.println(retrievedName);

        //assertEquals("bertha", retrievedName);
        dbConn1.createStatement().execute("DROP FOREIGN TABLE \"1_remote\"");
    }

    @Test
    public void testGetColumns() {
        List<ColumnInfo> columns = client2.getColumns("schema_b", instance2.getDbConnectionDetails());
        List<ColumnInfo> expected = List.of(
                new ColumnInfo("name", "VARCHAR", 30),
                new ColumnInfo("sales", "DECIMAL", 0),
                new ColumnInfo("region", "VARCHAR", 30)
        );
        Assertions.assertThat(columns).hasSameElementsAs(expected);
    }

    @Test
    public void logoutUser() throws Exception {
        client1.createForeignTable("remote",
                "schema_b",
                client2connection,
                instance1.getDbConnectionDetails(),
                innerDB2connection);
        User max = instance1.getUserManager().getUser("max");
        max.logout();
        Assertions.assertThat(max.isLoggedIn()).isFalse();
        Statement stmt = dbConn1.createStatement();
        Throwable thrown = Assertions.catchThrowable(() -> stmt.execute("DROP FOREIGN TABLE \"1_remote\""));
        Assertions.assertThat(thrown).isInstanceOf(SQLException.class);
    }

}
