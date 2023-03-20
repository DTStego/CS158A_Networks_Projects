package edu.sjsu.cs158a;

import java.net.Socket;

public class ServerThread implements Runnable
{
    private Socket socket;
    private String name;

    public ServerThread(Socket socket, String name)
    {
        this.socket = socket;
        this.name = name;
    }

    @Override
    public void run()
    {

    }
}
