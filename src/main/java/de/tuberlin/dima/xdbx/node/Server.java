package de.tuberlin.dima.xdbx.node;

import io.grpc.ServerBuilder;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class Server {
    private final io.grpc.Server server;
    private final int port;

    public Server(int port, NodeServiceGrpc service){
        this.port = port;
        this.server = ServerBuilder.forPort(port).addService(service).build();
    }

    public void stop() throws InterruptedException {
        this.server.shutdown().awaitTermination(30, TimeUnit.SECONDS);
    }

    public void start() throws IOException {
        this.server.start();
        System.out.println("Started Server on port " + port);
        Runtime.getRuntime().addShutdownHook(new Thread(){
            @Override
            public void run() {
                System.out.println("Shutting down server because the JVM is shutting down");
                try {
                    Server.this.stop();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                System.out.println("Server shut down");
            }
        });

    }
}
