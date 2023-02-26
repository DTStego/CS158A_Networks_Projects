package edu.sjsu.cs158a;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.Arrays;

public class HelloUDPServer
{
    public static void main(String[] args) throws IOException
    {
        int port = -1;

        String usageMsg = """
                Usage: HelloUDPServer <port>
                A UDP server that implements the HelloUDP protocol.
                      <port>   port to listen on.""";

        // Program requires one argument that details a port number (int) between 0 and 65535.
        if (args.length < 1)
        {
            System.out.println("Missing required parameter: '<port>'\n" + usageMsg);
            System.exit(2);
        }

        // Retrieve the port number from args and check for incorrect arguments.
        try
        {
            port = Integer.parseInt(args[0]);

            if (port < 0 || port > 65535)
            {
                System.out.println("port must be a number between 0 and 65535\n" + usageMsg);
                System.exit(2);
            }

        }
        catch (NumberFormatException ex)
        {
            System.out.println("port must be a number between 0 and 65535\n" + usageMsg);
            System.exit(2);
        }

        // Reached when a port number has correctly been retrieved but there are additional, unused arguments.
        if (args.length > 1)
        {
            System.out.print("Unmatched arguments from index 1: ");
            // Loop through the arguments past the port number.
            for (int i = 1; i < args.length; i++)
            {
                System.out.print("'" + args[i] + "'");

                if (i != args.length - 1)
                {
                    System.out.print(", ");
                }
            }

            System.out.print("\n" + usageMsg);
            System.exit(2);
        }

        System.out.println("Listening on port " + port);
        DatagramSocket socket = new DatagramSocket(port);

        // A unique conversation number that identifies this conversation.
        // Don't know about conflicts so currently hardcoded.
        int convoNumber = 1331;

        while (true)
        {
            // Receive the first packet!
            byte[] messageBytes = receivePacket(socket);

            if (messageBytes != null)
            {
                // Should contain [the number 1 as a network order 2-byte integer, "hello, i am name (id)"]
                String firstMessage = new String(messageBytes);
            }
        }
    }

    public static byte[] receivePacket(DatagramSocket socket) throws IOException
    {
        socket.setSoTimeout(5000);

        byte[] bytes = new byte[512];
        DatagramPacket packet = new DatagramPacket(bytes, bytes.length);

        try
        {
            socket.receive(packet);
        } catch (SocketTimeoutException ex)
        {
            System.out.println("TIMEOUT ERROR");
            return null;
        }

        System.out.print("From \\" + packet.getSocketAddress() + ": ");

        // Return the byte[] packet but cut the length to the packet size, not 512 bytes.
        return Arrays.copyOf(bytes, packet.getLength());
    }
}
