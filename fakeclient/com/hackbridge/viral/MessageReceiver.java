package com.hackbridge.viral;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.Socket;

public class MessageReceiver extends Thread
{

    private Message      mess;
    private Socket       s;
    private GameBot      ma;

    public MessageReceiver(GameBot ma, Socket s)
    {
        this.ma = ma;
        this.s = s;
    }

    @Override
    public void run()
    {
        try
        {
            ObjectInputStream ois = new ObjectInputStream(s.getInputStream());
            while (true)
            {
                mess = null;
                try
                {
                    while (mess == null)
                    {
                        mess = (Message) ois.readObject();
                    }
                    if (mess instanceof ChangeMessage)
                    {
                        System.out.println("Got a ChangeMessage!");
                        ma.changeState((ChangeMessage) mess);
                    }
                    else if (mess instanceof StopMessage)
                    {
                        System.out.println("Got a StopMessage");
                        ma.setRoundOn(false);
                        
                        // fill out with dummy data
                        ChangeMessage tmp =
                                new ChangeMessage(PhysicalState.SUSCEPTIBLE, AwarenessState.AWARE);
                        ma.changeState(tmp);
                    }
                    else if (mess instanceof StartMessage)
                    {
                        System.out.println("Got a StartMessage");
                        ma.setIdentity(((StartMessage) mess).getId());
                        ma.setRole(((StartMessage) mess).getRole());
                        ma.setRoundOn(((StartMessage) mess).isRunning());

                        StartMessage sm = (StartMessage) mess;
                        ChangeMessage tmp =
                            new ChangeMessage(sm.getInfected(), sm.getAware());
                        if(sm.isRunning())
                        {
                            // regular start message
                            tmp.setCode(sm.getCode());
                            ma.changeState(tmp);
                        }
                        else
                        {
                            // round is not on
                            tmp.setCode("~");
                            ma.changeState(tmp);
                        }
                    }
                    else
                    {
                        System.out.println("Unexpected Message object");
                        // treated silently
                    }
                }
                catch (ClassNotFoundException e)
                {
                    System.out.println("Unknown class received from server");
                    // we're doing this silently
                }
            }
        }
        catch (IOException e)
        {
            System.out.println("IOException caught in MessageReceiver, thread exiting");
            return;
        }
    }

}
