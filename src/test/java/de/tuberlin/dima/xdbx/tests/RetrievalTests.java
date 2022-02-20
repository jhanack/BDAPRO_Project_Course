package de.tuberlin.dima.xdbx.tests;

import com.palantir.docker.compose.DockerComposeExtension;
import com.palantir.docker.compose.configuration.ShutdownStrategy;
import de.tuberlin.dima.xdbx.client.Client;
import de.tuberlin.dima.xdbx.client.ClientFactory;
import de.tuberlin.dima.xdbx.client.SimpleClientFactory;
import de.tuberlin.dima.xdbx.connector.DBConnectionDetails;
import de.tuberlin.dima.xdbx.connector.DBType;
import de.tuberlin.dima.xdbx.connector.DefaultDBTypes;
import de.tuberlin.dima.xdbx.connector.JDBCConnectionDetails;
import de.tuberlin.dima.xdbx.connector.mariadb.MariaDBInstance;
import de.tuberlin.dima.xdbx.connector.postgres.PostgresInstance;
import de.tuberlin.dima.xdbx.node.JDBCConnectionDetailFromGRPCFactory;
import de.tuberlin.dima.xdbx.node.SimpleXDBConnection;
import de.tuberlin.dima.xdbx.node.XDBConnectionDetails;
import de.tuberlin.dima.xdbx.user.SimpleUser;
import de.tuberlin.dima.xdbx.user.User;
import de.tuberlin.dima.xdbx.util.ThrowingSupplier;
import org.assertj.core.api.Assertions;
import org.javatuples.Pair;
import org.javatuples.Quartet;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Stream;

public class RetrievalTests {

    ClientFactory clientFactory = new SimpleClientFactory(new JDBCConnectionDetailFromGRPCFactory());

    @RegisterExtension
    public static DockerComposeExtension dockerCompose = DockerComposeExtension.builder()
            .file("src/test/resources/docker-compose.yml")
            .shutdownStrategy(ShutdownStrategy.KILL_DOWN)
            .build();

    @ParameterizedTest
    @MethodSource("mixedDBProvider")
    public void testRetrievalFromForeignTable(DBType type1,
                                              DBType type2,
                                              ThrowingSupplier<TestInstance, Exception> instance1Supplier,
                                              XDBConnectionDetails xdbConnectionDetails1,
                                              ThrowingSupplier<Connection, Exception> connectionSupplier,
                                              Function<String, String> queryModifier,
                                              ThrowingSupplier<TestInstance, Exception> instance2Supplier,
                                              XDBConnectionDetails xdbConnectionDetails2,
                                              DBConnectionDetails innerDBConnection) throws Throwable {
        System.out.println("Testing " + type1.getName() + " with " + type2.getName());
        TestInstance instance1 = instance1Supplier.get();
        TestInstance instance2 = instance2Supplier.get();
        Throwable throwable = null;
        try {

            Client client1 = clientFactory.create(xdbConnectionDetails1);
            Client client2 = clientFactory.create(xdbConnectionDetails2);


            client2.createView("someView", "SELECT name, region FROM schema_b WHERE sales > 200", instance2.getDbConnectionDetails());
            client1.createForeignTable("remote",
                    "someView",
                    xdbConnectionDetails2,
                    instance1.getDbConnectionDetails(),
                    innerDBConnection);

            String query = "SELECT uid, region FROM schema_a JOIN \"1_remote\" USING (name)";
            query = queryModifier.apply(query);

            Connection connection = connectionSupplier.get();
            Statement statement = connection.createStatement();
            statement.execute(query);


            ResultSet resultSet = statement.getResultSet();
            List<Integer> expectedUids = List.of(1, 2, 3);
            List<String> expectedRegions = List.of("europe", "europe", "asia");
            List<Integer> receivedUids = new LinkedList<>();
            List<String> receivedRegions = new LinkedList<>();
            while (resultSet.next()) {
                receivedUids.add(resultSet.getInt(1));
                receivedRegions.add(resultSet.getString(2));
            }
            resultSet.close();
            statement.close();

            Assertions.assertThat(receivedUids).hasSameElementsAs(expectedUids);
            Assertions.assertThat(receivedRegions).hasSameElementsAs(expectedRegions);
        } catch (Throwable e){
            throwable = e;
        }
        instance1.stop();
        instance2.stop();
        Thread.sleep(1000);
        if (throwable != null) throw throwable;

    }

    public static Quartet<Map<DBType, Pair<ThrowingSupplier<TestInstance, Exception>, XDBConnectionDetails>>,
            Map<DBType, Pair<ThrowingSupplier<TestInstance, Exception>, XDBConnectionDetails>>,
            Map<DBType, Pair<ThrowingSupplier<Connection, Exception>, Function<String, String>>>,
            Map<DBType, DBConnectionDetails>>
    createDBProviders() {
        Map<DBType, Pair<ThrowingSupplier<TestInstance, Exception>, XDBConnectionDetails>> firstInstance = new HashMap<>();
        Map<DBType, Pair<ThrowingSupplier<TestInstance, Exception>, XDBConnectionDetails>> secondInstance = new HashMap<>();
        Map<DBType, Pair<ThrowingSupplier<Connection, Exception>, Function<String, String>>> firstAdditionalInfo = new HashMap<>();

        Map<DBType, DBConnectionDetails> innerDBConnections = new HashMap<>();

        ClientFactory clientFactory = new SimpleClientFactory(new JDBCConnectionDetailFromGRPCFactory());

        Map<User, String> users1 = new HashMap<>();
        users1.put(new SimpleUser("max", 1, simpleUser -> {}, simpleUser -> {}), "123");
        users1.put(new SimpleUser("moritz", 2, simpleUser -> {}, simpleUser -> {}), "456");
        Map<User, String> users2 = new HashMap<>();
        users2.put(new SimpleUser("tim", 1, simpleUser -> {}, simpleUser -> {}), "abc");
        users2.put(new SimpleUser("tom", 2, simpleUser -> {}, simpleUser -> {}), "def");

        Properties properties = new Properties();
        properties.setProperty("user", "root");
        properties.setProperty("password", "secret");

        firstInstance.put(DefaultDBTypes.POSTGRES, Pair.with(
                () -> {
                    Map<String, DBType> types = new HashMap<>();
                    types.put("localhost:8080", DefaultDBTypes.POSTGRES);
                    //types1.put("pg-1:8080", DefaultDBTypes.POSTGRES);

                    Map<String, String> hostMappings = new HashMap<>();
                    hostMappings.put("pg-1:5432", "localhost:8080");
                    return TestInstance.createInstanceWithSimpleImplementation(
                            7000,
                            users1,
                            types,
                            hostMappings,
                            DefaultDBTypes.POSTGRES,
                            new PostgresInstance(clientFactory),
                            "localhost",
                            8080,
                            "bdapro_user",
                            "bdapro_password",
                            "bdapro_database");
                },
                new SimpleXDBConnection("localhost", 7000, "max", "123"))
        );
        secondInstance.put(DefaultDBTypes.POSTGRES, Pair.with(
                () -> {
                    Map<String, DBType> types = new HashMap<>();
                    types.put("localhost:8081", DefaultDBTypes.POSTGRES);

                    Map<String, String> hostMappings = new HashMap<>();
                    hostMappings.put("pg-2:5432", "localhost:8081");

                    return TestInstance.createInstanceWithSimpleImplementation(
                            7001,
                            users2,
                            types,
                            hostMappings,
                            DefaultDBTypes.POSTGRES,
                            new PostgresInstance(clientFactory),
                            "localhost",
                            8081,
                            "bdapro_user",
                            "bdapro_password",
                            "bdapro_database");
                },
                new SimpleXDBConnection("localhost", 7001, "tim", "abc")
        ));

        firstInstance.put(DefaultDBTypes.MARIADB, Pair.with(
                () -> {
                    Map<String, DBType> types = new HashMap<>();
                    types.put("localhost:8082", DefaultDBTypes.MARIADB);
                    //types1.put("pg-1:8080", DefaultDBTypes.POSTGRES);

                    Connection adminConnection = DriverManager.getConnection("jdbc:mariadb://localhost:8082/bdapro_database", properties);
                    Map<String, String> hostMappings = new HashMap<>();
                    hostMappings.put("mdb-1:3306", "localhost:8082");
                    return TestInstance.createInstanceWithSimpleImplementation(
                            7002,
                            users1,
                            types,
                            hostMappings,
                            DefaultDBTypes.MARIADB,
                            new MariaDBInstance(adminConnection, clientFactory),
                            "localhost",
                            8082,
                            "bdapro_user",
                            "bdapro_password",
                            "bdapro_database");
                },
                new SimpleXDBConnection("localhost", 7002, "max", "123")) );
        secondInstance.put(DefaultDBTypes.MARIADB, Pair.with(
                () -> {
                    Map<String, DBType> types = new HashMap<>();
                    types.put("localhost:8083", DefaultDBTypes.MARIADB);
                    //types1.put("pg-1:8080", DefaultDBTypes.POSTGRES);

                    Connection adminConnection = DriverManager.getConnection("jdbc:mariadb://localhost:8083/bdapro_database", properties);
                    Map<String, String> hostMappings = new HashMap<>();
                    hostMappings.put("mdb-2:3306", "localhost:8083");
                    return TestInstance.createInstanceWithSimpleImplementation(
                            7003,
                            users2,
                            types,
                            hostMappings,
                            DefaultDBTypes.MARIADB,
                            new MariaDBInstance(adminConnection, clientFactory),
                            "localhost",
                            8083,
                            "bdapro_user",
                            "bdapro_password",
                            "bdapro_database");
                },
                new SimpleXDBConnection("localhost", 7003, "tim", "abc"))
        );


        innerDBConnections.put(DefaultDBTypes.POSTGRES,
                new JDBCConnectionDetails("pg-2", 5432, "bdapro_user", "bdapro_password", "bdapro_database"));

        innerDBConnections.put(DefaultDBTypes.MARIADB,
                new JDBCConnectionDetails("mdb-2", 3306, "bdapro_user", "bdapro_password", "bdapro_database"));

        firstAdditionalInfo.put(DefaultDBTypes.POSTGRES, Pair.with(
                () -> DriverManager.getConnection("jdbc:postgresql://localhost:8080/bdapro_database", "bdapro_user", "bdapro_password"),
                Function.identity()
        ));
        firstAdditionalInfo.put(DefaultDBTypes.MARIADB, Pair.with(
                () -> DriverManager.getConnection("jdbc:mariadb://localhost:8082/bdapro_database", "bdapro_user", "bdapro_password"),
                (string) -> string.replace("\"", "")
        ));
        return Quartet.with(firstInstance, secondInstance, firstAdditionalInfo, innerDBConnections);
    }


    public static Stream<Arguments> mixedDBProvider() {
        Quartet<Map<DBType, Pair<ThrowingSupplier<TestInstance, Exception>, XDBConnectionDetails>>,
                Map<DBType, Pair<ThrowingSupplier<TestInstance, Exception>, XDBConnectionDetails>>,
                Map<DBType, Pair<ThrowingSupplier<Connection, Exception>, Function<String, String>>>,
                Map<DBType, DBConnectionDetails>> providers = createDBProviders();

        Map<DBType, Pair<ThrowingSupplier<TestInstance, Exception>, XDBConnectionDetails>> firstInstance = providers.getValue0();
        Map<DBType, Pair<ThrowingSupplier<TestInstance, Exception>, XDBConnectionDetails>> secondInstance = providers.getValue1();
        Map<DBType, Pair<ThrowingSupplier<Connection, Exception>, Function<String, String>>> firstAdditionalInfo = providers.getValue2();

        Map<DBType, DBConnectionDetails> innerDBConnections = providers.getValue3();

        List<DBType> typesToUse = List.of(DefaultDBTypes.POSTGRES, DefaultDBTypes.MARIADB);
        List<Arguments> args = new ArrayList<>();

        for(DBType firstType : typesToUse) {
            Pair<ThrowingSupplier<TestInstance, Exception>, XDBConnectionDetails> first = firstInstance.get(firstType);
            Pair<ThrowingSupplier<Connection, Exception>, Function<String, String>> firstAdditional = firstAdditionalInfo.get(firstType);

            for (DBType secondType : typesToUse){
                Pair<ThrowingSupplier<TestInstance, Exception>, XDBConnectionDetails> second = secondInstance.get(secondType);
                DBConnectionDetails intraConnection = innerDBConnections.get(secondType);

                Arguments arg = Arguments.of(
                        firstType,
                        secondType,
                        first.getValue0(),
                        first.getValue1(),
                        firstAdditional.getValue0(),
                        firstAdditional.getValue1(),
                        second.getValue0(),
                        second.getValue1(),
                        intraConnection
                );
                args.add(arg);
            }
        }

        return args.stream();
    }

}
