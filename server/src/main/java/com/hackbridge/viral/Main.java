package com.hackbridge.viral;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;

public class Main {
    private static LocationState locState;
    private static ConcurrentLinkedQueue<Message> queue;
    private static Map<Long, ClientHandler> handlers = new HashMap<Long, ClientHandler>();

    private static boolean isRunning = false;
    private static long roundDuration = 900000; // 15 min = 900 s = 900000 ms
    private static long delayBetweenRounds = 15000; // 15 s = 15000 ms
    private static Timer timer = new Timer();

    private static Random random = new Random();
    private static int codeSize = 7;
    private static String code;

    private static TimerTask startTask = new TimerTask() {
        @Override
        public void run() {
            startRound();
        }
    };

    private static TimerTask stopTask = new TimerTask() {
        @Override
        public void run() {
            stopRound();
        }
    };

    public static String getCode() {
        return code;
    }

    public static boolean isRunning() {
        return isRunning;
    }

    // Starts a round
    public static void startRound() {
        isRunning = true;
        code = "";
        for (int i=0;i<codeSize;i++) {
            code += random.nextInt(10);
        }
        for (Map.Entry<Long, ClientHandler> kvp : handlers.entrySet()) {
            long id = kvp.getKey();
            ClientHandler handler = kvp.getValue();
            StartMessage sm = locState.onConnect(id);
            sm.setIsRunning(true);
            handler.sendMessage(sm);
        }
        timer.schedule(stopTask, roundDuration);
    }

    // Stop a round
    public static void stopRound() {
        isRunning = false;
        for (ClientHandler handler : handlers.values()) {
            handler.sendMessage(new StopMessage());
        }
        queue = new ConcurrentLinkedQueue<Message>();
        locState.reset();
        timer.schedule(startTask, delayBetweenRounds);
    }

    // Send a message (ChangeMessage)
    public static void changeState(ChangeMessage chg, long id) {
        if (chg.getAware() == AwarenessState.AWARE) {
            chg.setCode(code);
        }
        handlers.get(id).sendMessage(chg);
    }

    public static void main(String[] args) {
        locState = new LocationState();
        queue = new ConcurrentLinkedQueue<Message>();
        startRound();
        Thread input = new Thread() {
            @Override
            public void run() {
                try {
                    System.out.println(InetAddress.getLocalHost());
                    ServerSocket ss = new ServerSocket(25000);
                    while (true) {
                        System.out.println("Accepting...");
                        Socket s = ss.accept();
                        System.out.println("Socket accepted!");
                        final IdHandlerPair ihp = ClientHandler.fromSocket(s, locState, queue);
                        if (ihp != null) {
                            handlers.put(ihp.getId(), ihp.getHandler());
                            new Thread() {
                                @Override
                                public void run() {
                                    ihp.getHandler().listen();
                                }
                            }.start();
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
                    locState.onLocationChange(pm.getId(), lw);
                    System.out.println(lw.getLatitude() + " " + lw.getLongitude() + " " + lw.getAltitude());
                } else if (front instanceof CodeMessage) {
                    System.out.println("Received CodeMessage!");
                    CodeMessage cm = (CodeMessage)front;
                    if (code.equals(cm.getCode())) {
                        locState.onVaccinate(cm.getId());
                    }
                    System.out.println("id = " + cm.getId() + ", Code = " + cm.getCode());
                } else if (front instanceof DisconnectMessage) {
                    System.out.println("Received DisconnectMessage!");
                    DisconnectMessage dm = (DisconnectMessage)front;
                    System.out.println("id = " + dm.getId());
                    handlers.remove(dm.getId());
                    locState.onDisconnect(dm.getId());
                }
            }
        }
    }
}
