package com.hackbridge.viral;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class MessageSender
{

    private boolean            updated;
    private Socket             s;
    private ObjectOutputStream oos;

    public MessageSender(Socket s) throws IOException
    {
        this.s = s;
        oos = new ObjectOutputStream(s.getOutputStream());
    }

    public void sendMessage(Message mess)
    {
        try
        {
            oos.writeObject(mess);
        }
        catch (IOException e)
        {
            System.out.println("Could not send message: " + mess.toString());
        }
        return;
    }

}
