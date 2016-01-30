package com.hackbridge.viral;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

public class Main {
    // Starts a round
    public static void startRound() {

    }

    // Stop a round
    public static void stopRound() {

    }

    // Send a message (ChangeMessage)
    public static void changeState(ChangeMessage chg) {

    }

    public static void main(String[] args) {
        try {
            System.out.println(InetAddress.getLocalHost());
            ServerSocket ss = new ServerSocket(25000);
            Socket s = ss.accept();
            InputStream is = s.getInputStream();
            ObjectInputStream ois = new ObjectInputStream(is);
            Message m = (Message)ois.readObject();
            if (m instanceof HelloNewMessage) {
                System.out.println("YAY");
            }
            StartMessage msg = new StartMessage(9876543, PhysicalState.SUSCEPTIBLE, AwarenessState.UNAWARE);
            System.out.println("Accepted socket!");
            ObjectOutputStream oos = new ObjectOutputStream(s.getOutputStream());
            oos.writeObject(msg);
            System.out.println("KEK");
            m = (Message)ois.readObject();
            System.out.println("WEW");
            if (m instanceof HelloMessage) {
                HelloMessage hm = (HelloMessage)m;
                System.out.println(hm.getId());
            }
            //ois.close();
            //oos.close();
            System.out.println("SENT");
            while (true) { }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        //new LocationStateTest();
	// write your code here
    }
}
