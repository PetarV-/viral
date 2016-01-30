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
    private ObjectInputStream ois;
    private ObjectOutputStream oos;
    private long id;
    private ConcurrentLinkedQueue<Message> queue;

    public ClientHandler(ObjectInputStream ois, ObjectOutputStream oos, long id, ConcurrentLinkedQueue<Message> queue) {
        this.ois = ois;
        this.oos = oos;
        this.id = id;
        this.queue = queue;
    }

    public static IdHandlerPair fromSocket(Socket socket, LocationState locState, ConcurrentLinkedQueue<Message> queue) {
        try {
            ObjectInputStream ois = new ObjectInputStream(socket.getInputStream());
            ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
            Message m = (Message)ois.readObject();
            StartMessage smsg;
            long id;
            if (m instanceof HelloNewMessage) {
                smsg = locState.OnConnect();
                id = smsg.getId();
            } else if (m instanceof HelloMessage) {
                HelloMessage hm = (HelloMessage)m;
                smsg = locState.OnConnect(hm.getId());
                id = hm.getId();
            } else {
                ois.close();
                oos.close();
                return null;
            }
            oos.writeObject(smsg);
            return new IdHandlerPair(id, new ClientHandler(ois, oos, id, queue));

        } catch (IOException e) {
            e.printStackTrace();
            return null;
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }

    public void listen() {
        try {
            while (true) {
                Message m = (Message)ois.readObject();
                queue.add(m);
            }
        } catch (Exception e) { // Treat any exception as a disconnect
            queue.add(new DisconnectMessage(id));
        }
    }

    public void sendMessage(Message msg) {
        try {
            oos.writeObject(msg);
        } catch (Exception e) { // Treat any exception as a disconnect
            queue.add(new DisconnectMessage(id));
        }
    }
}
