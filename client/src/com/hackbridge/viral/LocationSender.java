package com.hackbridge.viral;

import java.io.IOException;
import java.ObjectOutputStream;
import java.net.socket;

public class LocationSender implements Runnable {
    
    private String mess;
    private boolean updated;
    private Socket s;
    
    public LocationSender(Socket s) {
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
            break;
        }
    }
    public void getMessage(String m) {
        mess = m;
        updated = true;
    }

}
