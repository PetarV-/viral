package com.hackbridge.viral;

import java.io.IOException;
import java.ObjectputStream;
import java.net.socket;

public class MessageReceiver implements Runnable {
    
    private Message mess;
    private Socket s;

    public MessageReceiver(Socket s) {
        this.s = s;
    }
    @Override
    public void run() {
        try {
            ObjectInputStream ois = new ObjectInputStream(s.getInputStream());
            while (true) {
                mess = null;
                while (mess == null) {
                    mess = (Message) ois.readObject();
                }
                if (mess instanceof ChangeMessage) {
                    // TODO: Stanoje, I need a method for this!
                } else if (mess instanceof StopMessage) {
                    // TODO: Stanoje, tell me what the 'stop round' method is!
                } else if (mess instanceof StartMessage) {
                    // TODO: Stanoje, act!
                } else {
                    System.out.println("IDK");
                    // TODO: do something useful
                }
            }
        } catch (IOException e) {
            System.out.println("o noes!!");
            break;
        }
    }

}
