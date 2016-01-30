package com.hackbridge.viral;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.Socket;

public class MessageReceiver extends Thread {
    
    private Message mess;
    private Socket s;
    private MainActivity ma;

    public MessageReceiver(MainActivity ma, Socket s) {
        this.ma = ma;
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
                        ma.setAwareness(((ChangeMessage) mess).getAware());
                        ma.setInfected(((ChangeMessage) mess).getInfected());
                    } else if (mess instanceof StopMessage) {
                        System.out.println("Got a StopMessage");
                        ma.setRoundOn(false);
                    } else if (mess instanceof StartMessage) {
                        System.out.println("Got a StartMessage"); 
                        ma.setIdentity(((StartMessage) mess).getId());
                        ma.setAwareness(((StartMessage) mess).getAware());
                        ma.setInfected(((StartMessage) mess).getInfected());
                        ma.setRoundOn(((StartMessage) mess).isRunning());
                    } else {
                        System.out.println("Unexpected Message object");
                        // treated silently
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
