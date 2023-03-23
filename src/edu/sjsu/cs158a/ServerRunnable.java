package edu.sjsu.cs158a;

import java.io.*;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.Arrays;

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
        try
        {
            handleClient();
        }
        catch (IOException ex)
        {
            ex.printStackTrace();
        }
    }

    /**
     * Helper method to group IOExceptions in one try catch block since
     * method signature of run() can't be changed to include "throws IOException"
     */
    public void handleClient() throws IOException
    {
        // Used to read from client.
        InputStream inputStream = null;

        // Used to write to client.
        OutputStream outputStream = null;

        inputStream = socket.getInputStream();
        outputStream = socket.getOutputStream();

        if (inputStream == null || outputStream == null)
        {
            System.err.println("Cannot resolve socket information. Closing connection!");
            try
            {
                socket.close();
            }
            catch (IOException e)
            {
                throw new RuntimeException(e);
            }
            return;
        }

        System.out.println("Server: contact from " + socket.getRemoteSocketAddress());

        String firstMsg = "welcome to CS 158A. send id and name, and i'll send a PIN which you must also send";

        ByteBuffer byteBuffer = ByteBuffer.wrap(firstMsg.getBytes());

        // Write the first message.
        outputStream.write(firstMsg.length());
        outputStream.write(byteBuffer.array());


    }
}
