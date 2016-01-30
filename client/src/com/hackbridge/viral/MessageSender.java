package com.hackbridge.viral;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class MessageSender extends Thread {
    
    private Message mess;
    private boolean updated;
    private Socket s;
    
    public MessageSender(Socket s) {
        this.s = s;
    }
    @Override
    public void run() {
        try {
            ObjectOutputStream oos = new ObjectOutputStream(s.getOutputStream());
            while (true) {
                if (updated) {
                    updated = false;
                    oos.writeObject(mess);
                }
            }
        } catch (IOException e) {
            System.out.println("o noes!!");
            return;
        }
    }
    public void sendMessage(Message m) {
        mess = m;
        updated = true;
    }

}
