package com.hackbridge.viral;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.file.Files;

/**
 * Created by PetarV on 25/03/2016.
 */
public class Tikzer {
    private static int maxLogs = 100; // this should be enough to avoid any issues with loading the image
    private HttpServer tikzer;
    private String mostRecentLog;
    private int currentId;

    public Tikzer(int port) {
        try {
            currentId = 0;
            mostRecentLog = "";
            tikzer = HttpServer.create(new InetSocketAddress(port), 0);
            tikzer.createContext("/viz", new VizHandler(this));
            tikzer.setExecutor(null); // creates a default executor
            tikzer.start();
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }

    public void addLog(StateLog s) {
        // TODO add some tikz generators here.
        // This will require spawning them in a separate thread - otherwise we risk blocking up the whole server
        // and therefore some further synchronisation will be needed to make sure workers don't overwrite one another.
    }

    static class VizHandler implements HttpHandler {
        Tikzer tkz;

        public VizHandler(Tikzer tkz) {
            this.tkz = tkz; // this is by reference, so always store a pointer to its parent
        }

        public void handle(HttpExchange t) throws IOException {
            File file = new File(tkz.mostRecentLog);
            t.sendResponseHeaders(200, file.length());
            OutputStream outputStream = t.getResponseBody();
            Files.copy(file.toPath(), outputStream);
            outputStream.close();
        }
    }
}
