/*
 * Sends simulated random-ish walk data to server, posing as
 * a client.
 * More detailed & useful comments coming soon.
 * Class currently being maintained by Andrej.
 */

package com.hackbridge.viral;

import java.io.IOException;
import java.io.FileInputStream;
import java.net.Socket;

public class GameBot extends Thread {

    // static attributes, deal with TCP connection

    private static String server;   // 188.166.154.60 default
    private static int port;        // 25000 default

    // attributes

    private long           id;          // unique ID of a client
    private PhysicalState  physState;   // physical state
    private AwarenessState awareState;  // awareness state
    private RoleState      roleState;   // role in the game
    private double         longit;      // position: longitude
    private double         latit;       // position: latitude
    private boolean        running;     // is a round on?
    private MessageSender  ms;          // client side of connection

    // constructors

    // input is bot's initial position
    public GameBot(double mLongit, double mLatit) {
        longit = mLongit;
        latit = mLatit;
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
                    // TODO: more general behaviour, specified in file?
                    longit += 0.0004 * Math.random();
                    latit += 0.0004 * Math.random();
                } else if (running) {
                    // round is on, ID exists
                    System.out.println(id + " is about to send position");
                    // TODO: more general behaviour, specified in file?
                    longit += 0.00008 * Math.random();
                    latit += 0.00008 * Math.random();
                    // send position message to server
                    ms.sendMessage(new PositionMessage(id,
                                   new LocationWrapper(longit, latit, 0.0)));
                }
                try {
                    // wait between updates, variable time, simulating some
                    // realistic inputs
                    Thread.sleep(15000 + (int)(5000 * Math.random()));
                    // TODO: write in a possibly more elegant way
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

    public static void main(String[] args) {
        if (args.length != 3) {
            // wrong usage
            System.out.println("Usage: java com.hackbridge.viral.GameBot "
                             + "<bot parameter file> <server> <port>");
            return;
        }
        try {
            String filename;
            server = args[1];
            port = Integer.parseInt(args[2]);
        } catch (NumberFormatException e) {
            // wrong usage
            System.out.println("Usage: java com.hackbridge.viral.GameBot "
                             + "<bot parameter file> <server> <port>");
        }
        FileInputStream fis = null;
        try {
            fis = new FileInputStream(args[0]);
            // TODO: actually read from file and use it
            for (int i = 0; i < 10; i++) {
                GameBot bot = new GameBot(52.2042, 0.1198);
                bot.setDaemon(false); // JVM must not exit!
                bot.start();
            }
        } catch (IOException e) {
            System.out.println("Error reading from file " + args[0]);
        } finally {
            try {
                fis.close();
            } catch (IOException ioe) {
                // ignore
            }
        }
        // TODO: friendlier interface?
    }
}
