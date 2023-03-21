package edu.sjsu.cs158a;

import java.io.*;
import java.net.Socket;

public class ServerRunnable implements Runnable
{
    private Socket socket;
    private String name;

    public ServerRunnable(Socket socket, String name)
    {
        this.socket = socket;
        this.name = name;
    }

    @Override
    public void run()
    {
        BufferedReader inputStream;
        BufferedWriter outputStream;

        try
        {
            inputStream = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            outputStream = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
        }
        catch (IOException ex)
        {
            System.out.println("Cannot find client socket info!");
            ex.printStackTrace();
        }
    }
}
