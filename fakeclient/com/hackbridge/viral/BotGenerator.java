/*
 * This is the entry point for the bot generator.
 * The main method is here and takes the following arguments:
 *      filename:   name of the file that contains parameters about the bots,
 *                  adheres to the specification described in the relevant
 *                  documentation file
 *      server:     server address
 *      port:       port number at which server takes requests
 * Every bot establishes a separate TCP connection with the server and is a
 * separate thread.
 */

package com.hackbridge.viral;

import java.io.IOException;
import java.io.File;
import java.util.Scanner;
import java.net.Socket;

public class BotGenerator {

    // attributes deal with TCP connection

    private static String server;   // 188.166.154.60 default
    private static int port;        // 25000 default

    // attributes deal with bot control
    
    private static final int MAXCOUNT = 500;
    private static int botCount = 0;
    private static GameBot[] bot;

    // reading the bot specification file
    
    private static void readBotFile(String filename) {
        File file = null;
        Scanner sc = null;
        int version;
        double longitude;
        double latitude;
        double speed;
        int rate;
        int ratedev; 
        try {
            // file I/O
            file = new File(filename);
            sc = new Scanner(file);
            version = sc.nextInt();
            // check version count
            if (version != 1) {
                // invalid version
                throw new IOException();
            }
            while (sc.hasNext("#")) {
                sc.nextLine();
            }
            botCount = sc.nextInt();
            // check whether bot count is valid
            if (botCount < 0 || botCount > MAXCOUNT) {
                System.out.println("Invalid number of bots.");
                System.out.println("Must be between 0 and " + MAXCOUNT + ".");
                throw new IOException();
            }
            bot = new GameBot[botCount];
            // input all bots
            for (int i = 0; i < botCount; i++) {
                while (sc.hasNext("#")) {
                    sc.nextLine();
                }
                longitude = sc.nextDouble();
                while (sc.hasNext("#")) {
                    sc.nextLine();
                }
                latitude = sc.nextDouble();
                while (sc.hasNext("#")) {
                    sc.nextLine();
                }
                speed = sc.nextDouble();
                while (sc.hasNext("#")) {
                    sc.nextLine();
                }
                rate = sc.nextInt();
                while (sc.hasNext("#")) {
                    sc.nextLine();
                }
                ratedev = sc.nextInt();
                bot[i] = new GameBot(longitude, latitude, speed, 
                                     rate, ratedev, server, port);
            }
        } catch (IOException e) {
            System.out.println("Error reading from file " + filename);
            botCount = 0;
        } finally {
            sc.close();
        }
    }

    // main part of program

    public static void main(String[] args) {
        if (args.length != 3) {
            // wrong usage
            System.out.println("Usage: java com.hackbridge.viral.BotGenerator "
                             + "<bot parameter file> <server> <port>");
            return;
        }
        String filename = "";
        try {
            filename = args[0];
            server = args[1];
            port = Integer.parseInt(args[2]);
        } catch (NumberFormatException e) {
            // wrong usage
            System.out.println("Usage: java com.hackbridge.viral.BotGenerator "
                             + "<bot parameter file> <server> <port>");
            return;
        }
        readBotFile(filename);
        for (int i = 0; i < botCount; i++) {
            bot[i].setDaemon(false); // JVM must not exit!
            bot[i].start();
        }
        // TODO: friendlier interface?
    }
}
