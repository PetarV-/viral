package com.hackbridge.viral;

import java.io.IOException;
import java.io.ObjectOutputStream;
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
            ServerSocket ss = new ServerSocket(1500);
            Socket s = ss.accept();
            StartMessage msg = new StartMessage(1, PhysicalState.SUSCEPTIBLE, AwarenessState.UNAWARE);
            System.out.println("Accepted socket!");
            ObjectOutputStream oos = new ObjectOutputStream(s.getOutputStream());
            oos.writeObject(msg);
            oos.close();
            System.out.println("SENT");
        } catch (IOException e) {
            e.printStackTrace();
        }
        //new LocationStateTest();
	// write your code here
    }
}
