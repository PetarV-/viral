package com.hackbridge.viral;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.io.ObjectOutputStream;

public class NetworkTest {

    public static long id = -1;
    public static int state = 0;
    public static PhysicalState physState;
    public static AwarenessState awareState; 

    public static void runTest() {
        System.out.println("Starting test!!");
        String server = "172.20.10.4";
        int port = 25000;
        try {
            physState = PhysicalState.SUSCEPTIBLE;
            awareState = AwarenessState.UNAWARE;
            System.out.println("about to create Socket");
            Socket s = new Socket(server, port);
            System.out.println("Socket created");
            MessageSender ms = new MessageSender(s);
            MessageReceiver mr = new MessageReceiver(s);
            mr.setDaemon(true);
            System.out.println("about to start threads");
            mr.start();
            System.out.println("about to send message");
            while (true) {
                // don't want to close anything, really
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    System.out.println("InterruptedException? What should I do?");
                }
                switch (state) {
                    case 0:
                        ms.sendMessage(new HelloNewMessage());
                        break;
                    case 1: case 2: case 3: case 4:
                        if (id != -1) {
                            System.out.println("about to send location");
                            ms.sendMessage(new PositionMessage(id,
                                           new LocationWrapper(state + 0.0,
                                               state * 1.5, (state + 3.0) * 0.73)));
                            state++;
                        }
                        break;
                    default:
                        return;
                }
            }
        } catch (IOException e) {
            System.out.println("Cannot connect to " + server + ", port " + port);
            return;
        }
    }

    public static void main(String[] args) {
        runTest();
    }
}

