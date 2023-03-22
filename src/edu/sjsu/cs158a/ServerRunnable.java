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
        BufferedReader inputStream = null;
        PrintWriter outputStream = null;

        try
        {
            inputStream = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            outputStream = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())));
        }
        catch (IOException ex)
        {
            System.out.println("Cannot find client socket info!");
            ex.printStackTrace();
        }

        System.out.println("Server: contact from " + socket.getRemoteSocketAddress());

        String firstMsg = "welcome to CS 158A. send id and name, and i'll send a PIN which you must also send";

        assert outputStream != null;
        outputStream.write(firstMsg.length());
        outputStream.print(firstMsg);
        outputStream.flush();
    }
}
