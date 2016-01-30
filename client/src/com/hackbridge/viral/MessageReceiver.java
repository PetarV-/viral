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
                        System.out.println("OBAMA CHANGE");
                        // TODO: Stanoje, I need a method for this!
                    } else if (mess instanceof StopMessage) {
                        System.out.println("STAHP");
                        // TODO: Stanoje, tell me what the 'stop round' method is!
                    } else if (mess instanceof StartMessage) {
                        System.out.println("GOGOGOGO");
                        // TODO: Stanoje, act!
                    } else {
                        System.out.println("IDK");
                        // TODO: do something useful
                    }
                } catch (ClassNotFoundException e) {
                    System.out.println("naughty unknown class");
                    // we're doing this silently
                }
            }
        } catch (IOException e) {
            System.out.println("o noes!!");
            return;
        }
    }

}
