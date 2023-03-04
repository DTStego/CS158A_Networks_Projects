package edu.sjsu.cs158a.hw2;

import java.io.IOException;
import java.net.*;
import java.util.Arrays;
import java.util.HashSet;

// Homework #2
public class QotD
{
    public static void main(String[] args) throws IOException
    {
        String serverAddress = args[0];
        int numOfQuotes = 3;

        DatagramSocket socket = new DatagramSocket();

        if (args.length > 2)
        {
            if (args[1].equals("--count"))
            {
                try
                {
                    numOfQuotes = Integer.parseInt(args[2]);
                } catch (NumberFormatException ex)
                {
                    ex.printStackTrace();
                }
            }
        }

        // Run sender and receiver until quotes[] has n amount of quotes that are distinct.
        boolean hasAllQuotes = false;
        String[] quotes = new String[numOfQuotes];

        // Run until
        while (!hasAllQuotes)
        {
            // Send an empty packet to the server which will send back a packet with a quote.
            sendPacket(serverAddress, socket);

            // Receive the packet and store a quote. If the process timed out, method returns null;
            String quote = receivePacket(socket);

            HashSet<String> testList = new HashSet<>(Arrays.asList(quotes));

            // Find an empty spot and put the quote in.
            for (int i = 0; i < quotes.length; i++)
            {
                // If the quote is null (receivePacket() timed out), do nothing.
                // If adding the quote to the hashset returns false, that means it's a duplicate!
                if (quotes[i] == null && quote != null && testList.add(quote))
                    quotes[i] = quote;
            }

            hasAllQuotes = checkQuotes(quotes);
        }

        for (int i = 1; i <= numOfQuotes; i++)
        {
            System.out.println("Quote " + i + ":\n" + quotes[i - 1]);
        }
    }

    /**
     * Check whether the quotes array is full. Elements that are null indicates it is not full.
     * @param quotes A String array
     * @return boolean identifying whether the parameter has every index filled, i.e., not null.
     */
    public static boolean checkQuotes(String[] quotes)
    {
        // If any element in quotes[] is null, return false.
        for (String quote : quotes)
        {
            if (quote == null)
                return false;
        }

        return true;
    }

    public static void sendPacket(String IPAddress, DatagramSocket socket) throws IOException
    {
        if (IPAddress.equals(""))
        {
            System.out.println("No IP Address Given.");
            System.exit(0);
        }

        byte[] bytes = new byte[512];

        DatagramPacket packet = new DatagramPacket(bytes, bytes.length);
        packet.setAddress(InetAddress.getByName(IPAddress));
        packet.setPort(17);
        // System.out.println(packet.getSocketAddress());

        socket.send(packet);
    }

    public static String receivePacket(DatagramSocket socket) throws IOException
    {
        socket.setSoTimeout(1000);

        byte[] bytes = new byte[512];
        DatagramPacket packet = new DatagramPacket(bytes, bytes.length);

        try
        {
            socket.receive(packet);
        } catch (SocketTimeoutException ex)
        {
            return null;
        }

        return new String(bytes, 0, packet.getLength());
    }
}