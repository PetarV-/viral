package com.hackbridge.viral;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.Socket;

import android.util.Log;

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
                        Log.d("LAG-INPUT", "Got a ChangeMessage");
                        ma.setAwareness(((ChangeMessage) mess).getAware(), ((ChangeMessage) mess).getCode());
                        ma.setPhysicalState(((ChangeMessage) mess).getInfected());
                    } else if (mess instanceof StopMessage) {
                        Log.d("LAG-INPUT", "Got a StopMessage");
                        ma.setRoundOn(false);
                    } else if (mess instanceof StartMessage) {
                        Log.d("LAG-INPUT", "Got a StartMessage"); 
                        ma.setIdentity(((StartMessage) mess).getId());
                        ma.setAwareness(((StartMessage) mess).getAware(), ((StartMessage) mess).getCode());
                        ma.setPhysicalState(((StartMessage) mess).getInfected());
                        ma.setRoundOn(((StartMessage) mess).isRunning());
                    } else {
                        Log.d("LAG-INPUT", "Unexpected Message object");
                        // treated silently
                    }
                } catch (ClassNotFoundException e) {
                    Log.d("LAG-INPUT", "Unknown class received from server");
                    // we're doing this silently
                }
            }
        } catch (IOException e) {
            Log.d("LAG-INPUT", "IOException caught in MessageReceiver, thread exiting");
            return;
        }
    }

}
