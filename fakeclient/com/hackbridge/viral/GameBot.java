/*
 * Sends simulated data to server, posing as a client.
 * Uses a random walk model.
 */

package com.hackbridge.viral;

import java.io.IOException;
import java.io.FileInputStream;
import java.net.Socket;

public class GameBot extends Thread {

    // attributes

    private String          server;     // string, server IP address
    private int             port;       // port of server
    private long            id;         // unique ID of a client
    private PhysicalState   physState;  // physical state
    private AwarenessState  awareState; // awareness state
    private RoleState       roleState;  // role in the game
    private double          longit;     // position: longitude
    private double          latit;      // position: latitude
    private double          speed;      // speed of bot
    private double          meanTime;   // mean time between sending messages
    private boolean         running;    // is a round on?
    private MessageSender   ms;         // client side of connection

    // constructors

    // input is bot's initial position
    public GameBot(double mLongit, double mLatit, double mSpeed,
                   double mMeanTime, String mServer, int mPort) {
        longit      = mLongit;
        latit       = mLatit;
        speed       = mSpeed;
        meanTime    = mMeanTime;
        server      = mServer;
        port        = mPort;
        // default parameters
        physState = PhysicalState.SUSCEPTIBLE;
        awareState = AwarenessState.UNAWARE;
        roleState = RoleState.HUMAN;
        // not yet known
        id = -1;
    }

    // methods

    // sets the bot's ID
    public void setIdentity(long mId) {
        id = mId;
    }

    // sets the bot's role
    public void setRole(RoleState mRoleState) {
        roleState = mRoleState;
    }

    // this method changes the bot's physical state (both physical and
    // awareness)
    // also handles sending the code to the server
    public void changeState(ChangeMessage mess) {
        physState = mess.getInfected();
        awareState = mess.getAware();
        if (roleState == RoleState.HUMAN && awareState == AwarenessState.AWARE
         && physState == PhysicalState.SUSCEPTIBLE) {
            ms.sendMessage(new CodeMessage(id, mess.getCode()));
        }
    }

    // change state of playing
    // used to control whether messages get sent to the server
    public void setRoundOn(boolean b) {
        running = b;
    }

    // main behaviour of the bot
    // runs until an exception gets thrown or JVM exits
    @Override
    public void run() {
        System.out.println("Creating a bot");
        running = false;
        double waitTime = 0.0;
        try {
            // handle TCP setup
            System.out.println("Creating a Socket...");
            Socket s = new Socket(server, port);
            System.out.println("Socket created!");
            ms = new MessageSender(s);
            MessageReceiver mr = new MessageReceiver(this, s);
            mr.setDaemon(true);
            System.out.println("About to start receiver");
            mr.start();
            System.out.println("About to start sending messages");
            // main loop of bot
            while (true) {
                if (id == -1) {
                    // still not identified
                    System.out.println("About to get introduced");
                    ms.sendMessage(new HelloNewMessage());
                    System.out.println("HelloNewMessage object sent");
                } else if (running) {
                    // round is on, ID exists
                    System.out.println(id + " is about to send position");
                    // send position message to server
                    ms.sendMessage(new PositionMessage(id,
                                   new LocationWrapper(longit, latit, 0.0)));
                }
                try {
                    // find time between updates
                    waitTime = -meanTime * Math.log(1.0 - Math.random());
                    // move bot
                    longit += (-speed + 2.0 * Math.random() * speed)
                                * waitTime / 1000.0;
                    latit += (-speed + 2.0 * Math.random() * speed)
                                * waitTime / 1000.0;
                    // wait between updates, variable time, simulating some
                    // realistic inputs
                    Thread.sleep((int) waitTime);
                }
                catch (InterruptedException e) {
                    System.out.println("InterruptedException raised by bot");
                    // we hope everything is fine
                }
            }
        } catch (IOException e) {
            // network failure
            System.out.println("Cannot connect to " + server + ", port " + port);
            return;
        }
    }

}
