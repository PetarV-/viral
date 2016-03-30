package com.hackbridge.viral;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class MessageSender
{

    private boolean            updated;
    private Socket             s;
    private ObjectOutputStream oos;
    private MainActivity ma;

    public MessageSender(MainActivity m, Socket s) throws IOException
    {
        this.s = s;
        this.ma = m;
        oos = new ObjectOutputStream(s.getOutputStream());
    }

    public void sendMessage(Message mess)
    {
        if (s.isClosed() || s.isInputShutdown() || s.isOutputShutdown())
        {
            ma.restartEverything();
        }
        try
        {
            System.out.println("Sent message: " + mess.toString());
            oos.writeObject(mess);
            oos.flush();
        }
        catch (IOException e)
        {
            System.out.println("Could not send message: " + mess.toString());
            // could not send, so restart everything
            ma.restartEverything();
        }
        return;
    }

}
