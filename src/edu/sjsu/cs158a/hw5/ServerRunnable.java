package edu.sjsu.cs158a.hw5;

import java.io.*;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Random;

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
            System.err.println("Connection error. Closing channel.");
            try {socket.close();}catch(IOException ignored){}
        }
    }

    /**
     * Helper method to group IOExceptions in one try catch block since
     * method signature of run() can't be changed to include "throws IOException"
     */
    public void handleClient() throws IOException
    {
        // Used to read from client.
        InputStream inputStream = socket.getInputStream();

        // Used to write to client.
        OutputStream outputStream = socket.getOutputStream();

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

        String firstMsg = "welcome to CS 158A. send id and name, and i'll send a PIN which you must also send";

        ByteBuffer byteBuffer = ByteBuffer.wrap(firstMsg.getBytes());

        // Write the first message.
        outputStream.write(firstMsg.length());
        outputStream.write(byteBuffer.array());

        int SSID = ByteBuffer.wrap(inputStream.readNBytes(4)).getInt();
        int length = inputStream.read();
        String name = new String(inputStream.readNBytes(length));

        System.out.println("contact from " + name + " " + "(" + SSID + ") at " + socket.getRemoteSocketAddress());

        // Create a random PIN and send it to the client.
        byte[] randomPin = ByteBuffer.allocate(4).putInt(new Random().nextInt(1000, 10_000)).array();
        outputStream.write(randomPin);

        byte[] receivedPin = inputStream.readNBytes(4);

        if (ByteBuffer.wrap(randomPin).getInt() != ByteBuffer.wrap(receivedPin).getInt())
        {
            byte[] wrongPin = ByteBuffer.wrap("bad pin".getBytes()).array();
            outputStream.write(wrongPin.length);
            outputStream.write(wrongPin);
            return;
        }

        String successMessage = "you are registered ".concat(name);

        byte[] finishingMessage = ByteBuffer.wrap(successMessage.getBytes()).array();
        outputStream.write(finishingMessage.length);
        outputStream.write(finishingMessage);

        System.out.println("sent message to " + socket.getRemoteSocketAddress() + ": " + successMessage);
    }
}
