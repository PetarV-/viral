package com.hackbridge.viral;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

// For the demo! Yay pretty graphs!
public class Main {

    public static void main(String[] args) {
        try {
            ServerSocket ss = new ServerSocket(25001);
            /*
                In an ideal world, this guy would handle multiple sockets.
                But in demoreality™, only one thing will ever attempt to connect here.
             */
            Socket s = ss.accept();
            OutputStream outStream = s.getOutputStream();
            while (true) {
                File file = new File("ret.png");
                BufferedImage buffImg = ImageIO.read(file);
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                ImageIO.write(buffImg, "png", baos);
                baos.flush();
                outStream.write(baos.toByteArray());
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
