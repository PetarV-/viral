package com.hackbridge.viral;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.file.Files;

// For the demo! Yay pretty graphs!
public class Main {

    public static void main(String[] args) throws Exception {
        HttpServer server = HttpServer.create(new InetSocketAddress(8000), 0);
        server.createContext("/viz", new VizHandler());
        server.setExecutor(null); // creates a default executor
        server.start();
    }

    static class VizHandler implements HttpHandler {
        public void handle(HttpExchange t) throws IOException {
            System.out.println("Sending file...");
            File file = new File("ret.png");
            t.sendResponseHeaders(200, file.length());
            OutputStream outputStream = t.getResponseBody();
            Files.copy(file.toPath(), outputStream);
            outputStream.close();
            System.out.println("Sent file.");
        }
    }
}
