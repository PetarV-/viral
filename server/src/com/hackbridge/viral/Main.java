package com.hackbridge.viral;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedQueue;

public class Main {
    private static LocationState locState;
    private static ConcurrentLinkedQueue<Message> queue;
    private static Map<Long, ClientHandler> handlers = new HashMap<>();

    // Starts a round
    public static void startRound() {

    }

    // Stop a round
    public static void stopRound() {

    }

    // Send a message (ChangeMessage)
    public static void changeState(ChangeMessage chg, long id) {

    }

    public static void main(String[] args) {
        locState = new LocationState();
        queue = new ConcurrentLinkedQueue<>();
        Thread input = new Thread() {
            @Override
            public void run() {
                try {
                    System.out.println(InetAddress.getLocalHost());
                    ServerSocket ss = new ServerSocket(25000);
                    while (true) {
                        Socket s = ss.accept();
                        IdHandlerPair ihp = ClientHandler.fromSocket(s, locState, queue);
                        if (ihp != null) {
                            handlers.put(ihp.getId(), ihp.getHandler());
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        };
        input.setDaemon(true);
        input.start();
        while (true) {
            if (!queue.isEmpty()) {
                Message front = queue.poll();
                if (front instanceof PositionMessage) {
                    System.out.println("Received PositionMessage!");
                    PositionMessage pm = (PositionMessage)front;
                    LocationWrapper lw = pm.getLocationWrapper();
                    System.out.println(lw.getLatitude() + " " + lw.getLongitude() + " " + lw.getAltitude());
                } else if (front instanceof DisconnectMessage) {
                    System.out.println("Received DisconnectMessage!");
                    DisconnectMessage dm = (DisconnectMessage)front;
                    System.out.println("id = " + dm.getId());
                }
            }
        }
    }
}
