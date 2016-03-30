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

    public static IdHandlerPair fromSocket(Socket socket, StateManager stateManager, ConcurrentLinkedQueue<Message> queue) {
        try {
            ObjectInputStream ois = new ObjectInputStream(socket.getInputStream());
            ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
            Message m = (Message)ois.readObject();
            StartMessage smsg;
            long id;
            if (m instanceof HelloNewMessage) {
                smsg = stateManager.onConnect();
                id = smsg.getId();
            } else if (m instanceof HelloMessage) {
                HelloMessage hm = (HelloMessage)m;
                smsg = stateManager.onConnect(hm.getId());
                if (smsg == null) { // if this ID wasn't recognised, give a new one
                    smsg = stateManager.onConnect();
                    id = smsg.getId();
                } else {
                    id = hm.getId();
                }
            } else {
                ois.close();
                oos.close();
                return null;
            }
            if (smsg == null) {
                ois.close();
                oos.close();
                return null;
            }
            if (smsg.getAware() == AwarenessState.AWARE) {
                smsg.setCode(Main.getCode());
            }
            smsg.setIsRunning(Main.isRunning());
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
                queue.offer(m);
            }
        } catch (Exception e) { // Treat any exception as a disconnect
            queue.offer(new DisconnectMessage(id));
        }
    }

    public void sendMessage(Message msg) {
        try {
            oos.writeObject(msg);
        } catch (Exception e) { // Treat any exception as a disconnect
            queue.offer(new DisconnectMessage(id));
        }
    }
}
