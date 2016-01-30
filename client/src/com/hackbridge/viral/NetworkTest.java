package com.hackbridge.viral;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.io.ObjectOutputStream;

public class NetworkTest {

    public static long id = -1;
    public static boolean sentAlready = false;

    public static void main(String[] args) {
        System.out.println("woohoo");
        String server;
        int port;
        try {
            server = args[0];
            port = Integer.parseInt(args[1]);
            System.out.println(server + ":" + port);
        } catch (NumberFormatException | ArrayIndexOutOfBoundsException e) {
            System.out.println("O NOES, ARGS ARE BAD!!");
            return;
        }
        try {
            System.out.println("about to create Socket");
            Socket s = new Socket(server, port);
            System.out.println("Socket created");
            MessageSender ms = new MessageSender(s);
            MessageReceiver mr = new MessageReceiver(s);
            mr.setDaemon(true);
            System.out.println("about to start threads");
            mr.start();
            System.out.println("about to send message");
            ms.sendMessage(new HelloNewMessage());
            while (true) {
                // don't want to close anything, really
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    System.out.println("InterruptedException? What should I do?");
                }
                if (id != -1) {
                    System.out.println("I have an ID");
                    if (!sentAlready) {
                        System.out.println("about to send new hello");
                        sentAlready = true;
                        ms.sendMessage(new HelloMessage(id));
                        System.out.println("new Hello sent");
                    }
                }
            }
        } catch (IOException e) {
            System.out.println("Cannot connect to " + server + ", port " + port);
            return;
        }
    }
}

