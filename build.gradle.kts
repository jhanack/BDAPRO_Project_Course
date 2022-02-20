import com.google.protobuf.gradle.*
import org.gradle.kotlin.dsl.provider.gradleKotlinDslOf
plugins {
    java
    id("com.google.protobuf") version("0.8.18")
}

group = "org.example"
version = "1.0-SNAPSHOT"
val grpcVersion = "1.43.2" // CURRENT_GRPC_VERSION
val protobufVersion = "3.19.2"
val protocVersion = protobufVersion

repositories {
    mavenCentral()
    maven {
        url = uri("https://maven.exasol.com/artifactory/exasol-releases")
    }
}

tasks.test {
    useJUnitPlatform()
}

dependencies {
    implementation("io.grpc:grpc-protobuf:${grpcVersion}")
    implementation("io.grpc:grpc-stub:${grpcVersion}")
    compileOnly("org.apache.tomcat:annotations-api:6.0.53")
    implementation("com.google.guava:guava:31.0.1-jre")
    // examples/advanced need this for JsonFormat
    implementation("com.google.protobuf:protobuf-java-util:${protobufVersion}")

    runtimeOnly("io.grpc:grpc-netty-shaded:${grpcVersion}")
    testImplementation("io.grpc:grpc-testing:${grpcVersion}")
    testImplementation("org.mockito:mockito-core:3.4.0")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.8.1")
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.8.1")
    testImplementation("com.palantir.docker.compose:docker-compose-junit-jupiter:1.7.0")
    testImplementation("org.assertj:assertj-core:3.22.0")
    testImplementation("org.junit.jupiter:junit-jupiter-params:5.8.1")
    testImplementation("org.javatuples:javatuples:1.2")
    // https://mvnrepository.com/artifact/org.mariadb.jdbc/mariadb-java-client

    if (JavaVersion.current().isJava9Compatible) {
        // Workaround for @javax.annotation.Generated
        // see: https://github.com/grpc/grpc-java/issues/3633
        compile("javax.annotation:javax.annotation-api:1.3.1")
    }
    protobuf(files("ext/"))


    //JDBC Drivers
    implementation("org.postgresql:postgresql:42.3.1")
    implementation("org.mariadb.jdbc:mariadb-java-client:3.0.3")
    implementation("com.exasol:exasol-jdbc:6.0.0")
    // Adding dependency for configuration from custom sourceSet
    //"sampleProtobuf"(files("ext/"))

}
sourceSets{
    main {
        java {
            srcDir("build/generated/source/proto/main/grpc")
            srcDir("build/generated/source/proto/main/java")
        }
    }
}
protobuf {
    protoc {
        // The artifact spec for the Protobuf Compiler
        artifact = "com.google.protobuf:protoc:3.6.1"
    }
    plugins {
        // Optional: an artifact spec for a protoc plugin, with "grpc" as
        // the identifier, which can be referred to in the "plugins"
        // container of the "generateProtoTasks" closure.
        id("grpc") {
            artifact = "io.grpc:protoc-gen-grpc-java:1.15.1"
        }
    }
    generateProtoTasks {
        ofSourceSet("main").forEach {
            it.plugins {
                // Apply the "grpc" plugin whose spec is defined above, without options.
                id("grpc")
            }
        }
    }
}