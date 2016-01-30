package com.hackbridge.viral;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.Socket;

public class MessageReceiver extends Thread {
    
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
                try {
                    while (mess == null) {
                        mess = (Message) ois.readObject();
                    }
                    if (mess instanceof ChangeMessage) {
                        System.out.println("Got a ChangeMessage");
                        // TODO: Stanoje, I need a method for this!
                    } else if (mess instanceof StopMessage) {
                        System.out.println("Got a StopMessage");
                        // TODO: Stanoje, tell me what the 'stop round' method is!
                    } else if (mess instanceof StartMessage) {
                        NetworkTest.id = ((StartMessage) mess).getId();
                        System.out.println("My name is " + NetworkTest.id); 
                        // TODO: Stanoje, act!
                    } else {
                        System.out.println("Unexpected Message object");
                        // TODO: do something useful
                    }
                } catch (ClassNotFoundException e) {
                    System.out.println("Unknown class received from server");
                    // we're doing this silently
                }
            }
        } catch (IOException e) {
            System.out.println("IOException caught in MessageReceiver, thread exiting");
            return;
        }
    }

}
