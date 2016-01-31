package com.hackbridge.viral;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;

import android.util.Log;

public class NetworkTest
{
/*
    public static long           id    = -1;
    public static int            state = 0;
    public static PhysicalState  physState;
    public static AwarenessState awareState;

    public static void runTest()
    {
        Log.d("LAG", "Starting test!!");
        String server = "188.166.154.60";//"94.197.120.86";//"172.20.10.4";
        int port = 25000;
        try
        {
            physState = PhysicalState.SUSCEPTIBLE;
            awareState = AwarenessState.UNAWARE;
            Log.d("LAG", "about to create Socket");
            Socket s = new Socket(server, port);
            Log.d("LAG", "Socket created");
            MessageSender ms = new MessageSender(s);
            MessageReceiver mr = new MessageReceiver(s);
            mr.setDaemon(true);
            Log.d("LAG", "about to start threads");
            mr.start();
            Log.d("LAG", "about to send message");
            while (true)
            {
                // don't want to close anything, really
                Log.d("LAG", "looping");
                try
                {
                    Thread.sleep(2000);
                }
                catch (InterruptedException e)
                {
                    Log.d("LAG", "InterruptedException? What should I do?");
                }
                switch (state)
                {
                    case 0:
                        Log.d("LAG", "about to send");
                        ms.sendMessage(new HelloNewMessage());
                        Log.d("LAG", "sent");
                        break;
                    case 1:
                    case 2:
                    case 3:
                    case 4:
                        if (id != -1)
                        {
                            Log.d("LAG", "about to send location");
                            ms.sendMessage(new PositionMessage(id,
                                    new LocationWrapper(state + 0.0,
                                            state * 1.5, (state + 3.0) * 0.73)));
                            state++;
                        }
                        break;
                    default:
                        return;
                }
            }
        }
        catch (IOException e)
        {
            Log.d("LAG", "Cannot connect to " + server + ", port " + port);
            return;
        }
    }

    public static void main(String[] args)
    {
        runTest();
    }
    */
}
