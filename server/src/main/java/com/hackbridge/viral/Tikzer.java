package com.hackbridge.viral;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.*;
import java.net.InetSocketAddress;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

/**
 * The TikZ visualisation server (Tikzer).
 * It continuously generates TikZ figures from the most recent state log, and HTTP serves the
 * most recently generated figure to the client; listening on port 8000 at /viz.
 */
class Tikzer {
    private static int maxLogs = 100; // this should be enough to avoid any issues with loading the image
    private HttpServer tikzer;
    private StateLog latestLog;
    private String mostRecentLogFile;
    private int currentId;

    private final Object guard = new Object();
    private boolean hasChanged = false;

    private static String base = "ret_";

    Tikzer(int port) {
        try {
            currentId = 0;
            mostRecentLogFile = "";
            tikzer = HttpServer.create(new InetSocketAddress(port), 0);
            tikzer.createContext("/viz", new VizHandler(this));
            tikzer.setExecutor(null); // creates a default executor
            tikzer.start();
            this.launch();
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }

    private void launch() {
        Thread t = new Thread() {
            @Override
            public void run() {
                while (true) {
                    StateLog curr;
                    synchronized (guard) {
                        while (!hasChanged) {
                            try {
                                guard.wait();
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                        curr = latestLog;
                        hasChanged = false;
                    }
                    try {
                        int nextId = (currentId + 1) % maxLogs;
                        mostRecentLogFile = TikzWorker.generate(curr, nextId);
                        currentId = nextId;
                    } catch (IOException ioe) {
                        ioe.printStackTrace();
                    }
                }
            }
        };
        t.setDaemon(true);
        t.start();
    }

    void addLog(StateLog s) {
        synchronized (guard) {
            latestLog = s;
            hasChanged = true;
            guard.notify();
        }
    }

    private static class TikzWorker {
        static void gen_tikz(StateLog log, String tikzName) throws IOException {
            Map<Long, Node> nodes = log.getNodes();
            Map<Long, Integer> positions = log.getPositionMap();
            List<ArrayList<Double>> state = log.getState();

            BufferedWriter tikzFile = new BufferedWriter(new FileWriter(tikzName));
            tikzFile.write("\\documentclass[crop,tikz]{standalone}\n");
            tikzFile.write("\\definecolor{mygreen}{HTML}{006400}\n");
            tikzFile.write("\\definecolor{myred}{HTML}{8B0000}\\n");
            tikzFile.write("\\begin{document}\n");
            tikzFile.write("\\begin{tikzpicture}[transform shape,line width=2pt]\n");

            double R = 1.0;
            int n = nodes.size();
            int i = 0;
            while (2.0 * R * Math.sin(Math.PI / n) < 3) R += 0.1;
            for (Map.Entry<Long, Node> entry : nodes.entrySet()) {
                Long index = entry.getKey();
                Node currNode = entry.getValue();
                double pos = (i * 360.0) / n;
                String color = "white";
                switch (currNode.getPhysicalState()) {
                    case SUSCEPTIBLE: color = "blue"; break;
                    case INFECTED:
                    case CARRIER: color = "myred"; break;
                    case VACCINATED: color = "mygreen"; break;
                }
                char awareness = '\0';
                switch (currNode.getAwarenessState()) {
                    case AWARE: awareness = 'A'; break;
                    case UNAWARE: awareness = 'U'; break;
                }
                tikzFile.write(String.format(
                        "\\node[draw,circle,inner sep=0.25cm,fill=%s,text=white] (N-%d) at (%f:%fcm) [thick] {$%c$};\n",
                        color, index, pos, R, awareness));
                i++;
            }

            HashSet<Long> used = new HashSet<Long>();

            for (Map.Entry<Long, Integer> i_entry : positions.entrySet()) {
                Long i_id = i_entry.getKey();
                int i_pos = i_entry.getValue();
                used.add(i_id);
                for (Map.Entry<Long, Integer> j_entry : positions.entrySet()) {
                    Long j_id = j_entry.getKey();
                    int j_pos = j_entry.getValue();
                    if (used.contains(j_id)) continue;
                    double w = Math.max(0.01, state.get(i_pos).get(j_pos));
                    tikzFile.write(String.format(
                            "\\path(N-%d) edge[-, red, opacity=%f] (N-%d);\n",
                            i_id, w, j_id));
                }
            }

            tikzFile.write("\\end{tikzpicture}\n");
            tikzFile.write("\\end{document}\n");

            tikzFile.close();
        }

        static void build_tikz(String tikzName, String pdfName, String pngName) throws IOException {
            Runtime.getRuntime().exec("exec pdflatex " + tikzName + " &> /dev/null");
            Runtime.getRuntime().exec("exec convert -density 300 " + pdfName + " -quality 90 " + pngName + " &> /dev/null");
        }

        static String generate(StateLog log, int id) throws IOException {
            String tikzName = base + id + ".tex";
            String pdfName = base + id + ".pdf";
            String pngName = base + id + ".png";
            gen_tikz(log, tikzName);
            build_tikz(tikzName, pdfName, pngName);
            return pngName;
        }
    }

    private static class VizHandler implements HttpHandler {
        Tikzer tkz;

        VizHandler(Tikzer tkz) {
            this.tkz = tkz; // this is by reference, so always store a pointer to its parent
        }

        public void handle(HttpExchange t) throws IOException {
            File file = new File(tkz.mostRecentLogFile);
            t.sendResponseHeaders(200, file.length());
            OutputStream outputStream = t.getResponseBody();
            Files.copy(file.toPath(), outputStream);
            outputStream.close();
        }
    }
}
