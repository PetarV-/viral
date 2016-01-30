package com.hackbridge.viral;

import java.io.IOException;
import java.net.Socket;

public class NetworkTest {
    public static void main(String[] args) {
        String server;
        int port;
        try {
            server = args[0];
            port = Integer.parseInt(args[1]);
        } catch (NumberFormatException | ArrayIndexOutOfBoundsException e) {
            System.out.println("O NOES, ARGS ARE BAD!!");
            return;
        }
        try {
            Socket s = new Socket(server, port);
            MessageSender ms = new MessageSender(s);
            MessageReceiver mr = new MessageReceiver(s);
            ms.setDaemon(true);
            mr.setDaemon(true);
            mr.run();
            ms.run();
            ms.sendMessage(new HelloMessage(1));
        } catch (IOException e) {
            System.out.println("Cannot connect to " + server + ", port " + port);
            return;
        }
    }
}

