package com.hackbridge.viral;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Created by PetarV on 30/01/2016.
 */
public class ClientHandler {
    private Socket socket;
    private ObjectInputStream ois;
    private ObjectOutputStream oos;
    private long id;
    private ConcurrentLinkedQueue<Message> queue;

    public ClientHandler(Socket socket, long id, ConcurrentLinkedQueue<Message> queue) {
        this.socket = socket;
        try {
            this.ois = new ObjectInputStream(socket.getInputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            this.oos = new ObjectOutputStream(socket.getOutputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
        this.id = id;
        this.queue = queue;
    }

    public void listen() {
        try {
            while (true) {

            }
        } catch (Exception e) { // Treat any exception as a disconnect

        }
    }

    public void sendMessage(Message msg) {
        try {
            oos.writeObject(msg);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
