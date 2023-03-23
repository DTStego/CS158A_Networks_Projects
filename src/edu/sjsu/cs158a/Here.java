package edu.sjsu.cs158a;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ConnectException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

// Homework #1
public class Here
{
    public static void main(String[] args) throws IOException, InterruptedException
    {
        if (args.length < 3)
        {
            System.err.println("Not enough arguments!\n" +
                    "Command: [Server/Client/Test] [Host Name if using client/test] [Port Number] [Name if using server]");
            System.exit(2);
        }

        if (args[0].equalsIgnoreCase("server"))
        {
            startServer(Integer.parseInt(args[1]), args[2]);
        }

        if (args[0].equalsIgnoreCase("client"))
        {
            try
            {
                startClient(new Socket(args[1], Integer.parseInt(args[2])));
            }
            catch (ConnectException exception)
            {
                System.err.println("Connection cannot be made to: " + args[1] + ":" + args[2] + "!");
            }
        }

        if (args[0].equalsIgnoreCase("test"))
        {
            new Thread(() ->
            {
                try
                {
                    startServer(Integer.parseInt(args[2]), "Jimmy Nguyen");
                } catch (IOException e)
                {
                    throw new RuntimeException(e);
                }
            }).start();

            Thread.sleep(10);

            new Thread(() ->
            {
                try
                {
                    startClient(new Socket(args[1], Integer.parseInt(args[2])));
                } catch (IOException e)
                {
                    throw new RuntimeException(e);
                }
            }).start();

        }
    }

    public static void startClient(Socket socket) throws IOException
    {
        // Write things to server
        OutputStream out = socket.getOutputStream();

        // Read things from server
        InputStream in = socket.getInputStream();

        // First character is an 8-bit number denoting the length of the message.
        int length = in.read();

        // Server set to send info in chunks of four bytes, need to use readNBytes(lengthOfMsg) to read entire message.
        // The message is stored in ASCII numbers in a byte array.
        byte[] bytes = in.readNBytes(length);

        // Print out the length of the message and the actual message.
        // new String() converts the byte array's ASCII characters into actual words and letters.
        System.out.println(new String(bytes));

        // Example student ID with the initial zero truncated as ints don't accept starting zeros... octal integer.
        int myID = 1555733224;

        // You can use a ByteBuffer to convert it for you.
        byte[] idBytes = ByteBuffer.allocate(4).putInt(myID).array();

        // Converts your name to a byte array to send to server.
        String name = "Jimmy Nguyen";
        byte[] nameBytes = name.getBytes();

        // Sends your ID, length of your name, and your name to the server.
        out.write(idBytes);
        out.write(nameBytes.length);
        out.write(nameBytes);
        out.flush();

        // Read the pin that the server gives back (Specified as four bytes).
        var pin = in.readNBytes(4);

        // Give pin back to server.
        out.write(pin);

        // Read and print the success message that the server should give back.
        var successMessage = in.readNBytes(in.read());

        System.out.println(new String(successMessage));
    }

    public static void startServer(int port, String name) throws IOException
    {
        Executor executor = Executors.newSingleThreadExecutor();

        try (ServerSocket serverSocket = new ServerSocket(port))
        {
            // Ten second server timeout!
            serverSocket.setSoTimeout(10000);

            while (true)
            {
                try
                {
                    executor.execute(new ServerRunnable(serverSocket.accept(), name));
                }
                catch (SocketTimeoutException exception)
                {
                    System.err.println("Server Timeout.");
                    System.exit(2);
                }
            }
        }
    }
}
