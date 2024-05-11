package com.harvey.server;

import com.harvey.handler.HttpServerRequestHandler;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServer;

/**
 * @author Harvey Suen
 */
public class VertxRpcServer implements RpcServer {
    @Override
    public void doStart(int port) {
        Vertx vertx = Vertx.vertx();
        
        HttpServer server = vertx.createHttpServer();
        
        server.requestHandler(new HttpServerRequestHandler());
        
        server.listen(port, (res) -> {
            if (res.succeeded()) {
                System.out.println("Server is now listening on port " + port);
            } else {
                System.err.println("Failed to start server: " + res.cause());
            }
        });
    }
}
