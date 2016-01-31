package com.hackbridge.viral;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;

public class GameBot extends Thread
{

    private long           id    = -1;
    private PhysicalState  physState;
    private AwarenessState awareState;
    private RoleState      roleState;
    private double         longit = 52.2042;
    private double         latit = 0.1198;
    private boolean        running;
    private MessageSender  ms;

    public void setIdentity(long mId)
    {
        id = mId;
    }

    public void changeState(ChangeMessage mess)
    {
        physState = mess.getInfected();
        awareState = mess.getAware();
        if (roleState == RoleState.HUMAN && awareState == AwarenessState.AWARE
                && physState == PhysicalState.SUSCEPTIBLE)
        {
            ms.sendMessage(new CodeMessage(id, mess.getCode()));
        }
    }

    public void setRoundOn(boolean b)
    {
        running = b;
    }

    @Override
    public void run()
    {
        System.out.println("Creating a bot");
        String server = "188.166.154.60";
        int port = 25000;
        running = false;
        try
        {
            physState = PhysicalState.SUSCEPTIBLE;
            awareState = AwarenessState.UNAWARE;
            roleState = RoleState.HUMAN;
            System.out.println("Creating a Socket...");
            Socket s = new Socket(server, port);
            System.out.println("Socket created!");
            ms = new MessageSender(s);
            MessageReceiver mr = new MessageReceiver(this, s);
            mr.setDaemon(true);
            System.out.println("About to start receiver");
            mr.start();
            System.out.println("About to start sending stuff");
            while (true)
            {
                if (id == -1)
                {
                    System.out.println("About to get introduced");
                    ms.sendMessage(new HelloNewMessage());
                    System.out.println("HelloNewMessage object sent");
                    longit += 0.0004 * Math.random();
                    latit += 0.0004 * Math.random();
                }
                else if (running)
                {
                    System.out.println(id + " is about to send position");
                    longit += 0.00008 * Math.random();
                    latit += 0.00008 * Math.random();
                    ms.sendMessage(new PositionMessage(id,
                                new LocationWrapper(longit, latit, 0.0)));
                }
                try
                {
                    Thread.sleep(15000 + (int)(5000 * Math.random()));
                }
                catch (InterruptedException e)
                {
                    System.out.println("InterruptedException? What should I do?");
                }
            }
        }
        catch (IOException e)
        {
            System.out.println("Cannot connect to " + server + ", port " + port);
            return;
        }
    }

    public static void main(String[] args)
    {
        for (int i = 0; i < 10; i++)
        {
            GameBot bot = new GameBot();
            bot.setDaemon(false);
            bot.start();
        }
    }
}
