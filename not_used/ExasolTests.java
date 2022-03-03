/*
package de.tuberlin.dima.xdbx.tests;

import com.palantir.docker.compose.DockerComposeExtension;
import com.palantir.docker.compose.configuration.ShutdownStrategy;
import de.tuberlin.dima.xdbx.client.Client;
import de.tuberlin.dima.xdbx.client.ClientFactory;
import de.tuberlin.dima.xdbx.client.SimpleClient;
import de.tuberlin.dima.xdbx.client.SimpleClientFactory;
import de.tuberlin.dima.xdbx.connector.DBConnectionDetails;
import de.tuberlin.dima.xdbx.connector.DBType;
import de.tuberlin.dima.xdbx.connector.DefaultDBTypes;
import de.tuberlin.dima.xdbx.connector.JDBCConnectionDetails;
import de.tuberlin.dima.xdbx.connector.exasol.ExasolInstance;
import de.tuberlin.dima.xdbx.node.JDBCConnectionDetailFromGRPCFactory;
import de.tuberlin.dima.xdbx.node.SimpleXDBConnection;
import de.tuberlin.dima.xdbx.node.XDBConnectionDetails;
import de.tuberlin.dima.xdbx.user.SimpleUser;
import de.tuberlin.dima.xdbx.user.User;
import de.tuberlin.dima.xdbx.util.ThrowingSupplier;
import org.javatuples.Pair;
import org.javatuples.Quartet;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.*;
import java.util.function.Function;

public class ExasolTests {

    ClientFactory clientFactory = new SimpleClientFactory(new JDBCConnectionDetailFromGRPCFactory());
    @RegisterExtension
    public static DockerComposeExtension dockerCompose = DockerComposeExtension.builder()
            .file("src/test/resources/exa-compose.yml")
            .shutdownStrategy(ShutdownStrategy.KILL_DOWN)
           // .waitingForService("exa-1", HealthChecks.toRespondOverHttp(2580, (port) -> port.inFormat("https://$HOST:$EXTERNAL_PORT")))
            .build();


    @BeforeAll
    static void empty() throws IOException, InterruptedException {
        //dockerCompose.exec(DockerComposeExecOption.noOptions(), "exasol", DockerComposeExecArgument.arguments("/bin/bash", "/setup.sh"));
        Thread.sleep(1000*35);
        Process p = Runtime.getRuntime().exec("docker exec exa-1 /bin/bash /setup.sh");
        new Thread(() -> {
            BufferedReader input = new BufferedReader(new InputStreamReader(p.getInputStream()));
            String line = null;

            try {
                while ((line = input.readLine()) != null)
                    System.out.println(line);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();

        p.waitFor();
        Assertions.assertTrue(true);
    }

    @Test
    public void testRetrieveFromForeignTable() throws Exception {
        Quartet<Map<DBType, Pair<ThrowingSupplier<TestInstance, Exception>, XDBConnectionDetails>>,
                Map<DBType, Pair<ThrowingSupplier<TestInstance, Exception>, XDBConnectionDetails>>,
                Map<DBType, Pair<ThrowingSupplier<Connection, Exception>, Function<String, String>>>,
                Map<DBType, DBConnectionDetails>> providers = RetrievalTests.createDBProviders();

        Map<DBType, Pair<ThrowingSupplier<TestInstance, Exception>, XDBConnectionDetails>> firstInstance = providers.getValue0();
        Map<DBType, Pair<ThrowingSupplier<TestInstance, Exception>, XDBConnectionDetails>> secondInstance = providers.getValue1();
        Map<DBType, Pair<ThrowingSupplier<Connection, Exception>, Function<String, String>>> firstAdditionalInfo = providers.getValue2();

        Map<DBType, DBConnectionDetails> innerDBConnections = providers.getValue3();

        Pair<ThrowingSupplier<TestInstance, Exception>, XDBConnectionDetails> postgresPair = firstInstance.get(DefaultDBTypes.POSTGRES);
        Pair<ThrowingSupplier<TestInstance, Exception>, XDBConnectionDetails> mariaDBPair = secondInstance.get(DefaultDBTypes.MARIADB);
        TestInstance postgres = postgresPair.getValue0().get();
        TestInstance mariadb = mariaDBPair.getValue0().get();

        Client client1 = clientFactory.create(postgresPair.getValue1());
        Client client2 = clientFactory.create(mariaDBPair.getValue1());

        Map<User, String> users = new HashMap<>();
        users.put(new SimpleUser("mop", 1, simpleUser -> {}, simpleUser -> {}), "def");

        Map<String, String> hosts = new HashMap<>();
        Map<String, DBType> types = new HashMap<>();
        types.put("localhost:8563", DefaultDBTypes.EXASOL);

        Properties properties = new Properties();
        properties.setProperty("user", "sys");
        properties.setProperty("password", "exasol");
        Connection adminConnection = DriverManager.getConnection("jdbc:exa:localhost:8563", properties);

        TestInstance exaInstance = TestInstance.createInstanceWithSimpleImplementation(
            7005,
                users,
                types,
                hosts,
                DefaultDBTypes.EXASOL,
                new ExasolInstance(clientFactory, adminConnection),
                "localhost",
                8563,
                "sys",
                "exasol",
                "EXA_DB");
        Client exaClient = clientFactory.create(new SimpleXDBConnection("localhost", 7005,"mop", "def"));

        exaClient.createForeignTable("remote_a", "schema_a",
                postgresPair.getValue1(),
                exaInstance.getDbConnectionDetails(),
                postgres.getDbConnectionDetails());

        exaClient.createForeignTable("remote_b", "schema_b",
                mariaDBPair.getValue1(),
                exaInstance.getDbConnectionDetails(),
                mariadb.getDbConnectionDetails());

        String schemaAName = exaClient.translate("remote_a");
        String schemaBName = exaClient.translate("remote_b");

        Statement statement = adminConnection.createStatement();


        String query = "SELECT uid, region FROM " + schemaAName + " JOIN " + schemaBName +" USING (name)";

        statement.execute(query);


        ResultSet resultSet = statement.getResultSet();
//        List<Integer> expectedUids = List.of(1, 2, 3);
//        List<String> expectedRegions = List.of("europe", "europe", "asia");
//        List<Integer> receivedUids = new LinkedList<>();
//        List<String> receivedRegions = new LinkedList<>();
        while (resultSet.next()) {
//            receivedUids.add(resultSet.getInt(1));
 //           receivedRegions.add(resultSet.getString(2));
        }
        resultSet.close();
        statement.close();

//        org.assertj.core.api.Assertions.assertThat(receivedUids).hasSameElementsAs(expectedUids);
//       org.assertj.core.api.Assertions.assertThat(receivedRegions).hasSameElementsAs(expectedRegions);

    }


}
*/